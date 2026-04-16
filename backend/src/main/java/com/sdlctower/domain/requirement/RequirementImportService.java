package com.sdlctower.domain.requirement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdlctower.domain.requirement.dto.ImportInspectionDto;
import com.sdlctower.domain.requirement.dto.ImportInspectionFileDto;
import com.sdlctower.domain.requirement.dto.RawRequirementInputDto;
import com.sdlctower.domain.requirement.dto.RequirementDraftDto;
import com.sdlctower.domain.requirement.dto.RequirementImportFileStatusDto;
import com.sdlctower.domain.requirement.dto.RequirementImportStatusDto;
import com.sdlctower.domain.requirement.persistence.RequirementImportFileEntity;
import com.sdlctower.domain.requirement.persistence.RequirementImportFileRepository;
import com.sdlctower.domain.requirement.persistence.RequirementImportTaskEntity;
import com.sdlctower.domain.requirement.persistence.RequirementImportTaskRepository;
import com.sdlctower.integration.kb.KnowledgeBaseGateway;
import com.sdlctower.shared.exception.ResourceNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class RequirementImportService {

    private static final List<String> SUPPORTED_FILE_TYPES = List.of(
            ".txt", ".md", ".pdf", ".html", ".xlsx", ".xls", ".docx", ".csv", ".htm"
    );
    private static final long MAX_UPLOAD_BATCH_BYTES = 100L * 1024L * 1024L;
    private static final int MAX_PREVIEW_CHARS = 220;

    private final RequirementImportTaskRepository importTaskRepository;
    private final RequirementImportFileRepository importFileRepository;
    private final RequirementService requirementService;
    private final KnowledgeBaseGateway knowledgeBaseGateway;
    private final ObjectMapper objectMapper;

    public RequirementImportService(
            RequirementImportTaskRepository importTaskRepository,
            RequirementImportFileRepository importFileRepository,
            RequirementService requirementService,
            KnowledgeBaseGateway knowledgeBaseGateway,
            ObjectMapper objectMapper
    ) {
        this.importTaskRepository = importTaskRepository;
        this.importFileRepository = importFileRepository;
        this.requirementService = requirementService;
        this.knowledgeBaseGateway = knowledgeBaseGateway;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public RequirementImportStatusDto startImport(String kbName, String profileId, List<MultipartFile> files) {
        if (kbName == null || kbName.isBlank()) {
            throw new IllegalArgumentException("kb_name is required");
        }

        List<MultipartFile> uploadedFiles = validateUploadedFiles(files);
        List<PreparedImportSource> preparedSources = prepareSources(uploadedFiles);
        String resolvedProfileId = profileId == null || profileId.isBlank() ? "standard-sdd" : profileId.trim();
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        String importId = UUID.randomUUID().toString();
        KnowledgeBaseGateway.KnowledgeBaseTarget target = knowledgeBaseGateway.resolveKnowledgeBase(kbName.trim());

        int successCount = 0;
        int failureCount = 0;
        Set<String> unsupportedTypes = new LinkedHashSet<>();
        List<RequirementImportFileEntity> fileEntities = new ArrayList<>();

        for (PreparedImportSource source : preparedSources) {
            RequirementImportFileEntity entity = RequirementImportFileEntity.create(
                    importId,
                    source.displayName(),
                    source.sourceFileName(),
                    source.sourceKind(),
                    source.fileExtension(),
                    source.contentType(),
                    source.fileSize(),
                    source.supported(),
                    source.supported() ? "QUEUED" : "UNSUPPORTED",
                    source.errorMessage(),
                    now
            );

            if (!source.supported()) {
                failureCount += 1;
                if (source.fileExtension() != null && !source.fileExtension().isBlank()) {
                    unsupportedTypes.add(source.fileExtension());
                }
                fileEntities.add(entity);
                continue;
            }

            try {
                KnowledgeBaseGateway.UploadResult uploadResult = knowledgeBaseGateway.uploadFile(
                        target,
                        source.displayName(),
                        source.contentType(),
                        source.fileBytes()
                );
                entity.markUploaded(
                        uploadResult.documentId(),
                        uploadResult.batchId(),
                        normalizeProviderStatus(uploadResult.indexingStatus()),
                        now
                );
                successCount += 1;
            } catch (RuntimeException exception) {
                entity.markFailed("ERROR", exception.getMessage(), now);
                failureCount += 1;
            }
            fileEntities.add(entity);
        }

        RequirementImportTaskEntity task = importTaskRepository.save(RequirementImportTaskEntity.create(
                importId,
                target.name(),
                target.datasetId(),
                knowledgeBaseGateway.providerName(),
                resolvedProfileId,
                determineSubmissionStatus(successCount, failureCount),
                buildSubmissionMessage(target.name(), successCount, failureCount),
                preparedSources.size(),
                successCount,
                failureCount,
                preparedSources.stream().mapToLong(PreparedImportSource::fileSize).sum(),
                joinValues(SUPPORTED_FILE_TYPES),
                joinValues(unsupportedTypes.stream().toList()),
                now
        ));
        fileEntities = importFileRepository.saveAll(fileEntities);

        return toStatusDto(task, fileEntities, null);
    }

    @Transactional
    public RequirementImportStatusDto getImportStatus(String importId) {
        RequirementImportTaskEntity task = importTaskRepository.findById(importId)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement import not found: " + importId));
        List<RequirementImportFileEntity> fileEntities = importFileRepository.findByImportIdOrderByIdAsc(importId);

        RequirementDraftDto draft = readDraft(task.getDraftPayloadJson());
        if (!"DRAFT_READY".equals(task.getStatus()) && !"FAILED".equals(task.getStatus())) {
            draft = refreshImportStatus(task, fileEntities);
        }
        return toStatusDto(task, fileEntities, draft);
    }

    private RequirementDraftDto refreshImportStatus(
            RequirementImportTaskEntity task,
            List<RequirementImportFileEntity> fileEntities
    ) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        KnowledgeBaseGateway.KnowledgeBaseTarget target = new KnowledgeBaseGateway.KnowledgeBaseTarget(task.getKbName(), task.getDatasetId());
        int dynamicFailures = 0;
        boolean hasPending = false;

        for (RequirementImportFileEntity file : fileEntities) {
            if (!file.isSupported() || file.getProviderDocumentId() == null) {
                if (!file.isSupported()) {
                    dynamicFailures += 1;
                }
                continue;
            }

            KnowledgeBaseGateway.DocumentStatus status = knowledgeBaseGateway.getDocumentStatus(target, file.getProviderDocumentId());
            if (status.isCompleted()) {
                file.markCompleted("COMPLETED", file.getExtractedPreview(), now);
            } else if (status.isFailed()) {
                file.markFailed("ERROR", status.errorMessage(), now);
                dynamicFailures += 1;
            } else {
                file.markUploaded(file.getProviderDocumentId(), file.getProviderBatchId(), normalizeProviderStatus(status.indexingStatus()), now);
                hasPending = true;
            }
            importFileRepository.save(file);
        }

        boolean allCompleted = fileEntities.stream()
                .filter(RequirementImportFileEntity::isSupported)
                .allMatch(file -> "COMPLETED".equals(file.getProviderStatus()) || "ERROR".equals(file.getProviderStatus()));
        boolean anyCompleted = fileEntities.stream().anyMatch(file -> "COMPLETED".equals(file.getProviderStatus()));

        if (allCompleted && anyCompleted) {
            RequirementDraftDto draft = buildDraft(task, fileEntities, target, now);
            task.storeDraft(writeDraft(draft), "DRAFT_READY", "Draft ready for review.", now);
            importTaskRepository.save(task);
            return draft;
        }

        if (!hasPending && !anyCompleted) {
            task.updateProgress("FAILED", "All uploaded files failed during knowledge base processing.", task.getNumberOfSuccesses(), dynamicFailures, now);
            importTaskRepository.save(task);
            return null;
        }

        String status = dynamicFailures > 0 ? "PARTIAL_SUCCESS" : "PROCESSING";
        String message = dynamicFailures > 0
                ? "Knowledge base processing is in progress, but some files already failed."
                : "Knowledge base processing is still running.";
        task.updateProgress(status, message, task.getNumberOfSuccesses(), dynamicFailures, now);
        importTaskRepository.save(task);
        return null;
    }

    private RequirementDraftDto buildDraft(
            RequirementImportTaskEntity task,
            List<RequirementImportFileEntity> fileEntities,
            KnowledgeBaseGateway.KnowledgeBaseTarget target,
            Instant now
    ) {
        List<String> aggregatedNames = new ArrayList<>();
        List<String> missingInfo = new ArrayList<>();
        List<String> openQuestions = new ArrayList<>();
        List<ImportInspectionFileDto> inspectionFiles = new ArrayList<>();
        StringBuilder sourceText = new StringBuilder();

        for (RequirementImportFileEntity file : fileEntities) {
            aggregatedNames.add(file.getDisplayName());
            if (!file.isSupported()) {
                inspectionFiles.add(new ImportInspectionFileDto(
                        file.getDisplayName(),
                        detectInspectionType(file.getDisplayName()),
                        "MANUAL_REVIEW",
                        "Unsupported file type for knowledge base ingestion",
                        null,
                        null
                ));
                missingInfo.add("Unsupported source file requires manual review: " + file.getDisplayName());
                continue;
            }

            if ("ERROR".equals(file.getProviderStatus())) {
                inspectionFiles.add(new ImportInspectionFileDto(
                        file.getDisplayName(),
                        detectInspectionType(file.getDisplayName()),
                        "MANUAL_REVIEW",
                        file.getErrorMessage() == null ? "Knowledge base processing failed" : file.getErrorMessage(),
                        null,
                        null
                ));
                missingInfo.add("Knowledge base processing failed for " + file.getDisplayName());
                continue;
            }

            List<String> segments = knowledgeBaseGateway.getDocumentSegments(target, file.getProviderDocumentId());
            String mergedText = segments.stream()
                    .filter(segment -> segment != null && !segment.isBlank())
                    .reduce((left, right) -> left + "\n" + right)
                    .orElse("");
            String preview = buildPreview(mergedText);
            file.markCompleted("COMPLETED", preview, now);
            importFileRepository.save(file);
            inspectionFiles.add(new ImportInspectionFileDto(
                    file.getDisplayName(),
                    detectInspectionType(file.getDisplayName()),
                    mergedText.isBlank() ? "MANUAL_REVIEW" : "PARSED",
                    mergedText.isBlank() ? "No segment content returned from knowledge base" : "Indexed by knowledge base and ready for normalization",
                    mergedText.isBlank() ? null : mergedText.length(),
                    preview
            ));

            if (mergedText.isBlank()) {
                missingInfo.add("No chunk content was returned for " + file.getDisplayName());
                continue;
            }

            sourceText.append("Source file: ")
                    .append(file.getDisplayName())
                    .append('\n')
                    .append(mergedText)
                    .append("\n\n");
        }

        if (inspectionFiles.stream().filter(file -> "PARSED".equals(file.processingStatus())).count() > 1) {
            openQuestions.add("Which uploaded file should be treated as the source of truth for this requirement?");
            openQuestions.add("Should these uploaded files be merged into one requirement or split into multiple requirements?");
        }

        ImportInspectionDto inspection = buildInspection(task, inspectionFiles);
        RawRequirementInputDto rawInput = new RawRequirementInputDto(
                "FILE",
                sourceText.toString().trim(),
                summarizeFileNames(aggregatedNames),
                task.getTotalSize(),
                List.copyOf(aggregatedNames),
                aggregatedNames.size(),
                task.getKbName()
        );

        return requirementService.normalizeImportedSource(
                task.getProfileId(),
                rawInput,
                true,
                buildImportSummary(task, aggregatedNames, inspection),
                aggregatedNames.size() == 1
                        ? "Imported requirement from " + aggregatedNames.get(0)
                        : "Imported requirement package from " + aggregatedNames.size() + " files",
                deduplicateStrings(missingInfo),
                deduplicateStrings(openQuestions),
                inspection,
                true
        );
    }

    private RequirementImportStatusDto toStatusDto(
            RequirementImportTaskEntity task,
            List<RequirementImportFileEntity> fileEntities,
            RequirementDraftDto draft
    ) {
        return new RequirementImportStatusDto(
                task.getId(),
                task.getId(),
                task.getStatus(),
                task.getMessage(),
                task.getKbName(),
                task.getDatasetId(),
                task.getTotalNumberOfFiles(),
                task.getNumberOfSuccesses(),
                task.getNumberOfFailures(),
                task.getTotalSize(),
                splitValues(task.getUnsupportedFileTypes()),
                splitValues(task.getSupportedFileTypes()),
                fileEntities.stream().map(this::toFileStatusDto).toList(),
                draft != null ? draft : readDraft(task.getDraftPayloadJson()),
                task.getCreatedAt().toString(),
                task.getUpdatedAt().toString()
        );
    }

    private RequirementImportFileStatusDto toFileStatusDto(RequirementImportFileEntity entity) {
        return new RequirementImportFileStatusDto(
                entity.getDisplayName(),
                entity.getSourceFileName(),
                entity.getSourceKind(),
                entity.getFileExtension(),
                entity.getFileSize(),
                entity.isSupported(),
                entity.getProviderStatus(),
                entity.getErrorMessage(),
                entity.getExtractedPreview(),
                entity.getProviderDocumentId()
        );
    }

    private List<MultipartFile> validateUploadedFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }
        long totalSize = 0L;
        List<MultipartFile> result = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Uploaded file must not be empty");
            }
            result.add(file);
            totalSize += file.getSize();
        }
        if (totalSize > MAX_UPLOAD_BATCH_BYTES) {
            throw new IllegalArgumentException("Total upload size exceeds the 100 MB limit for one request");
        }
        return List.copyOf(result);
    }

    private List<PreparedImportSource> prepareSources(List<MultipartFile> uploadedFiles) {
        List<PreparedImportSource> preparedSources = new ArrayList<>();
        for (MultipartFile uploadedFile : uploadedFiles) {
            String fileName = safeFileName(uploadedFile);
            try {
                if (fileName.toLowerCase(Locale.ROOT).endsWith(".zip")) {
                    preparedSources.addAll(expandZipFile(uploadedFile, fileName));
                    continue;
                }
                preparedSources.add(createPreparedSource(
                        fileName,
                        fileName,
                        "FILE",
                        uploadedFile.getContentType(),
                        uploadedFile.getBytes()
                ));
            } catch (IOException exception) {
                preparedSources.add(PreparedImportSource.unsupported(
                        fileName,
                        fileName,
                        "FILE",
                        detectExtension(fileName),
                        uploadedFile.getSize(),
                        "Failed to read uploaded file"
                ));
            }
        }
        return preparedSources;
    }

    private List<PreparedImportSource> expandZipFile(MultipartFile uploadedFile, String archiveName) throws IOException {
        List<PreparedImportSource> preparedSources = new ArrayList<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(uploadedFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory() || shouldSkipArchiveEntry(entry.getName())) {
                    zipInputStream.closeEntry();
                    continue;
                }
                byte[] bytes = readAllBytes(zipInputStream);
                preparedSources.add(createPreparedSource(
                        entry.getName(),
                        shortFileName(entry.getName()),
                        "ZIP_ENTRY",
                        MediaTypeFactory.getMediaType(shortFileName(entry.getName())).orElse(MediaType.APPLICATION_OCTET_STREAM).toString(),
                        bytes
                ));
                zipInputStream.closeEntry();
            }
        }
        if (preparedSources.isEmpty()) {
            preparedSources.add(PreparedImportSource.unsupported(
                    archiveName,
                    archiveName,
                    "ZIP",
                    ".zip",
                    uploadedFile.getSize(),
                    "ZIP package was empty or contained only ignored metadata entries"
            ));
        }
        return preparedSources;
    }

    private PreparedImportSource createPreparedSource(
            String displayName,
            String sourceFileName,
            String sourceKind,
            String contentType,
            byte[] fileBytes
    ) {
        String extension = detectExtension(displayName);
        if (!SUPPORTED_FILE_TYPES.contains(extension)) {
            return PreparedImportSource.unsupported(
                    displayName,
                    sourceFileName,
                    sourceKind,
                    extension,
                    fileBytes.length,
                    "Unsupported file type for knowledge base ingestion"
            );
        }
        return PreparedImportSource.supported(
                displayName,
                sourceFileName,
                sourceKind,
                extension,
                contentType == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : contentType,
                fileBytes
        );
    }

    private boolean shouldSkipArchiveEntry(String entryName) {
        String shortName = shortFileName(entryName);
        return entryName.startsWith("__MACOSX/")
                || ".DS_Store".equals(shortName)
                || shortName.startsWith(".");
    }

    private byte[] readAllBytes(ZipInputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        return outputStream.toByteArray();
    }

    private ImportInspectionDto buildInspection(RequirementImportTaskEntity task, List<ImportInspectionFileDto> files) {
        int parsedFiles = (int) files.stream().filter(file -> "PARSED".equals(file.processingStatus())).count();
        int manualReviewFiles = (int) files.stream().filter(file -> "MANUAL_REVIEW".equals(file.processingStatus())).count();
        int skippedFiles = (int) files.stream().filter(file -> "SKIPPED".equals(file.processingStatus())).count();
        String sourceKind = task.getTotalNumberOfFiles() == 1 ? "FILE" : "BATCH";
        return new ImportInspectionDto(
                summarizeFileNames(files.stream().map(ImportInspectionFileDto::fileName).toList()),
                sourceKind,
                files.size(),
                parsedFiles,
                manualReviewFiles,
                skippedFiles,
                List.copyOf(files)
        );
    }

    private String buildImportSummary(
            RequirementImportTaskEntity task,
            List<String> fileNames,
            ImportInspectionDto inspection
    ) {
        List<String> parts = new ArrayList<>();
        parts.add("Imported " + fileNames.size() + " source file(s) for knowledge base " + task.getKbName() + ": " + summarizeFileNames(fileNames) + ".");
        if (inspection.parsedFiles() > 0) {
            parts.add("Parsed " + inspection.parsedFiles() + " file(s) through knowledge base indexing.");
        }
        if (inspection.manualReviewFiles() > 0) {
            parts.add("Manual review still needed for " + inspection.manualReviewFiles() + " file(s).");
        }
        return String.join(" ", parts);
    }

    private String summarizeFileNames(List<String> fileNames) {
        if (fileNames.isEmpty()) {
            return "uploaded-files";
        }
        if (fileNames.size() == 1) {
            return fileNames.get(0);
        }
        if (fileNames.size() == 2) {
            return fileNames.get(0) + ", " + fileNames.get(1);
        }
        return fileNames.get(0) + " + " + (fileNames.size() - 1) + " more";
    }

    private String determineSubmissionStatus(int successCount, int failureCount) {
        if (successCount == 0) {
            return "FAILED";
        }
        if (failureCount > 0) {
            return "PARTIAL_SUCCESS";
        }
        return "PROCESSING";
    }

    private String buildSubmissionMessage(String kbName, int successCount, int failureCount) {
        if (successCount == 0) {
            return "No files were accepted for knowledge base '" + kbName + "'.";
        }
        if (failureCount > 0) {
            return "Partial success, the number of successful records is " + successCount + ", and the number of failed records is " + failureCount + ".";
        }
        return "Files uploaded successfully for knowledge base '" + kbName + "'. Processing in background.";
    }

    private String safeFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return "uploaded-file";
        }
        return shortFileName(originalFilename);
    }

    private String shortFileName(String fileName) {
        int slash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        return slash >= 0 ? fileName.substring(slash + 1) : fileName;
    }

    private String detectExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        return fileName.substring(dot).toLowerCase(Locale.ROOT);
    }

    private String detectInspectionType(String fileName) {
        String extension = detectExtension(fileName);
        return switch (extension) {
            case ".txt", ".md", ".html", ".htm" -> "TEXT";
            case ".csv", ".xlsx", ".xls" -> "SPREADSHEET";
            case ".docx" -> "DOCUMENT";
            case ".pdf" -> "PDF";
            default -> "FILE";
        };
    }

    private String normalizeProviderStatus(String status) {
        return status == null || status.isBlank() ? "QUEUED" : status.toUpperCase(Locale.ROOT);
    }

    private String buildPreview(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String compact = value.replace('\n', ' ').replaceAll("\\s+", " ").trim();
        if (compact.length() <= MAX_PREVIEW_CHARS) {
            return compact;
        }
        return compact.substring(0, MAX_PREVIEW_CHARS).trim() + "...";
    }

    private List<String> deduplicateStrings(List<String> values) {
        return new ArrayList<>(new LinkedHashSet<>(values));
    }

    private String joinValues(List<String> values) {
        return String.join(",", values);
    }

    private List<String> splitValues(String values) {
        if (values == null || values.isBlank()) {
            return List.of();
        }
        return List.of(values.split(","));
    }

    private String writeDraft(RequirementDraftDto draft) {
        try {
            return objectMapper.writeValueAsString(draft);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize requirement draft", exception);
        }
    }

    private RequirementDraftDto readDraft(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(payload, RequirementDraftDto.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize requirement draft", exception);
        }
    }

    private record PreparedImportSource(
            String displayName,
            String sourceFileName,
            String sourceKind,
            String fileExtension,
            String contentType,
            long fileSize,
            byte[] fileBytes,
            boolean supported,
            String errorMessage
    ) {
        private static PreparedImportSource supported(
                String displayName,
                String sourceFileName,
                String sourceKind,
                String fileExtension,
                String contentType,
                byte[] fileBytes
        ) {
            return new PreparedImportSource(
                    displayName,
                    sourceFileName,
                    sourceKind,
                    fileExtension,
                    contentType,
                    fileBytes.length,
                    fileBytes,
                    true,
                    null
            );
        }

        private static PreparedImportSource unsupported(
                String displayName,
                String sourceFileName,
                String sourceKind,
                String fileExtension,
                long fileSize,
                String errorMessage
        ) {
            return new PreparedImportSource(
                    displayName,
                    sourceFileName,
                    sourceKind,
                    fileExtension,
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    fileSize,
                    new byte[0],
                    false,
                    errorMessage
            );
        }
    }
}
