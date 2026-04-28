package com.sdlctower.domain.requirement;

import com.sdlctower.domain.requirement.dto.AcceptanceCriterionDto;
import com.sdlctower.domain.requirement.dto.AiAnalysisDto;
import com.sdlctower.domain.requirement.dto.CreateRequirementRequestDto;
import com.sdlctower.domain.requirement.dto.GenerateSpecRequestDto;
import com.sdlctower.domain.requirement.dto.GenerationResultDto;
import com.sdlctower.domain.requirement.dto.ImportInspectionDto;
import com.sdlctower.domain.requirement.dto.ImportInspectionFileDto;
import com.sdlctower.domain.requirement.dto.InvokeSkillRequestDto;
import com.sdlctower.domain.requirement.dto.LinkedSpecDto;
import com.sdlctower.domain.requirement.dto.LinkedSpecsSectionDto;
import com.sdlctower.domain.requirement.dto.LinkedStoriesSectionDto;
import com.sdlctower.domain.requirement.dto.LinkedStoryDto;
import com.sdlctower.domain.requirement.dto.OrchestratorResultDto;
import com.sdlctower.domain.requirement.dto.RawRequirementInputDto;
import com.sdlctower.domain.requirement.dto.RequirementDraftDto;
import com.sdlctower.domain.requirement.dto.RequirementDescriptionDto;
import com.sdlctower.domain.requirement.dto.RequirementDetailDto;
import com.sdlctower.domain.requirement.dto.RequirementHeaderDto;
import com.sdlctower.domain.requirement.dto.RequirementListDto;
import com.sdlctower.domain.requirement.dto.RequirementListItemDto;
import com.sdlctower.domain.requirement.dto.RequirementNormalizeRequestDto;
import com.sdlctower.domain.requirement.dto.SdlcChainDto;
import com.sdlctower.domain.requirement.dto.SdlcChainLinkDto;
import com.sdlctower.domain.requirement.dto.SkillExecutionResultDto;
import com.sdlctower.domain.requirement.dto.SimilarRequirementDto;
import com.sdlctower.domain.requirement.dto.StatusDistributionDto;
import com.sdlctower.domain.requirement.persistence.RequirementAcceptanceCriterionEntity;
import com.sdlctower.domain.requirement.persistence.RequirementAcceptanceCriterionRepository;
import com.sdlctower.domain.requirement.persistence.RequirementAiAnalysisElementEntity;
import com.sdlctower.domain.requirement.persistence.RequirementAiAnalysisElementRepository;
import com.sdlctower.domain.requirement.persistence.RequirementAiAnalysisEntity;
import com.sdlctower.domain.requirement.persistence.RequirementAiAnalysisRepository;
import com.sdlctower.domain.requirement.persistence.RequirementAssumptionEntity;
import com.sdlctower.domain.requirement.persistence.RequirementAssumptionRepository;
import com.sdlctower.domain.requirement.persistence.RequirementConstraintEntity;
import com.sdlctower.domain.requirement.persistence.RequirementConstraintRepository;
import com.sdlctower.domain.requirement.persistence.RequirementEntity;
import com.sdlctower.domain.requirement.persistence.RequirementImportAuditEntity;
import com.sdlctower.domain.requirement.persistence.RequirementImportAuditRepository;
import com.sdlctower.domain.requirement.persistence.RequirementRepository;
import com.sdlctower.domain.requirement.persistence.RequirementSdlcChainLinkEntity;
import com.sdlctower.domain.requirement.persistence.RequirementSdlcChainLinkRepository;
import com.sdlctower.domain.requirement.persistence.RequirementSpecEntity;
import com.sdlctower.domain.requirement.persistence.RequirementSpecRepository;
import com.sdlctower.domain.requirement.persistence.UserStoryEntity;
import com.sdlctower.domain.requirement.persistence.UserStoryRepository;
import com.sdlctower.platform.profile.PipelineProfileDto;
import com.sdlctower.platform.profile.PipelineProfileService;
import com.sdlctower.shared.dto.SectionResultDto;
import com.sdlctower.shared.exception.ResourceNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Service
public class RequirementService {

    private static final Logger log = LoggerFactory.getLogger(RequirementService.class);
    private static final Map<String, Integer> PRIORITY_ORDER = Map.of(
            "Critical", 0,
            "High", 1,
            "Medium", 2,
            "Low", 3
    );
    private static final Map<String, Integer> STATUS_ORDER = Map.of(
            "Draft", 0,
            "In Review", 1,
            "Approved", 2,
            "In Progress", 3,
            "Delivered", 4,
            "Archived", 5
    );
    private static final int MAX_ARCHIVE_ENTRIES = 20;
    private static final int MAX_ARCHIVE_TOTAL_CHARS = 16000;
    private static final int MAX_ARCHIVE_ENTRY_BYTES = 262144;
    private static final int MAX_PREVIEW_CHARS = 220;
    private static final int MAX_SPREADSHEET_ROWS = 20;
    private static final long MAX_UPLOAD_BATCH_BYTES = 100L * 1024L * 1024L;

    private static final List<RequirementListItemDto> ALL_REQUIREMENTS = List.of(
            new RequirementListItemDto("REQ-0001", "User Authentication and SSO Integration", "Critical", "Approved", "Functional", 4, 2, 85, "Sarah Chen", "2026-04-10T09:00:00Z", "2026-04-16T10:30:00Z"),
            new RequirementListItemDto("REQ-0002", "Role-Based Access Control", "High", "In Progress", "Functional", 3, 1, 60, "Alex Kim", "2026-04-11T10:00:00Z", "2026-04-16T09:15:00Z"),
            new RequirementListItemDto("REQ-0003", "API Response Time Under 200ms", "High", "Draft", "Non-Functional", 0, 0, 15, "Unassigned", "2026-04-15T16:00:00Z", "2026-04-15T16:00:00Z"),
            new RequirementListItemDto("REQ-0004", "Database Migration to Oracle 23ai", "Medium", "In Review", "Technical", 2, 1, 45, "David Park", "2026-04-12T09:00:00Z", "2026-04-15T14:20:00Z"),
            new RequirementListItemDto("REQ-0005", "Audit Trail for Compliance", "Critical", "In Progress", "Business", 3, 2, 70, "Mike Ross", "2026-04-12T08:00:00Z", "2026-04-16T08:45:00Z"),
            new RequirementListItemDto("REQ-0006", "User Profile Management", "Low", "Delivered", "Functional", 2, 1, 100, "Emily Wang", "2026-04-05T09:00:00Z", "2026-04-14T11:00:00Z"),
            new RequirementListItemDto("REQ-0007", "99.9% Uptime SLA", "Medium", "Approved", "Non-Functional", 1, 1, 50, "Sarah Chen", "2026-04-08T10:00:00Z", "2026-04-14T09:30:00Z"),
            new RequirementListItemDto("REQ-0008", "Event-Driven Architecture Migration", "High", "Draft", "Technical", 0, 0, 10, "Unassigned", "2026-04-13T17:00:00Z", "2026-04-13T17:00:00Z"),
            new RequirementListItemDto("REQ-0009", "Legacy Report Export", "Medium", "Archived", "Business", 2, 1, 100, "Alex Kim", "2026-03-20T09:00:00Z", "2026-04-10T10:00:00Z"),
            new RequirementListItemDto("REQ-0010", "AI-Powered Requirement Analysis", "High", "In Review", "Functional", 2, 0, 35, "Sarah Chen", "2026-04-15T07:00:00Z", "2026-04-16T07:00:00Z")
    );

    private static final StatusDistributionDto DISTRIBUTION = new StatusDistributionDto(2, 2, 2, 2, 1, 1);
    private static final Map<String, Set<String>> REQUIREMENT_STORY_IDS = Map.of(
            "REQ-0001", Set.of("US-001", "US-002", "US-003", "US-004"),
            "REQ-0002", Set.of("US-010", "US-011", "US-012"),
            "REQ-0003", Set.of(),
            "REQ-0004", Set.of("US-030", "US-031"),
            "REQ-0005", Set.of("US-040", "US-041", "US-042"),
            "REQ-0006", Set.of("US-050", "US-051"),
            "REQ-0007", Set.of("US-060"),
            "REQ-0008", Set.of(),
            "REQ-0009", Set.of("US-080", "US-081"),
            "REQ-0010", Set.of("US-090", "US-091")
    );
    private static final Map<String, String> STORY_REQUIREMENT_IDS = buildStoryRequirementIds();
    private final PipelineProfileService pipelineProfileService;
    private final RequirementRepository requirementRepository;
    private final RequirementAcceptanceCriterionRepository acceptanceCriterionRepository;
    private final RequirementAssumptionRepository assumptionRepository;
    private final RequirementConstraintRepository constraintRepository;
    private final UserStoryRepository userStoryRepository;
    private final RequirementSpecRepository requirementSpecRepository;
    private final RequirementAiAnalysisRepository analysisRepository;
    private final RequirementAiAnalysisElementRepository analysisElementRepository;
    private final RequirementSdlcChainLinkRepository chainLinkRepository;
    private final RequirementImportAuditRepository importAuditRepository;

    public RequirementService(
            PipelineProfileService pipelineProfileService,
            RequirementRepository requirementRepository,
            RequirementAcceptanceCriterionRepository acceptanceCriterionRepository,
            RequirementAssumptionRepository assumptionRepository,
            RequirementConstraintRepository constraintRepository,
            UserStoryRepository userStoryRepository,
            RequirementSpecRepository requirementSpecRepository,
            RequirementAiAnalysisRepository analysisRepository,
            RequirementAiAnalysisElementRepository analysisElementRepository,
            RequirementSdlcChainLinkRepository chainLinkRepository,
            RequirementImportAuditRepository importAuditRepository
    ) {
        this.pipelineProfileService = pipelineProfileService;
        this.requirementRepository = requirementRepository;
        this.acceptanceCriterionRepository = acceptanceCriterionRepository;
        this.assumptionRepository = assumptionRepository;
        this.constraintRepository = constraintRepository;
        this.userStoryRepository = userStoryRepository;
        this.requirementSpecRepository = requirementSpecRepository;
        this.analysisRepository = analysisRepository;
        this.analysisElementRepository = analysisElementRepository;
        this.chainLinkRepository = chainLinkRepository;
        this.importAuditRepository = importAuditRepository;
    }

    public RequirementListDto getRequirementList(
            String priority,
            String status,
            String category,
            String search,
            String sortBy,
            String sortDirection
    ) {
        List<RequirementListItemDto> allRequirements = getAllRequirements();
        List<RequirementListItemDto> filtered = allRequirements.stream()
                .filter(r -> priority == null || r.priority().equalsIgnoreCase(priority))
                .filter(r -> status == null || r.status().equalsIgnoreCase(status))
                .filter(r -> category == null || r.category().equalsIgnoreCase(category))
                .filter(r -> {
                    if (search == null || search.isBlank()) return true;
                    String term = search.toLowerCase();
                    return r.title().toLowerCase().contains(term) || r.id().toLowerCase().contains(term);
                })
                .toList();
        return new RequirementListDto(
                buildDistribution(allRequirements),
                sortRequirements(filtered, sortBy, sortDirection),
                filtered.size());
    }

    public RequirementDetailDto getRequirementDetail(String requirementId) {
        if (requirementRepository.existsById(requirementId)) {
            return buildRequirementDetailFromDatabase(requirementId);
        }
        var builder = DETAIL_BUILDERS.get(requirementId);
        if (builder == null) {
            throw new ResourceNotFoundException("Requirement not found: " + requirementId);
        }
        return builder.get();
    }

    public GenerationResultDto generateStories(String requirementId) {
        validateExists(requirementId);
        return buildGenerationResult(
                "EXEC-STORY-" + requirementId,
                "req-to-user-story",
                "RUNNING",
                requirementId,
                null,
                "2026-04-16T11:30:00Z",
                15,
                "Story generation initiated for " + requirementId
        );
    }

    public GenerationResultDto generateSpec(String storyId) {
        String requirementId = findRequirementIdForStory(storyId);
        return buildSpecGenerationResult(requirementId, List.of(storyId));
    }

    public GenerationResultDto generateSpec(String requirementId, GenerateSpecRequestDto body) {
        validateExists(requirementId);
        List<String> storyIds = body != null ? body.storyIds() : null;
        if (storyIds == null || storyIds.isEmpty() || storyIds.stream().anyMatch(id -> id == null || id.isBlank())) {
            throw new IllegalArgumentException("At least one story ID is required");
        }

        List<String> normalizedStoryIds = storyIds.stream()
                .map(String::trim)
                .toList();
        if (requirementRepository.existsById(requirementId)) {
            List<UserStoryEntity> linkedStories = userStoryRepository.findByRequirementIdAndIdIn(requirementId, normalizedStoryIds);
            if (linkedStories.size() != normalizedStoryIds.size()) {
                Set<String> linkedIds = linkedStories.stream()
                        .map(UserStoryEntity::getId)
                        .collect(java.util.stream.Collectors.toSet());
                String invalidStoryId = normalizedStoryIds.stream()
                        .filter(id -> !linkedIds.contains(id))
                        .findFirst()
                        .orElse(normalizedStoryIds.get(0));
                throw new IllegalArgumentException("Story " + invalidStoryId + " is not linked to requirement " + requirementId);
            }
        } else {
            Set<String> linkedStories = REQUIREMENT_STORY_IDS.getOrDefault(requirementId, Set.of());
            for (String storyId : normalizedStoryIds) {
                if (!linkedStories.contains(storyId)) {
                    throw new IllegalArgumentException("Story " + storyId + " is not linked to requirement " + requirementId);
                }
            }
        }

        return buildSpecGenerationResult(requirementId, normalizedStoryIds);
    }

    public GenerationResultDto runAnalysis(String requirementId) {
        validateExists(requirementId);
        return buildGenerationResult(
                "EXEC-ANALYSIS-" + requirementId,
                "requirement-analysis",
                "RUNNING",
                requirementId,
                null,
                "2026-04-16T12:00:00Z",
                10,
                "AI analysis initiated for " + requirementId
        );
    }

    public SdlcChainDto getRequirementChain(String requirementId) {
        return getRequirementDetail(requirementId).sdlcChain().data();
    }

    public AiAnalysisDto getRequirementAnalysis(String requirementId) {
        return getRequirementDetail(requirementId).aiAnalysis().data();
    }

    public SkillExecutionResultDto invokeSkill(String requirementId, InvokeSkillRequestDto body) {
        validateExists(requirementId);
        if (body == null || body.skillId() == null || body.skillId().isBlank()) {
            throw new IllegalArgumentException("skillId is required");
        }

        String skillId = body.skillId().trim();
        OrchestratorResultDto orchestratorResult = null;
        String message = "Skill " + skillId + " triggered for " + requirementId;

        if ("ibm-i-workflow-orchestrator".equals(skillId)) {
            orchestratorResult = new OrchestratorResultDto(
                    "enhancement",
                    "L2",
                    "High",
                    "Existing RPG/COBOL source modification detected — source analysis required before Program Spec generation."
            );
            message = "IBM i orchestrator triggered for " + requirementId;
        }

        return new SkillExecutionResultDto(
                "EXEC-SKILL-" + requirementId + "-" + normalizeExecutionToken(skillId),
                skillId,
                "TRIGGERED",
                requirementId,
                timestampNow(),
                20,
                message,
                orchestratorResult
        );
    }

    public RequirementDraftDto normalizeRequirement(RequirementNormalizeRequestDto body) {
        if (body == null || body.rawInput() == null) {
            throw new IllegalArgumentException("rawInput is required");
        }
        if (body.profileId() == null || body.profileId().isBlank()) {
            throw new IllegalArgumentException("profileId is required");
        }

        PipelineProfileDto profile = pipelineProfileService.getProfile(body.profileId());
        return buildNormalizedDraft(profile, body.rawInput(), false, null, null, List.of(), List.of(), null, false);
    }

    public RequirementDraftDto normalizeUploadedRequirement(String kbName, String profileId, List<MultipartFile> files) {
        if (kbName == null || kbName.isBlank()) {
            throw new IllegalArgumentException("kb_name is required");
        }

        List<MultipartFile> uploadedFiles = validateUploadedFiles(files);
        PipelineProfileDto profile = resolveProfile(profileId);
        UploadedSourceExtraction extraction = uploadedFiles.size() == 1
                ? extractUploadedSource(uploadedFiles.get(0))
                : extractUploadedSources(uploadedFiles, kbName);
        List<String> fileNames = uploadedFiles.stream()
                .map(this::safeFileName)
                .toList();
        RawRequirementInputDto rawInput = new RawRequirementInputDto(
                "FILE",
                extraction.sourceText(),
                buildUploadedSourceLabel(fileNames),
                calculateTotalUploadSize(uploadedFiles),
                fileNames,
                uploadedFiles.size(),
                kbName
        );
        return buildNormalizedDraft(
                profile,
                rawInput,
                uploadedFiles.size() == 1,
                extraction.summaryOverride(),
                extraction.titleOverride(),
                extraction.missingInfo(),
                extraction.openQuestions(),
                extraction.inspection(),
                false
        );
    }

    public RequirementDraftDto normalizeImportedSource(
            String profileId,
            RawRequirementInputDto rawInput,
            boolean archiveInspected,
            String summaryOverride,
            String titleOverride,
            List<String> additionalMissingInfo,
            List<String> additionalOpenQuestions,
            ImportInspectionDto importInspection,
            boolean skipLegacyFileWarnings
    ) {
        if (profileId == null || profileId.isBlank()) {
            throw new IllegalArgumentException("profileId is required");
        }
        if (rawInput == null) {
            throw new IllegalArgumentException("rawInput is required");
        }

        PipelineProfileDto profile = pipelineProfileService.getProfile(profileId);
        return buildNormalizedDraft(
                profile,
                rawInput,
                archiveInspected,
                summaryOverride,
                titleOverride,
                additionalMissingInfo,
                additionalOpenQuestions,
                importInspection,
                skipLegacyFileWarnings
        );
    }

    private PipelineProfileDto resolveProfile(String profileId) {
        if (profileId != null && !profileId.isBlank()) {
            return pipelineProfileService.getProfile(profileId);
        }
        return pipelineProfileService.getActiveProfile(null, null, null);
    }

    private List<MultipartFile> validateUploadedFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }

        long totalSize = 0L;
        List<MultipartFile> uploadedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Uploaded file must not be empty");
            }
            uploadedFiles.add(file);
            totalSize += file.getSize();
        }

        if (totalSize > MAX_UPLOAD_BATCH_BYTES) {
            throw new IllegalArgumentException("Total upload size exceeds the 100 MB limit for one request");
        }

        return List.copyOf(uploadedFiles);
    }

    private RequirementDraftDto buildNormalizedDraft(
            PipelineProfileDto profile,
            RawRequirementInputDto rawInput,
            boolean archiveInspected,
            String summaryOverride,
            String titleOverride,
            List<String> additionalMissingInfo,
            List<String> additionalOpenQuestions,
            ImportInspectionDto importInspection,
            boolean skipLegacyFileWarnings
    ) {
        String sourceText = rawInput.text() == null ? "" : rawInput.text().trim();
        boolean zipArchive = isZipFile(rawInput.fileName());
        String summary = summaryOverride != null ? summaryOverride : summarizeInput(sourceText, rawInput.fileName());
        String title = titleOverride != null ? titleOverride : deriveTitle(sourceText, rawInput.fileName());
        String priority = detectPriority(sourceText);
        String category = detectCategory(sourceText, rawInput.fileName());
        List<String> acceptanceCriteria = deriveAcceptanceCriteria(sourceText, rawInput.fileName());
        List<String> missingInfo = new ArrayList<>(additionalMissingInfo);
        List<String> openQuestions = new ArrayList<>(additionalOpenQuestions);

        if (!containsPriorityHint(sourceText)) {
            missingInfo.add("Priority was not explicit in the source material");
        }
        if (zipArchive && !archiveInspected) {
            missingInfo.add("ZIP package metadata was received without archive extraction — upload the archive directly to inspect its contents");
        }
        if (!skipLegacyFileWarnings && rawInput.fileName() != null && isImageFile(rawInput.fileName())) {
            missingInfo.add("Image OCR is not implemented in V1 — review the attached image manually");
        }
        if (!skipLegacyFileWarnings && rawInput.fileName() != null && isPdfFile(rawInput.fileName())) {
            missingInfo.add("PDF text extraction is not implemented in V1 — review the attached PDF manually");
        }
        if (!skipLegacyFileWarnings && rawInput.fileName() != null && isSpreadsheetFile(rawInput.fileName())) {
            missingInfo.add("Spreadsheet parsing is limited in V1 — confirm which worksheet and columns are authoritative");
        }
        if (summary.length() < 40) {
            missingInfo.add("Source context is limited — clarify the expected business outcome");
        }
        openQuestions.add("Who is the primary stakeholder for this requirement?");
        openQuestions.add("What is the target delivery window or milestone?");
        if (zipArchive && !archiveInspected) {
            openQuestions.add("Which files inside the ZIP package should be treated as the source of truth?");
        }
        if (profile.usesOrchestrator()) {
            openQuestions.add("Should IBM i orchestration classify this as full-chain, enhancement, or fast-path?");
        }

        RequirementDraftDto draft = new RequirementDraftDto(
                title,
                priority,
                category,
                summary,
                "Business value inferred from source material; confirm stakeholder impact during review.",
                acceptanceCriteria,
                List.of("Imported source reflects the latest stakeholder intent"),
                List.of(),
                List.copyOf(deduplicateStrings(missingInfo)),
                List.copyOf(deduplicateStrings(openQuestions)),
                List.of("title", "priority", "category", "summary"),
                profile.id() + "-normalizer",
                timestampNow(),
                importInspection
        );

        persistImportAudit(null, rawInput, profile.id() + "-normalizer", "SUCCESS");
        log.info(
                "Requirement normalize audit: sourceType={}, fileName={}, profileId={}, user={}, outcome=SUCCESS",
                rawInput.sourceType(),
                rawInput.fileName(),
                profile.id(),
                "local-user"
        );
        return draft;
    }

    @Transactional
    public RequirementListItemDto createRequirement(CreateRequirementRequestDto body) {
        if (body == null) {
            throw new IllegalArgumentException("request body is required");
        }
        if (body.title() == null || body.title().isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (body.priority() == null || body.priority().isBlank()) {
            throw new IllegalArgumentException("priority is required");
        }
        if (body.category() == null || body.category().isBlank()) {
            throw new IllegalArgumentException("category is required");
        }

        int nextRequirementNumber = determineNextRequirementNumber();
        String requirementId = String.format(Locale.ROOT, "REQ-%04d", nextRequirementNumber);
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        int completenessScore = computeCompletenessScore(body);
        String source = mapSourceLabel(body.sourceAttachment());
        RequirementEntity created = requirementRepository.save(RequirementEntity.create(
                requirementId,
                body.title().trim(),
                normalizeText(body.summary()),
                normalizeText(body.businessJustification()),
                normalizeDisplayEnum(body.priority()),
                "Draft",
                normalizeDisplayCategory(body.category()),
                source,
                "Unassigned",
                completenessScore,
                now,
                now,
                "GLOBAL-SDLC-TOWER"
        ));
        saveAcceptanceCriteria(requirementId, body.acceptanceCriteria());
        saveAssumptions(requirementId, body.assumptions());
        saveConstraints(requirementId, body.constraints());
        chainLinkRepository.save(RequirementSdlcChainLinkEntity.create(
                requirementId,
                "requirement",
                requirementId,
                created.getTitle(),
                "/requirements/" + requirementId,
                1
        ));

        RawRequirementInputDto sourceAttachment = body.sourceAttachment();
        persistImportAudit(requirementId, sourceAttachment, "req-normalizer", "CREATED");
        log.info(
                "Requirement import audit: sourceType={}, fileName={}, skillId={}, user={}, timestamp={}, outcome=CREATED",
                sourceAttachment != null ? sourceAttachment.sourceType() : "TEXT",
                sourceAttachment != null ? sourceAttachment.fileName() : null,
                "req-normalizer",
                "local-user",
                now
        );
        return toListItem(created);
    }

    private void validateExists(String requirementId) {
        if (!requirementRepository.existsById(requirementId) && !DETAIL_BUILDERS.containsKey(requirementId)) {
            throw new ResourceNotFoundException("Requirement not found: " + requirementId);
        }
    }

    private String findRequirementIdForStory(String storyId) {
        return userStoryRepository.findById(storyId)
                .map(UserStoryEntity::getRequirementId)
                .orElseGet(() -> {
                    String requirementId = STORY_REQUIREMENT_IDS.get(storyId);
                    if (requirementId == null) {
                        throw new ResourceNotFoundException("Story not found: " + storyId);
                    }
                    return requirementId;
                });
    }

    private GenerationResultDto buildSpecGenerationResult(String requirementId, List<String> storyIds) {
        int storyCount = storyIds.size();
        String message = "Spec generation initiated for "
                + storyCount
                + (storyCount == 1 ? " story" : " stories")
                + " under "
                + requirementId;
        return buildGenerationResult(
                "EXEC-SPEC-" + requirementId,
                "user-story-to-spec",
                "RUNNING",
                requirementId,
                storyIds,
                "2026-04-16T11:35:00Z",
                30,
                message
        );
    }

    private GenerationResultDto buildGenerationResult(
            String executionId,
            String skillName,
            String status,
            String requirementId,
            List<String> inputStoryIds,
            String startedAt,
            int estimatedCompletionSeconds,
            String message
    ) {
        return new GenerationResultDto(
                executionId,
                skillName,
                status,
                requirementId,
                inputStoryIds,
                startedAt,
                estimatedCompletionSeconds,
                message
        );
    }

    private List<RequirementListItemDto> getAllRequirements() {
        List<RequirementEntity> storedRequirements = requirementRepository.findAll();
        if (!storedRequirements.isEmpty()) {
            return storedRequirements.stream()
                    .sorted(Comparator.comparing(RequirementEntity::getId))
                    .map(this::toListItem)
                    .toList();
        }
        return ALL_REQUIREMENTS;
    }

    private List<RequirementListItemDto> sortRequirements(
            List<RequirementListItemDto> items,
            String sortBy,
            String sortDirection
    ) {
        if (sortBy == null || sortBy.isBlank()) {
            return items;
        }
        Comparator<RequirementListItemDto> comparator = switch (sortBy) {
            case "priority" -> Comparator.comparing(item -> PRIORITY_ORDER.getOrDefault(item.priority(), Integer.MAX_VALUE));
            case "status" -> Comparator.comparing(item -> STATUS_ORDER.getOrDefault(item.status(), Integer.MAX_VALUE));
            case "title" -> Comparator.comparing(RequirementListItemDto::title, String.CASE_INSENSITIVE_ORDER);
            case "updatedAt", "recency" -> Comparator.comparing(RequirementListItemDto::updatedAt);
            default -> null;
        };
        if (comparator == null) {
            return items;
        }
        if (!"asc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }
        return items.stream().sorted(comparator).toList();
    }

    private StatusDistributionDto buildDistribution(List<RequirementListItemDto> requirements) {
        int draft = 0;
        int inReview = 0;
        int approved = 0;
        int inProgress = 0;
        int delivered = 0;
        int archived = 0;
        for (RequirementListItemDto requirement : requirements) {
            switch (requirement.status()) {
                case "Draft" -> draft++;
                case "In Review" -> inReview++;
                case "Approved" -> approved++;
                case "In Progress" -> inProgress++;
                case "Delivered" -> delivered++;
                case "Archived" -> archived++;
                default -> {
                }
            }
        }
        return new StatusDistributionDto(draft, inReview, approved, inProgress, delivered, archived);
    }

    private RequirementDetailDto buildRequirementDetailFromDatabase(String requirementId) {
        RequirementEntity requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found: " + requirementId));

        List<UserStoryEntity> stories = userStoryRepository.findByRequirementIdOrderByDisplayOrderAsc(requirementId);
        List<RequirementSpecEntity> specs = requirementSpecRepository.findByRequirementIdOrderByDisplayOrderAsc(requirementId);
        return new RequirementDetailDto(
                SectionResultDto.ok(new RequirementHeaderDto(
                        requirement.getId(),
                        requirement.getTitle(),
                        requirement.getPriority(),
                        requirement.getStatus(),
                        requirement.getCategory(),
                        requirement.getSource(),
                        requirement.getAssignee(),
                        requirement.getCompletenessScore(),
                        stories.size(),
                        specs.size(),
                        requirement.getCreatedAt().toString(),
                        requirement.getUpdatedAt().toString()
                )),
                SectionResultDto.ok(new RequirementDescriptionDto(
                        normalizeText(requirement.getSummary()),
                        normalizeText(requirement.getBusinessJustification()),
                        acceptanceCriterionRepository.findByRequirementIdOrderByDisplayOrderAsc(requirementId).stream()
                                .map(entity -> new AcceptanceCriterionDto(
                                        entity.getCriterionKey(),
                                        entity.getCriterionText(),
                                        entity.isMet()))
                                .toList(),
                        assumptionRepository.findByRequirementIdOrderByDisplayOrderAsc(requirementId).stream()
                                .map(RequirementAssumptionEntity::getAssumptionText)
                                .toList(),
                        constraintRepository.findByRequirementIdOrderByDisplayOrderAsc(requirementId).stream()
                                .map(RequirementConstraintEntity::getConstraintText)
                                .toList()
                )),
                SectionResultDto.ok(new LinkedStoriesSectionDto(
                        stories.stream()
                                .map(story -> new LinkedStoryDto(
                                        story.getId(),
                                        story.getTitle(),
                                        story.getStatus(),
                                        story.getSpecId(),
                                        story.getSpecStatus()))
                                .toList(),
                        stories.size()
                )),
                SectionResultDto.ok(new LinkedSpecsSectionDto(
                        specs.stream()
                                .map(spec -> new LinkedSpecDto(
                                        spec.getId(),
                                        spec.getTitle(),
                                        spec.getStatus(),
                                        spec.getVersion()))
                                .toList(),
                        specs.size()
                )),
                buildAnalysisSection(requirementId),
                SectionResultDto.ok(new SdlcChainDto(
                        chainLinkRepository.findByRequirementIdOrderByLinkOrderAsc(requirementId).stream()
                                .map(link -> new SdlcChainLinkDto(
                                        link.getArtifactType(),
                                        link.getArtifactId(),
                                        link.getArtifactTitle(),
                                        link.getRoutePath()))
                                .toList()
                ))
        );
    }

    private SectionResultDto<AiAnalysisDto> buildAnalysisSection(String requirementId) {
        return analysisRepository.findByRequirementId(requirementId)
                .map(analysis -> {
                    List<RequirementAiAnalysisElementEntity> elements = analysisElementRepository.findByAnalysisIdOrderByDisplayOrderAsc(analysis.getId());
                    return SectionResultDto.ok(new AiAnalysisDto(
                            analysis.getCompletenessScore(),
                            elements.stream()
                                    .filter(element -> "missing".equalsIgnoreCase(element.getElementType()))
                                    .map(RequirementAiAnalysisElementEntity::getElementText)
                                    .toList(),
                            elements.stream()
                                    .filter(element -> "similar".equalsIgnoreCase(element.getElementType()))
                                    .map(element -> new SimilarRequirementDto(
                                            element.getRelatedRequirementId(),
                                            element.getNumericValue() != null ? element.getNumericValue().intValue() : 0))
                                    .toList(),
                            analysis.getImpactAssessment(),
                            elements.stream()
                                    .filter(element -> "suggestion".equalsIgnoreCase(element.getElementType()))
                                    .map(RequirementAiAnalysisElementEntity::getElementText)
                                    .toList()
                    ));
                })
                .orElseGet(() -> new SectionResultDto<>(null, null));
    }

    private RequirementListItemDto toListItem(RequirementEntity entity) {
        int storyCount = userStoryRepository.findByRequirementIdOrderByDisplayOrderAsc(entity.getId()).size();
        int specCount = requirementSpecRepository.findByRequirementIdOrderByDisplayOrderAsc(entity.getId()).size();
        return new RequirementListItemDto(
                entity.getId(),
                entity.getTitle(),
                entity.getPriority(),
                entity.getStatus(),
                entity.getCategory(),
                storyCount,
                specCount,
                entity.getCompletenessScore(),
                entity.getAssignee(),
                entity.getCreatedAt().toString(),
                entity.getUpdatedAt().toString()
        );
    }

    private int determineNextRequirementNumber() {
        return requirementRepository.findAll().stream()
                .map(RequirementEntity::getId)
                .filter(Objects::nonNull)
                .filter(id -> id.startsWith("REQ-"))
                .map(id -> id.substring(4))
                .filter(value -> value.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(ALL_REQUIREMENTS.size()) + 1;
    }

    private void saveAcceptanceCriteria(String requirementId, List<String> acceptanceCriteria) {
        if (acceptanceCriteria == null) {
            return;
        }
        int order = 1;
        for (String criterion : acceptanceCriteria) {
            if (criterion == null || criterion.isBlank()) {
                continue;
            }
            acceptanceCriterionRepository.save(RequirementAcceptanceCriterionEntity.create(
                    requirementId,
                    String.format(Locale.ROOT, "AC-%03d", order),
                    criterion.trim(),
                    false,
                    order
            ));
            order++;
        }
    }

    private void saveAssumptions(String requirementId, List<String> assumptions) {
        if (assumptions == null) {
            return;
        }
        int order = 1;
        for (String assumption : assumptions) {
            if (assumption == null || assumption.isBlank()) {
                continue;
            }
            assumptionRepository.save(RequirementAssumptionEntity.create(requirementId, assumption.trim(), order));
            order++;
        }
    }

    private void saveConstraints(String requirementId, List<String> constraints) {
        if (constraints == null) {
            return;
        }
        int order = 1;
        for (String constraint : constraints) {
            if (constraint == null || constraint.isBlank()) {
                continue;
            }
            constraintRepository.save(RequirementConstraintEntity.create(requirementId, constraint.trim(), order));
            order++;
        }
    }

    private void persistImportAudit(
            String requirementId,
            RawRequirementInputDto rawInput,
            String skillId,
            String outcome
    ) {
        importAuditRepository.save(RequirementImportAuditEntity.create(
                requirementId,
                rawInput != null && rawInput.sourceType() != null ? rawInput.sourceType() : "TEXT",
                rawInput != null ? rawInput.fileName() : null,
                rawInput != null ? rawInput.fileSize() : null,
                skillId,
                "local-user",
                Instant.now().truncatedTo(ChronoUnit.SECONDS),
                outcome
        ));
    }

    private UploadedSourceExtraction extractUploadedSource(MultipartFile file) {
        String fileName = safeFileName(file);
        try {
            if (isZipFile(fileName)) {
                return extractZipArchive(file, fileName);
            }

            InspectedFileContent inspectedFile = inspectUploadedFile(fileName, file.getBytes(), false);
            ImportInspectionDto inspection = buildImportInspection(fileName, "FILE", List.of(inspectedFile.inspectionFile()));

            return new UploadedSourceExtraction(
                    inspectedFile.sourceText(),
                    buildInspectionSummary(fileName, inspection),
                    inspectedFile.titleOverride(),
                    inspectedFile.missingInfo(),
                    List.of(),
                    inspection
            );
        } catch (IOException exception) {
            throw new IllegalArgumentException("Failed to read uploaded file: " + fileName, exception);
        }
    }

    private UploadedSourceExtraction extractUploadedSources(List<MultipartFile> files, String kbName) {
        List<ImportInspectionFileDto> inspectedFiles = new ArrayList<>();
        List<String> missingInfo = new ArrayList<>();
        List<String> openQuestions = new ArrayList<>();
        StringBuilder extractedText = new StringBuilder();
        List<String> uploadedFileNames = new ArrayList<>();

        for (MultipartFile file : files) {
            UploadedSourceExtraction extraction = extractUploadedSource(file);
            String fileName = safeFileName(file);
            uploadedFileNames.add(fileName);
            inspectedFiles.addAll(extraction.inspection().files());
            missingInfo.addAll(extraction.missingInfo());
            openQuestions.addAll(extraction.openQuestions());
            appendBatchSourceText(extractedText, fileName, extraction.sourceText());
        }

        ImportInspectionDto inspection = buildImportInspection(buildUploadedSourceLabel(uploadedFileNames), "BATCH", inspectedFiles);
        if (inspection.parsedFiles() > 1 || inspection.manualReviewFiles() > 0) {
            openQuestions.add("Which uploaded file should be treated as the source of truth for this requirement?");
        }
        openQuestions.add("Should these uploaded files be normalized into one requirement or split into multiple requirements?");

        return new UploadedSourceExtraction(
                extractedText.isEmpty() ? "[Batch upload: " + buildUploadedSourceLabel(uploadedFileNames) + "]" : extractedText.toString(),
                buildBatchInspectionSummary(kbName, inspection, uploadedFileNames),
                "Imported requirement package from " + files.size() + (files.size() == 1 ? " file" : " files"),
                deduplicateStrings(missingInfo),
                deduplicateStrings(openQuestions),
                inspection
        );
    }

    private UploadedSourceExtraction extractZipArchive(MultipartFile file, String fileName) throws IOException {
        List<ImportInspectionFileDto> inspectedFiles = new ArrayList<>();
        List<String> parsedEntries = new ArrayList<>();
        List<String> manualReviewEntries = new ArrayList<>();
        List<String> limitSkippedEntries = new ArrayList<>();
        StringBuilder extractedText = new StringBuilder();
        int scannedEntries = 0;

        try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zipInputStream.closeEntry();
                    continue;
                }

                if (scannedEntries >= MAX_ARCHIVE_ENTRIES) {
                    String entryName = entry.getName();
                    limitSkippedEntries.add(entryName);
                    inspectedFiles.add(buildSkippedInspection(
                            entryName,
                            detectImportFileType(entryName),
                            "Skipped because archive scan limit of " + MAX_ARCHIVE_ENTRIES + " files was reached"
                    ));
                    zipInputStream.closeEntry();
                    continue;
                }

                scannedEntries++;
                String entryName = entry.getName();
                if (shouldSkipArchiveEntry(entryName)) {
                    inspectedFiles.add(buildSkippedInspection(
                            entryName,
                            detectImportFileType(entryName),
                            "Skipped archive metadata file"
                    ));
                    zipInputStream.closeEntry();
                    continue;
                }

                byte[] entryBytes = readLimitedBytes(zipInputStream, MAX_ARCHIVE_ENTRY_BYTES);
                InspectedFileContent inspectedEntry = inspectUploadedFile(entryName, entryBytes, true);
                inspectedFiles.add(inspectedEntry.inspectionFile());
                if ("PARSED".equals(inspectedEntry.inspectionFile().processingStatus())) {
                    parsedEntries.add(entryName);
                    appendArchiveText(extractedText, entryName, inspectedEntry.sourceText());
                } else if ("MANUAL_REVIEW".equals(inspectedEntry.inspectionFile().processingStatus())) {
                    manualReviewEntries.add(entryName);
                }
                zipInputStream.closeEntry();
            }
        }

        ImportInspectionDto inspection = buildImportInspection(fileName, "ZIP", inspectedFiles);
        List<String> missingInfo = new ArrayList<>();
        List<String> openQuestions = new ArrayList<>();

        if (inspection.totalFiles() == 0) {
            missingInfo.add("ZIP package is empty");
        }
        if (inspection.parsedFiles() == 0) {
            missingInfo.add("No automatically parseable files were found in the ZIP package");
        }
        if (!manualReviewEntries.isEmpty()) {
            missingInfo.add("Manual review is still required for: " + joinFileNames(manualReviewEntries));
        }
        if (!limitSkippedEntries.isEmpty()) {
            missingInfo.add("Archive scan was limited to the first " + MAX_ARCHIVE_ENTRIES + " files");
        }
        if (inspection.parsedFiles() > 1 || !manualReviewEntries.isEmpty()) {
            openQuestions.add("Which files inside the ZIP package should be treated as the source of truth?");
        }
        if (inspection.parsedFiles() > 1) {
            openQuestions.add("Should the extracted files be merged into one requirement, or should they be split into multiple requirements?");
        }

        String summary = buildInspectionSummary(fileName, inspection);
        String sourceText = extractedText.isEmpty()
                ? "[ZIP package upload: " + fileName + "]"
                : extractedText.toString();

        return new UploadedSourceExtraction(
                sourceText,
                summary,
                "Imported requirement package from " + fileName,
                missingInfo,
                openQuestions,
                inspection
        );
    }

    private void appendBatchSourceText(StringBuilder extractedText, String fileName, String fileText) {
        if (extractedText.length() >= MAX_ARCHIVE_TOTAL_CHARS) {
            return;
        }

        int remainingChars = MAX_ARCHIVE_TOTAL_CHARS - extractedText.length();
        String header = "Uploaded file: " + fileName + "\n";
        if (header.length() < remainingChars) {
            extractedText.append(header);
            remainingChars -= header.length();
        }

        String normalizedFileText = fileText.replace("\u0000", "").trim();
        if (normalizedFileText.length() > remainingChars) {
            extractedText.append(normalizedFileText, 0, Math.max(0, remainingChars));
            extractedText.append("\n...[truncated]\n");
            return;
        }

        extractedText.append(normalizedFileText).append("\n\n");
    }

    private void appendArchiveText(StringBuilder extractedText, String entryName, String entryText) {
        if (extractedText.length() >= MAX_ARCHIVE_TOTAL_CHARS) {
            return;
        }

        int remainingChars = MAX_ARCHIVE_TOTAL_CHARS - extractedText.length();
        String header = "Source file: " + entryName + "\n";
        if (header.length() < remainingChars) {
            extractedText.append(header);
            remainingChars -= header.length();
        }

        String normalizedEntryText = entryText.replace("\u0000", "").trim();
        if (normalizedEntryText.length() > remainingChars) {
            extractedText.append(normalizedEntryText, 0, Math.max(0, remainingChars));
            extractedText.append("\n...[truncated]\n");
            return;
        }

        extractedText.append(normalizedEntryText).append("\n\n");
    }

    private byte[] readLimitedBytes(ZipInputStream zipInputStream, int maxBytes) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int totalRead = 0;
        int bytesRead;
        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
            int writable = Math.min(bytesRead, maxBytes - totalRead);
            if (writable > 0) {
                outputStream.write(buffer, 0, writable);
                totalRead += writable;
            }
            if (totalRead >= maxBytes) {
                break;
            }
        }
        return outputStream.toByteArray();
    }

    private String buildInspectionSummary(String fileName, ImportInspectionDto inspection) {
        List<String> parsedEntries = inspection.files().stream()
                .filter(file -> "PARSED".equals(file.processingStatus()))
                .map(ImportInspectionFileDto::fileName)
                .toList();
        List<String> manualReviewEntries = inspection.files().stream()
                .filter(file -> "MANUAL_REVIEW".equals(file.processingStatus()))
                .map(ImportInspectionFileDto::fileName)
                .toList();
        List<String> parts = new ArrayList<>();
        if ("ZIP".equals(inspection.sourceKind())) {
            parts.add("Imported mixed-source requirement package from " + fileName + ".");
        } else {
            parts.add("Imported source material from " + fileName + ".");
        }
        if (!parsedEntries.isEmpty()) {
            parts.add("Parsed " + inspection.parsedFiles() + " file(s): " + joinFileNames(parsedEntries) + ".");
        }
        if (!manualReviewEntries.isEmpty()) {
            parts.add("Manual review still needed for: " + joinFileNames(manualReviewEntries) + ".");
        }
        if (inspection.skippedFiles() > 0) {
            parts.add("Skipped " + inspection.skippedFiles() + " file(s) because they were archive metadata or exceeded the scan limit.");
        }
        return String.join(" ", parts);
    }

    private String buildBatchInspectionSummary(String kbName, ImportInspectionDto inspection, List<String> uploadedFileNames) {
        List<String> parsedEntries = inspection.files().stream()
                .filter(file -> "PARSED".equals(file.processingStatus()))
                .map(ImportInspectionFileDto::fileName)
                .toList();
        List<String> manualReviewEntries = inspection.files().stream()
                .filter(file -> "MANUAL_REVIEW".equals(file.processingStatus()))
                .map(ImportInspectionFileDto::fileName)
                .toList();
        List<String> parts = new ArrayList<>();
        parts.add("Imported " + uploadedFileNames.size() + " source file(s) for knowledge base " + kbName + ": " + joinFileNames(uploadedFileNames) + ".");
        if (!parsedEntries.isEmpty()) {
            parts.add("Parsed " + inspection.parsedFiles() + " file(s): " + joinFileNames(parsedEntries) + ".");
        }
        if (!manualReviewEntries.isEmpty()) {
            parts.add("Manual review still needed for: " + joinFileNames(manualReviewEntries) + ".");
        }
        if (inspection.skippedFiles() > 0) {
            parts.add("Skipped " + inspection.skippedFiles() + " file(s) because they were archive metadata or exceeded scan limits.");
        }
        return String.join(" ", parts);
    }

    private ImportInspectionDto buildImportInspection(String sourceFileName, String sourceKind, List<ImportInspectionFileDto> files) {
        int parsedFiles = (int) files.stream().filter(file -> "PARSED".equals(file.processingStatus())).count();
        int manualReviewFiles = (int) files.stream().filter(file -> "MANUAL_REVIEW".equals(file.processingStatus())).count();
        int skippedFiles = (int) files.stream().filter(file -> "SKIPPED".equals(file.processingStatus())).count();
        return new ImportInspectionDto(
                sourceFileName,
                sourceKind,
                files.size(),
                parsedFiles,
                manualReviewFiles,
                skippedFiles,
                List.copyOf(files)
        );
    }

    private InspectedFileContent inspectUploadedFile(String fileName, byte[] fileBytes, boolean archiveEntry) {
        String normalizedFileName = archiveEntry ? fileName : shortFileName(fileName);
        String shortFileName = shortFileName(fileName);

        try {
            if (isTextExtractableFile(normalizedFileName)) {
                String extractedText = readUtf8Text(fileBytes);
                if (!extractedText.isBlank()) {
                    return parsedFile(
                            normalizedFileName,
                            detectImportFileType(normalizedFileName),
                            extractedText,
                            "Extracted text from source file",
                            null
                    );
                }
                return manualReviewFile(
                        normalizedFileName,
                        detectImportFileType(normalizedFileName),
                        "[File upload: " + shortFileName + "]",
                        "Text file was empty after extraction",
                        List.of("Uploaded text source is empty — confirm the correct file was attached"),
                        archiveEntry ? null : "Imported requirement from " + shortFileName
                );
            }

            if (isXlsxFile(normalizedFileName)) {
                String extractedText = extractXlsxText(fileBytes);
                if (!extractedText.isBlank()) {
                    return parsedFile(
                            normalizedFileName,
                            "SPREADSHEET",
                            extractedText,
                            "Extracted worksheet text from workbook",
                            "Imported requirement from " + normalizedFileName
                    );
                }
                return manualReviewFile(
                        normalizedFileName,
                        "SPREADSHEET",
                        "[Spreadsheet upload: " + shortFileName + "]",
                        "Workbook was recognized but no worksheet text could be extracted automatically",
                        List.of("Spreadsheet parsing found no usable worksheet text — confirm which sheet is authoritative"),
                        "Imported requirement from " + shortFileName
                );
            }

            if (isDocxFile(normalizedFileName)) {
                String extractedText = extractDocxText(fileBytes);
                if (!extractedText.isBlank()) {
                    return parsedFile(
                            normalizedFileName,
                            "DOCUMENT",
                            extractedText,
                            "Extracted document text from Word file",
                            null
                    );
                }
                return manualReviewFile(
                        normalizedFileName,
                        "DOCUMENT",
                        "[Document upload: " + shortFileName + "]",
                        "Word document was recognized but no text could be extracted automatically",
                        List.of("Word document text could not be extracted automatically — confirm the source file"),
                        archiveEntry ? null : "Imported requirement from " + shortFileName
                );
            }
        } catch (IOException exception) {
            return manualReviewFile(
                    normalizedFileName,
                    detectImportFileType(normalizedFileName),
                    "[File upload: " + shortFileName + "]",
                    "File format was recognized but could not be extracted automatically",
                    List.of("Automatic extraction failed for " + shortFileName + " — review the source manually"),
                    archiveEntry ? null : "Imported requirement from " + shortFileName
            );
        }

        if (isImageFile(normalizedFileName)) {
            return manualReviewFile(
                    normalizedFileName,
                    "IMAGE",
                    "[Image upload: " + shortFileName + "]",
                    "Image requires OCR or manual review",
                    List.of(),
                    archiveEntry ? null : "Imported requirement from " + shortFileName
            );
        }

        if (isPdfFile(normalizedFileName)) {
            return manualReviewFile(
                    normalizedFileName,
                    "PDF",
                    "[PDF upload: " + shortFileName + "]",
                    "PDF requires text extraction or manual review",
                    List.of(),
                    archiveEntry ? null : "Imported requirement from " + shortFileName
            );
        }

        if (isXlsFile(normalizedFileName)) {
            return manualReviewFile(
                    normalizedFileName,
                    "SPREADSHEET",
                    "[Spreadsheet upload: " + shortFileName + "]",
                    "Legacy Excel (.xls) requires manual review in V1",
                    List.of("Legacy Excel (.xls) is not parsed automatically in V1 — confirm the authoritative worksheet"),
                    archiveEntry ? null : "Imported requirement from " + shortFileName
            );
        }

        if (isMsgFile(normalizedFileName)) {
            return manualReviewFile(
                    normalizedFileName,
                    "EMAIL",
                    "[Email upload: " + shortFileName + "]",
                    "Outlook .msg requires manual review in V1",
                    List.of("Outlook .msg files are not parsed automatically in V1 — summarize the relevant email thread manually"),
                    archiveEntry ? null : "Imported requirement from " + shortFileName
            );
        }

        if (isZipFile(normalizedFileName)) {
            return manualReviewFile(
                    normalizedFileName,
                    "ARCHIVE",
                    "[ZIP package upload: " + shortFileName + "]",
                    "Nested ZIP archive requires separate review",
                    List.of("Nested ZIP archives are not unpacked recursively in V1"),
                    archiveEntry ? null : "Imported requirement from " + shortFileName
            );
        }

        return manualReviewFile(
                normalizedFileName,
                detectImportFileType(normalizedFileName),
                "[Binary upload: " + shortFileName + "]",
                "Uploaded file format is not parsed automatically in V1",
                List.of("Uploaded file format is not parsed automatically in V1 — summarize the key source material manually"),
                archiveEntry ? null : "Imported requirement from " + shortFileName
        );
    }

    private InspectedFileContent parsedFile(
            String fileName,
            String fileType,
            String extractedText,
            String summary,
            String titleOverride
    ) {
        String normalizedText = truncateText(extractedText, MAX_ARCHIVE_TOTAL_CHARS);
        return new InspectedFileContent(
                normalizedText,
                List.of(),
                titleOverride,
                new ImportInspectionFileDto(
                        fileName,
                        fileType,
                        "PARSED",
                        summary,
                        normalizedText.length(),
                        buildPreview(normalizedText)
                )
        );
    }

    private InspectedFileContent manualReviewFile(
            String fileName,
            String fileType,
            String sourceText,
            String summary,
            List<String> missingInfo,
            String titleOverride
    ) {
        return new InspectedFileContent(
                sourceText,
                missingInfo,
                titleOverride,
                new ImportInspectionFileDto(
                        fileName,
                        fileType,
                        "MANUAL_REVIEW",
                        summary,
                        null,
                        null
                )
        );
    }

    private ImportInspectionFileDto buildSkippedInspection(String fileName, String fileType, String summary) {
        return new ImportInspectionFileDto(
                fileName,
                fileType,
                "SKIPPED",
                summary,
                null,
                null
        );
    }

    private boolean shouldSkipArchiveEntry(String entryName) {
        String shortName = shortFileName(entryName);
        return entryName.startsWith("__MACOSX/")
                || ".DS_Store".equals(shortName)
                || shortName.startsWith(".");
    }

    private String buildPreview(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String compact = text.replace('\n', ' ').replaceAll("\\s+", " ").trim();
        if (compact.length() <= MAX_PREVIEW_CHARS) {
            return compact;
        }
        return compact.substring(0, MAX_PREVIEW_CHARS).trim() + "...";
    }

    private String truncateText(String text, int maxChars) {
        if (text == null) {
            return "";
        }
        String normalized = text.replace("\u0000", "").trim();
        if (normalized.length() <= maxChars) {
            return normalized;
        }
        return normalized.substring(0, maxChars).trim() + "\n...[truncated]";
    }

    private String extractDocxText(byte[] fileBytes) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(fileBytes))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory() && "word/document.xml".equals(entry.getName())) {
                    byte[] xmlBytes = readLimitedBytes(zipInputStream, MAX_ARCHIVE_ENTRY_BYTES);
                    zipInputStream.closeEntry();
                    return truncateText(extractWordParagraphText(xmlBytes), MAX_ARCHIVE_TOTAL_CHARS);
                }
                zipInputStream.closeEntry();
            }
        }
        return "";
    }

    private String extractXlsxText(byte[] fileBytes) throws IOException {
        List<String> sharedStrings = List.of();
        Map<String, byte[]> worksheets = new HashMap<>();

        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(fileBytes))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zipInputStream.closeEntry();
                    continue;
                }

                String entryName = entry.getName();
                if ("xl/sharedStrings.xml".equals(entryName)) {
                    sharedStrings = parseSharedStrings(readLimitedBytes(zipInputStream, MAX_ARCHIVE_ENTRY_BYTES));
                } else if (entryName.startsWith("xl/worksheets/") && entryName.endsWith(".xml")) {
                    worksheets.put(entryName, readLimitedBytes(zipInputStream, MAX_ARCHIVE_ENTRY_BYTES));
                }
                zipInputStream.closeEntry();
            }
        }

        if (worksheets.isEmpty()) {
            return "";
        }

        StringBuilder extracted = new StringBuilder();
        List<String> resolvedSharedStrings = sharedStrings;
        worksheets.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> appendWorksheetText(extracted, shortFileName(entry.getKey()), entry.getValue(), resolvedSharedStrings));
        return truncateText(extracted.toString(), MAX_ARCHIVE_TOTAL_CHARS);
    }

    private List<String> parseSharedStrings(byte[] xmlBytes) throws IOException {
        Document document = parseXml(xmlBytes);
        NodeList sharedStringNodes = document.getElementsByTagNameNS("*", "si");
        List<String> sharedStrings = new ArrayList<>();
        for (int index = 0; index < sharedStringNodes.getLength(); index += 1) {
            String value = sharedStringNodes.item(index).getTextContent();
            if (value != null) {
                sharedStrings.add(value.replace('\u0000', ' ').trim());
            }
        }
        return sharedStrings;
    }

    private void appendWorksheetText(StringBuilder extracted, String sheetName, byte[] xmlBytes, List<String> sharedStrings) {
        try {
            Document document = parseXml(xmlBytes);
            NodeList rowNodes = document.getElementsByTagNameNS("*", "row");
            int rowsAdded = 0;
            for (int rowIndex = 0; rowIndex < rowNodes.getLength() && rowsAdded < MAX_SPREADSHEET_ROWS; rowIndex += 1) {
                Element rowElement = (Element) rowNodes.item(rowIndex);
                NodeList cellNodes = rowElement.getElementsByTagNameNS("*", "c");
                List<String> cells = new ArrayList<>();
                for (int cellIndex = 0; cellIndex < cellNodes.getLength(); cellIndex += 1) {
                    Element cellElement = (Element) cellNodes.item(cellIndex);
                    String value = resolveSpreadsheetCellValue(cellElement, sharedStrings);
                    if (!value.isBlank()) {
                        cells.add(value);
                    }
                }
                if (!cells.isEmpty()) {
                    if (rowsAdded == 0) {
                        extracted.append("Worksheet ").append(sheetName).append(":\n");
                    }
                    extracted.append(String.join(" | ", cells)).append('\n');
                    rowsAdded += 1;
                }
            }
            if (rowsAdded > 0) {
                extracted.append('\n');
            }
        } catch (IOException exception) {
            log.debug("Failed to parse worksheet {}: {}", sheetName, exception.getMessage());
        }
    }

    private String resolveSpreadsheetCellValue(Element cellElement, List<String> sharedStrings) {
        String cellType = cellElement.getAttribute("t");
        if ("inlineStr".equals(cellType)) {
            return normalizeText(cellElement.getTextContent());
        }

        String rawValue = firstDescendantText(cellElement, "v");
        if (rawValue.isBlank()) {
            return "";
        }

        if ("s".equals(cellType)) {
            try {
                int sharedStringIndex = Integer.parseInt(rawValue);
                if (sharedStringIndex >= 0 && sharedStringIndex < sharedStrings.size()) {
                    return normalizeText(sharedStrings.get(sharedStringIndex));
                }
            } catch (NumberFormatException ignored) {
                return normalizeText(rawValue);
            }
        }

        if ("b".equals(cellType)) {
            return "1".equals(rawValue) ? "TRUE" : "FALSE";
        }

        return normalizeText(rawValue);
    }

    private String extractWordParagraphText(byte[] xmlBytes) throws IOException {
        Document document = parseXml(xmlBytes);
        NodeList paragraphNodes = document.getElementsByTagNameNS("*", "p");
        List<String> paragraphs = new ArrayList<>();
        for (int index = 0; index < paragraphNodes.getLength(); index += 1) {
            String text = normalizeText(paragraphNodes.item(index).getTextContent());
            if (!text.isBlank()) {
                paragraphs.add(text);
            }
        }
        if (!paragraphs.isEmpty()) {
            return String.join("\n", paragraphs);
        }
        return normalizeText(document.getDocumentElement().getTextContent());
    }

    private String firstDescendantText(Element element, String localName) {
        NodeList nodes = element.getElementsByTagNameNS("*", localName);
        if (nodes.getLength() == 0) {
            return "";
        }
        return normalizeText(nodes.item(0).getTextContent());
    }

    private Document parseXml(byte[] xmlBytes) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlBytes)) {
                return factory.newDocumentBuilder().parse(inputStream);
            }
        } catch (ParserConfigurationException | SAXException exception) {
            throw new IOException("Failed to parse XML content", exception);
        }
    }

    private String joinFileNames(List<String> fileNames) {
        return fileNames.stream()
                .limit(5)
                .map(this::shortFileName)
                .reduce((left, right) -> left + ", " + right)
                .orElse("none");
    }

    private String buildUploadedSourceLabel(List<String> fileNames) {
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

    private long calculateTotalUploadSize(List<MultipartFile> files) {
        return files.stream()
                .mapToLong(MultipartFile::getSize)
                .sum();
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

    private String readUtf8Text(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8)
                .replace("\u0000", "")
                .trim();
    }

    private List<String> deduplicateStrings(List<String> values) {
        return new ArrayList<>(new LinkedHashSet<>(values));
    }

    private String summarizeInput(String sourceText, String fileName) {
        if (isZipFile(fileName)) {
            return "Imported mixed-source requirement package from " + fileName;
        }
        if (fileName != null && sourceText.startsWith("[")) {
            return "Imported source material from " + fileName;
        }
        if (!sourceText.isBlank()) {
            String compact = sourceText.replace('\n', ' ').replaceAll("\\s+", " ").trim();
            return compact.length() > 220 ? compact.substring(0, 220).trim() + "..." : compact;
        }
        return fileName != null ? "Imported source material from " + fileName : "Imported requirement source";
    }

    private String deriveTitle(String sourceText, String fileName) {
        if (isZipFile(fileName)) {
            return "Imported requirement package from " + fileName;
        }
        if (fileName != null && sourceText.startsWith("[")) {
            return "Imported requirement from " + fileName;
        }
        if (!sourceText.isBlank()) {
            String firstLine = sourceText.lines()
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .findFirst()
                    .orElse(sourceText.trim());
            if (firstLine.length() > 80) {
                return firstLine.substring(0, 80).trim();
            }
            return firstLine;
        }
        return fileName != null ? "Imported requirement from " + fileName : "Untitled Requirement";
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String detectPriority(String sourceText) {
        String lower = sourceText.toLowerCase(Locale.ROOT);
        if (lower.contains("critical")) {
            return "Critical";
        }
        if (lower.contains("high")) {
            return "High";
        }
        if (lower.contains("low")) {
            return "Low";
        }
        return "Medium";
    }

    private boolean containsPriorityHint(String sourceText) {
        String lower = sourceText.toLowerCase(Locale.ROOT);
        return lower.contains("critical")
                || lower.contains("high")
                || lower.contains("medium")
                || lower.contains("low");
    }

    private String detectCategory(String sourceText, String fileName) {
        String lower = (sourceText + " " + (fileName == null ? "" : fileName)).toLowerCase(Locale.ROOT);
        if (lower.contains("performance")
                || lower.contains("latency")
                || lower.contains("uptime")
                || lower.contains("availability")) {
            return "Non-Functional";
        }
        if (lower.contains("database")
                || lower.contains("architecture")
                || lower.contains("api")
                || lower.contains("migration")) {
            return "Technical";
        }
        if (lower.contains("compliance")
                || lower.contains("audit")
                || lower.contains("finance")
                || lower.contains("business")) {
            return "Business";
        }
        return "Functional";
    }

    private List<String> deriveAcceptanceCriteria(String sourceText, String fileName) {
        List<String> criteria = sourceText.lines()
                .map(String::trim)
                .filter(line -> line.startsWith("-")
                        || line.startsWith("*")
                        || line.matches("^\\d+\\..*"))
                .map(line -> line.replaceFirst("^[-*]\\s*", "").replaceFirst("^\\d+\\.\\s*", "").trim())
                .filter(line -> !line.isBlank())
                .limit(5)
                .toList();
        if (!criteria.isEmpty()) {
            return criteria;
        }
        if (isZipFile(fileName)) {
            return List.of(
                    "Archive contents and source-of-truth documents should be identified before requirement confirmation"
            );
        }
        return List.of("Requirement behavior should be reviewed and confirmed with stakeholders");
    }

    private int computeCompletenessScore(CreateRequirementRequestDto body) {
        int score = 0;
        if (body.title() != null && !body.title().isBlank()) score += 20;
        if (body.summary() != null && !body.summary().isBlank()) score += 25;
        if (body.businessJustification() != null && !body.businessJustification().isBlank()) score += 20;
        if (body.acceptanceCriteria() != null && !body.acceptanceCriteria().isEmpty()) score += 25;
        if (body.constraints() != null && !body.constraints().isEmpty()) score += 10;
        return Math.min(score, 100);
    }

    private String mapSourceLabel(RawRequirementInputDto sourceAttachment) {
        if (sourceAttachment == null || sourceAttachment.sourceType() == null) {
            return "Manual";
        }
        return switch (sourceAttachment.sourceType().toUpperCase(Locale.ROOT)) {
            case "TEXT" -> "Manual";
            case "FILE", "EMAIL", "MEETING" -> "Imported";
            default -> "Imported";
        };
    }

    private String normalizeDisplayEnum(String value) {
        return capitalizeToken(value);
    }

    private String normalizeDisplayCategory(String value) {
        if (value == null) {
            return "Functional";
        }
        return switch (value.toUpperCase(Locale.ROOT)) {
            case "NON-FUNCTIONAL", "NON_FUNCTIONAL" -> "Non-Functional";
            default -> capitalizeToken(value);
        };
    }

    private String capitalizeToken(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.trim().replace('_', ' ').replace('-', ' ').toLowerCase(Locale.ROOT);
        return normalized.substring(0, 1).toUpperCase(Locale.ROOT) + normalized.substring(1);
    }

    private boolean isImageFile(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".webp");
    }

    private boolean isPdfFile(String fileName) {
        return fileName != null && fileName.toLowerCase(Locale.ROOT).endsWith(".pdf");
    }

    private boolean isSpreadsheetFile(String fileName) {
        return isXlsxFile(fileName) || isXlsFile(fileName);
    }

    private boolean isXlsxFile(String fileName) {
        if (fileName == null) {
            return false;
        }
        return fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx");
    }

    private boolean isXlsFile(String fileName) {
        if (fileName == null) {
            return false;
        }
        return fileName.toLowerCase(Locale.ROOT).endsWith(".xls");
    }

    private boolean isZipFile(String fileName) {
        return fileName != null && fileName.toLowerCase(Locale.ROOT).endsWith(".zip");
    }

    private boolean isDocxFile(String fileName) {
        return fileName != null && fileName.toLowerCase(Locale.ROOT).endsWith(".docx");
    }

    private boolean isMsgFile(String fileName) {
        return fileName != null && fileName.toLowerCase(Locale.ROOT).endsWith(".msg");
    }

    private boolean isTextExtractableFile(String fileName) {
        if (fileName == null) {
            return false;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.endsWith(".txt")
                || lower.endsWith(".csv")
                || lower.endsWith(".md")
                || lower.endsWith(".html")
                || lower.endsWith(".htm")
                || lower.endsWith(".markdown")
                || lower.endsWith(".json")
                || lower.endsWith(".yaml")
                || lower.endsWith(".yml")
                || lower.endsWith(".xml")
                || lower.endsWith(".eml")
                || lower.endsWith(".vtt");
    }

    private String detectImportFileType(String fileName) {
        if (fileName == null) {
            return "BINARY";
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".markdown") || lower.endsWith(".html") || lower.endsWith(".htm")) {
            return "TEXT";
        }
        if (lower.endsWith(".csv") || lower.endsWith(".xlsx") || lower.endsWith(".xls")) {
            return "SPREADSHEET";
        }
        if (lower.endsWith(".docx") || lower.endsWith(".doc")) {
            return "DOCUMENT";
        }
        if (lower.endsWith(".eml") || lower.endsWith(".msg")) {
            return "EMAIL";
        }
        if (lower.endsWith(".json") || lower.endsWith(".yaml") || lower.endsWith(".yml") || lower.endsWith(".xml")) {
            return "STRUCTURED_DATA";
        }
        if (lower.endsWith(".vtt")) {
            return "TRANSCRIPT";
        }
        if (isImageFile(lower)) {
            return "IMAGE";
        }
        if (isPdfFile(lower)) {
            return "PDF";
        }
        if (isZipFile(lower)) {
            return "ARCHIVE";
        }
        return "BINARY";
    }

    private String normalizeExecutionToken(String skillId) {
        return skillId.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "-");
    }

    private String timestampNow() {
        return Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
    }

    private <T> SectionResultDto<T> loadSection(String name, Supplier<T> supplier) {
        try {
            return SectionResultDto.ok(supplier.get());
        } catch (Exception e) {
            log.error("Failed to load requirement section [{}]: {}", name, e.getMessage(), e);
            return SectionResultDto.fail("Failed to load " + name);
        }
    }

    private record UploadedSourceExtraction(
            String sourceText,
            String summaryOverride,
            String titleOverride,
            List<String> missingInfo,
            List<String> openQuestions,
            ImportInspectionDto inspection
    ) {}

    private record InspectedFileContent(
            String sourceText,
            List<String> missingInfo,
            String titleOverride,
            ImportInspectionFileDto inspectionFile
    ) {}

    // ── Per-requirement detail builders ──

    private static final Map<String, Supplier<RequirementDetailDto>> DETAIL_BUILDERS = Map.of(
            "REQ-0001", RequirementService::buildDetail0001,
            "REQ-0002", RequirementService::buildDetail0002,
            "REQ-0003", RequirementService::buildDetail0003,
            "REQ-0004", RequirementService::buildDetail0004,
            "REQ-0005", RequirementService::buildDetail0005,
            "REQ-0006", RequirementService::buildDetail0006,
            "REQ-0007", RequirementService::buildDetail0007,
            "REQ-0008", RequirementService::buildDetail0008,
            "REQ-0009", RequirementService::buildDetail0009,
            "REQ-0010", RequirementService::buildDetail0010
    );

    private static Map<String, String> buildStoryRequirementIds() {
        Map<String, String> storyRequirementIds = new HashMap<>();
        REQUIREMENT_STORY_IDS.forEach((requirementId, storyIds) ->
                storyIds.forEach(storyId -> storyRequirementIds.put(storyId, requirementId)));
        return Map.copyOf(storyRequirementIds);
    }

    // ── REQ-0001: Critical, Approved, Functional ──

    private static RequirementDetailDto buildDetail0001() {
        return new RequirementDetailDto(
                SectionResultDto.ok(new RequirementHeaderDto(
                        "REQ-0001", "User Authentication and SSO Integration", "Critical", "Approved",
                        "Functional", "Manual", "Sarah Chen", 85, 4, 2, "2026-04-10T09:00:00Z", "2026-04-16T10:30:00Z")),
                SectionResultDto.ok(new RequirementDescriptionDto(
                        "Implement enterprise SSO authentication using SAML 2.0 and OAuth 2.0 protocols to enable single sign-on across all SDLC Control Tower modules. Must support Active Directory, Okta, and Azure AD identity providers.",
                        "Enterprise customers require SSO integration for security compliance (SOC 2, ISO 27001). Without SSO, adoption is blocked for 60% of target enterprise accounts. Current username/password auth does not meet enterprise security policies.",
                        List.of(
                                new AcceptanceCriterionDto("AC-001", "Users can authenticate via SAML 2.0 SSO with their corporate IdP", true),
                                new AcceptanceCriterionDto("AC-002", "OAuth 2.0 authorization code flow works with Okta and Azure AD", true),
                                new AcceptanceCriterionDto("AC-003", "Session tokens expire after 8 hours of inactivity", false),
                                new AcceptanceCriterionDto("AC-004", "Failed login attempts are rate-limited to 5 per minute per IP", true),
                                new AcceptanceCriterionDto("AC-005", "Admin can configure IdP settings through Platform Center", false)),
                        List.of("Customer IdP supports SAML 2.0 or OAuth 2.0",
                                "Network connectivity to external IdP endpoints is available",
                                "Certificate management for SAML signing is handled externally"),
                        List.of("Must not introduce latency > 500ms to login flow",
                                "Must work behind corporate proxies",
                                "Must support multi-tenant isolation"))),
                SectionResultDto.ok(new LinkedStoriesSectionDto(List.of(
                        new LinkedStoryDto("US-001", "As an enterprise user, I can log in via SSO", "Done", "SPEC-001", "Approved"),
                        new LinkedStoryDto("US-002", "As an admin, I can configure SAML IdP settings", "In Progress", "SPEC-002", "Draft"),
                        new LinkedStoryDto("US-003", "As a user, I am automatically logged out after inactivity", "Draft", null, null),
                        new LinkedStoryDto("US-004", "As a security admin, I can view failed login attempts", "Ready", null, null)), 4)),
                SectionResultDto.ok(new LinkedSpecsSectionDto(List.of(
                        new LinkedSpecDto("SPEC-001", "SSO Authentication Flow Specification", "Approved", "v1.2"),
                        new LinkedSpecDto("SPEC-002", "IdP Configuration Management Spec", "Draft", "v0.1")), 2)),
                SectionResultDto.ok(new AiAnalysisDto(85,
                        List.of("Session timeout behavior not fully specified", "No error recovery flow for IdP outage"),
                        List.of(new SimilarRequirementDto("REQ-0002", 72), new SimilarRequirementDto("REQ-0005", 45)),
                        "High impact — blocks enterprise customer onboarding. Affects 8 downstream user stories across 3 modules. Critical path for Q2 release.",
                        List.of("Add specification for IdP failover behavior",
                                "Define session token refresh strategy",
                                "Consider adding MFA as optional enhancement",
                                "Align with REQ-0002 (RBAC) for permission model"))),
                SectionResultDto.ok(new SdlcChainDto(List.of(
                        new SdlcChainLinkDto("requirement", "REQ-0001", "User Authentication and SSO Integration", "/requirements/REQ-0001"),
                        new SdlcChainLinkDto("user-story", "US-001", "Enterprise SSO Login", "/requirements"),
                        new SdlcChainLinkDto("spec", "SPEC-001", "SSO Authentication Flow Specification", "/requirements"),
                        new SdlcChainLinkDto("design", "DES-001", "Auth Module Design", "/design"),
                        new SdlcChainLinkDto("code", "MR-1050", "feat: SAML 2.0 SSO integration", "/code"),
                        new SdlcChainLinkDto("test", "TS-042", "SSO Integration Test Suite", "/testing"))))
        );
    }

    // ── REQ-0002: High, In Progress, Functional ──

    private static RequirementDetailDto buildDetail0002() {
        return new RequirementDetailDto(
                SectionResultDto.ok(new RequirementHeaderDto(
                        "REQ-0002", "Role-Based Access Control", "High", "In Progress",
                        "Functional", "Manual", "Alex Kim", 60, 3, 1, "2026-04-11T10:00:00Z", "2026-04-16T09:15:00Z")),
                SectionResultDto.ok(new RequirementDescriptionDto(
                        "Implement role-based access control (RBAC) with predefined roles (Admin, Manager, Developer, Viewer) and custom role support. Permissions must be enforceable at both API and UI levels.",
                        "Enterprise customers require granular access control for compliance. Current system has no permission model, making it unsuitable for teams larger than 10 users.",
                        List.of(
                                new AcceptanceCriterionDto("AC-010", "Four predefined roles are available: Admin, Manager, Developer, Viewer", true),
                                new AcceptanceCriterionDto("AC-011", "Custom roles can be created with specific permission sets", false),
                                new AcceptanceCriterionDto("AC-012", "API endpoints enforce role-based permissions", true),
                                new AcceptanceCriterionDto("AC-013", "UI elements are hidden/disabled based on user role", false)),
                        List.of("Authentication (REQ-0001) is implemented before RBAC", "Permission checks have < 10ms overhead per request"),
                        List.of("Must integrate with existing SSO session tokens", "Permission cache must be invalidated within 30 seconds of role change"))),
                SectionResultDto.ok(new LinkedStoriesSectionDto(List.of(
                        new LinkedStoryDto("US-010", "As an admin, I can assign roles to team members", "In Progress", "SPEC-010", "Approved"),
                        new LinkedStoryDto("US-011", "As a developer, I see only permitted actions in the UI", "Draft", null, null),
                        new LinkedStoryDto("US-012", "As an admin, I can create custom roles", "Draft", null, null)), 3)),
                SectionResultDto.ok(new LinkedSpecsSectionDto(List.of(
                        new LinkedSpecDto("SPEC-010", "RBAC Permission Model Specification", "Approved", "v1.0")), 1)),
                SectionResultDto.ok(new AiAnalysisDto(60,
                        List.of("Custom role creation workflow not detailed", "Permission inheritance model not specified", "Audit logging for role changes not addressed"),
                        List.of(new SimilarRequirementDto("REQ-0001", 72), new SimilarRequirementDto("REQ-0005", 55)),
                        "Medium-high impact — required for multi-tenant operation. Depends on REQ-0001 (SSO). Blocks REQ-0005 (Audit Trail).",
                        List.of("Define permission inheritance hierarchy", "Add audit logging for all role/permission changes", "Consider attribute-based access control (ABAC) for future extensibility"))),
                SectionResultDto.ok(new SdlcChainDto(List.of(
                        new SdlcChainLinkDto("requirement", "REQ-0002", "Role-Based Access Control", "/requirements/REQ-0002"),
                        new SdlcChainLinkDto("user-story", "US-010", "Role Assignment", "/requirements"),
                        new SdlcChainLinkDto("spec", "SPEC-010", "RBAC Permission Model", "/requirements"))))
        );
    }

    // ── REQ-0003: High, Draft, Non-Functional (no stories/specs, no analysis) ──

    private static RequirementDetailDto buildDetail0003() {
        return new RequirementDetailDto(
                SectionResultDto.ok(new RequirementHeaderDto(
                        "REQ-0003", "API Response Time Under 200ms", "High", "Draft",
                        "Non-Functional", "AI-Generated", "Unassigned", 15, 0, 0, "2026-04-15T16:00:00Z", "2026-04-15T16:00:00Z")),
                SectionResultDto.ok(new RequirementDescriptionDto(
                        "All REST API endpoints must respond within 200ms at p95 latency under normal load (100 concurrent users). Batch operations may take up to 2 seconds.",
                        "User experience research shows that response times over 200ms are perceived as slow. Competitor benchmarks show sub-150ms p95 latency. Enterprise SLA requires documented performance targets.",
                        List.of(
                                new AcceptanceCriterionDto("AC-020", "p95 latency for single-resource GET < 200ms", false),
                                new AcceptanceCriterionDto("AC-021", "p95 latency for list endpoints < 500ms with pagination", false),
                                new AcceptanceCriterionDto("AC-022", "Performance regression tests run on every PR", false)),
                        List.of("Database queries are optimized with proper indexing", "Response caching is available for read-heavy endpoints"),
                        List.of("Measurement must include serialization and middleware overhead", "Must account for multi-tenant query isolation overhead"))),
                SectionResultDto.ok(new LinkedStoriesSectionDto(List.of(), 0)),
                SectionResultDto.ok(new LinkedSpecsSectionDto(List.of(), 0)),
                new SectionResultDto<>(null, null),
                SectionResultDto.ok(new SdlcChainDto(List.of(
                        new SdlcChainLinkDto("requirement", "REQ-0003", "API Response Time Under 200ms", "/requirements/REQ-0003"))))
        );
    }

    // ── REQ-0004: Medium, In Review, Technical ──

    private static RequirementDetailDto buildDetail0004() {
        return new RequirementDetailDto(
                SectionResultDto.ok(new RequirementHeaderDto("REQ-0004", "Database Migration to Oracle 23ai", "Medium", "In Review", "Technical", "Manual", "David Park", 45, 2, 1, "2026-04-12T09:00:00Z", "2026-04-15T14:20:00Z")),
                SectionResultDto.ok(new RequirementDescriptionDto(
                        "Migrate primary data store from PostgreSQL 15 to Oracle 23ai to leverage vector search, JSON Relational Duality, and enterprise-grade partitioning for the Control Tower platform.",
                        "Enterprise customers on Oracle infrastructure require native Oracle support. Oracle 23ai vector search eliminates the need for a separate vector DB, reducing operational complexity.",
                        List.of(new AcceptanceCriterionDto("AC-030", "All Flyway migrations execute successfully against Oracle 23ai", true),
                                new AcceptanceCriterionDto("AC-031", "Query performance parity with PostgreSQL baseline (±10%)", false),
                                new AcceptanceCriterionDto("AC-032", "Zero data loss during migration", false)),
                        List.of("Oracle 23ai license is procured", "DBA team available for schema review"),
                        List.of("Must maintain PostgreSQL compatibility for H2 local dev", "Migration window is 4 hours maximum"))),
                SectionResultDto.ok(new LinkedStoriesSectionDto(List.of(
                        new LinkedStoryDto("US-030", "As a DBA, I can run Flyway migrations against Oracle 23ai", "In Progress", "SPEC-030", "Review"),
                        new LinkedStoryDto("US-031", "As a developer, I can use H2 locally while prod uses Oracle", "Draft", null, null)), 2)),
                SectionResultDto.ok(new LinkedSpecsSectionDto(List.of(
                        new LinkedSpecDto("SPEC-030", "Oracle 23ai Migration Specification", "Review", "v0.8")), 1)),
                SectionResultDto.ok(new AiAnalysisDto(45,
                        List.of("Rollback strategy not defined", "Performance benchmark criteria incomplete"),
                        List.of(new SimilarRequirementDto("REQ-0007", 35)),
                        "Medium — affects all data access layers. Must coordinate with REQ-0007 (SLA) for performance targets.",
                        List.of("Define rollback procedure for failed migration", "Add performance benchmark suite requirement", "Consider blue-green migration approach"))),
                SectionResultDto.ok(new SdlcChainDto(List.of(
                        new SdlcChainLinkDto("requirement", "REQ-0004", "Database Migration to Oracle 23ai", "/requirements/REQ-0004"),
                        new SdlcChainLinkDto("spec", "SPEC-030", "Oracle 23ai Migration Specification", "/requirements"))))
        );
    }

    // ── REQ-0005: Critical, In Progress, Business ──

    private static RequirementDetailDto buildDetail0005() {
        return new RequirementDetailDto(
                SectionResultDto.ok(new RequirementHeaderDto("REQ-0005", "Audit Trail for Compliance", "Critical", "In Progress", "Business", "Imported", "Mike Ross", 70, 3, 2, "2026-04-12T08:00:00Z", "2026-04-16T08:45:00Z")),
                SectionResultDto.ok(new RequirementDescriptionDto(
                        "Maintain a comprehensive audit trail of all user actions, system changes, and AI decisions for regulatory compliance (SOC 2 Type II, GDPR Article 30). All events must be immutable and retained for 7 years.",
                        "Regulatory requirement for SOC 2 certification. Legal team mandates full traceability of all system actions. Insurance underwriter requires audit capability.",
                        List.of(new AcceptanceCriterionDto("AC-040", "All CRUD operations are logged with actor, timestamp, and delta", true),
                                new AcceptanceCriterionDto("AC-041", "AI decisions include reasoning chain in audit log", true),
                                new AcceptanceCriterionDto("AC-042", "Audit logs are immutable (append-only)", false),
                                new AcceptanceCriterionDto("AC-043", "Audit search supports date range and actor filtering", true),
                                new AcceptanceCriterionDto("AC-044", "Retention policy enforces 7-year minimum", false)),
                        List.of("Dedicated audit storage (separate from operational DB)", "Audit write latency does not block user operations (async)"),
                        List.of("Audit data must be encrypted at rest", "Must support export to SIEM tools (Splunk, ELK)", "GDPR right-to-erasure must be handled via pseudonymization, not deletion"))),
                SectionResultDto.ok(new LinkedStoriesSectionDto(List.of(
                        new LinkedStoryDto("US-040", "As an auditor, I can search audit events by date and actor", "Done", "SPEC-040", "Approved"),
                        new LinkedStoryDto("US-041", "As a compliance officer, I can export audit logs", "In Progress", "SPEC-041", "Draft"),
                        new LinkedStoryDto("US-042", "As the system, AI decisions are logged with reasoning", "Ready", null, null)), 3)),
                SectionResultDto.ok(new LinkedSpecsSectionDto(List.of(
                        new LinkedSpecDto("SPEC-040", "Audit Event Schema Specification", "Approved", "v2.0"),
                        new LinkedSpecDto("SPEC-041", "Audit Export API Specification", "Draft", "v0.3")), 2)),
                SectionResultDto.ok(new AiAnalysisDto(70,
                        List.of("Retention policy implementation details", "GDPR pseudonymization strategy not specified"),
                        List.of(new SimilarRequirementDto("REQ-0002", 55)),
                        "Critical — SOC 2 certification deadline is Q3 2026. Blocks production deployment for enterprise tier.",
                        List.of("Define pseudonymization strategy for GDPR compliance", "Specify audit storage partitioning strategy for 7-year retention", "Add specification for SIEM export format (CEF or LEEF)"))),
                SectionResultDto.ok(new SdlcChainDto(List.of(
                        new SdlcChainLinkDto("requirement", "REQ-0005", "Audit Trail for Compliance", "/requirements/REQ-0005"),
                        new SdlcChainLinkDto("user-story", "US-040", "Audit Event Search", "/requirements"),
                        new SdlcChainLinkDto("spec", "SPEC-040", "Audit Event Schema", "/requirements"),
                        new SdlcChainLinkDto("code", "MR-1180", "feat: audit event logging framework", "/code"))))
        );
    }

    // ── REQ-0006 through REQ-0010: compact builders ──

    private static RequirementDetailDto buildDetail0006() {
        return new RequirementDetailDto(
                SectionResultDto.ok(new RequirementHeaderDto("REQ-0006", "User Profile Management", "Low", "Delivered", "Functional", "Manual", "Emily Wang", 100, 2, 1, "2026-04-05T09:00:00Z", "2026-04-14T11:00:00Z")),
                SectionResultDto.ok(new RequirementDescriptionDto("Users can view and edit their profile information including display name, avatar, timezone, and notification preferences.", "Basic user experience requirement. Supports personalization and timezone-aware scheduling across modules.",
                        List.of(new AcceptanceCriterionDto("AC-050", "Users can update their display name and avatar", true), new AcceptanceCriterionDto("AC-051", "Timezone selection affects all date/time displays", true)),
                        List.of("Avatar storage uses existing file service"), List.of("Profile changes must propagate within 5 seconds"))),
                SectionResultDto.ok(new LinkedStoriesSectionDto(List.of(new LinkedStoryDto("US-050", "As a user, I can update my profile settings", "Done", "SPEC-050", "Implemented"), new LinkedStoryDto("US-051", "As a user, I can set my timezone preference", "Done", null, null)), 2)),
                SectionResultDto.ok(new LinkedSpecsSectionDto(List.of(new LinkedSpecDto("SPEC-050", "User Profile API Specification", "Implemented", "v1.0")), 1)),
                SectionResultDto.ok(new AiAnalysisDto(100, List.of(), List.of(), "Low — self-contained feature. Fully delivered.", List.of())),
                SectionResultDto.ok(new SdlcChainDto(List.of(new SdlcChainLinkDto("requirement", "REQ-0006", "User Profile Management", "/requirements/REQ-0006"), new SdlcChainLinkDto("code", "MR-1095", "feat: user profile management", "/code"), new SdlcChainLinkDto("test", "TS-020", "Profile Management Tests", "/testing"))))
        );
    }

    private static RequirementDetailDto buildDetail0007() {
        return new RequirementDetailDto(
                SectionResultDto.ok(new RequirementHeaderDto("REQ-0007", "99.9% Uptime SLA", "Medium", "Approved", "Non-Functional", "Imported", "Sarah Chen", 50, 1, 1, "2026-04-08T10:00:00Z", "2026-04-14T09:30:00Z")),
                SectionResultDto.ok(new RequirementDescriptionDto("The SDLC Control Tower platform must maintain 99.9% uptime measured monthly, excluding planned maintenance windows. This translates to a maximum of 43.8 minutes of unplanned downtime per month.", "Enterprise SLA commitment. Contractual obligation for Tier 1 customers. Failure to meet SLA triggers service credits.",
                        List.of(new AcceptanceCriterionDto("AC-060", "Health check endpoints respond within 2 seconds", true), new AcceptanceCriterionDto("AC-061", "Automated failover completes within 60 seconds", false), new AcceptanceCriterionDto("AC-062", "Monthly uptime report is auto-generated", false)),
                        List.of("Multi-AZ deployment is available", "Database replication is configured"), List.of("Planned maintenance windows must not exceed 4 hours/month", "Failover must be transparent to active users"))),
                SectionResultDto.ok(new LinkedStoriesSectionDto(List.of(new LinkedStoryDto("US-060", "As an SRE, I can view real-time uptime metrics", "Ready", "SPEC-060", "Approved")), 1)),
                SectionResultDto.ok(new LinkedSpecsSectionDto(List.of(new LinkedSpecDto("SPEC-060", "SLA Monitoring and Reporting Spec", "Approved", "v1.0")), 1)),
                SectionResultDto.ok(new AiAnalysisDto(50, List.of("Failover procedure not documented", "Alerting thresholds not specified"), List.of(new SimilarRequirementDto("REQ-0004", 35)), "Medium — operational requirement that affects deployment architecture and monitoring infrastructure.", List.of("Define alerting escalation chain", "Specify RTO and RPO targets", "Add chaos testing requirement"))),
                SectionResultDto.ok(new SdlcChainDto(List.of(new SdlcChainLinkDto("requirement", "REQ-0007", "99.9% Uptime SLA", "/requirements/REQ-0007"), new SdlcChainLinkDto("spec", "SPEC-060", "SLA Monitoring Spec", "/requirements"))))
        );
    }

    private static RequirementDetailDto buildDetail0008() {
        return new RequirementDetailDto(
                SectionResultDto.ok(new RequirementHeaderDto("REQ-0008", "Event-Driven Architecture Migration", "High", "Draft", "Technical", "AI-Generated", "Unassigned", 10, 0, 0, "2026-04-13T17:00:00Z", "2026-04-13T17:00:00Z")),
                SectionResultDto.ok(new RequirementDescriptionDto("Migrate inter-service communication from synchronous REST calls to an event-driven architecture using Apache Kafka. This enables asynchronous processing, better fault isolation, and horizontal scalability.", "Current synchronous architecture creates cascading failures under load. P1 incident INC-0421 (DB pool exhaustion) was caused by synchronous call chains. Event-driven decoupling prevents this class of failure.",
                        List.of(new AcceptanceCriterionDto("AC-070", "Core business events are published to Kafka topics", false), new AcceptanceCriterionDto("AC-071", "Consumers process events idempotently", false), new AcceptanceCriterionDto("AC-072", "Dead letter queue handles failed messages", false)),
                        List.of("Kafka cluster is provisioned", "Schema registry is available for Avro/Protobuf schemas"), List.of("Must maintain REST APIs for external consumers", "Migration must be incremental, not big-bang"))),
                SectionResultDto.ok(new LinkedStoriesSectionDto(List.of(), 0)),
                SectionResultDto.ok(new LinkedSpecsSectionDto(List.of(), 0)),
                new SectionResultDto<>(null, null),
                SectionResultDto.ok(new SdlcChainDto(List.of(new SdlcChainLinkDto("requirement", "REQ-0008", "Event-Driven Architecture Migration", "/requirements/REQ-0008"))))
        );
    }

    private static RequirementDetailDto buildDetail0009() {
        return new RequirementDetailDto(
                SectionResultDto.ok(new RequirementHeaderDto("REQ-0009", "Legacy Report Export", "Medium", "Archived", "Business", "Imported", "Alex Kim", 100, 2, 1, "2026-03-20T09:00:00Z", "2026-04-10T10:00:00Z")),
                SectionResultDto.ok(new RequirementDescriptionDto("Export SDLC metrics and compliance reports in PDF, CSV, and Excel formats compatible with legacy reporting tools used by finance and compliance teams.", "Finance team requires monthly SDLC cost reports in Excel format compatible with their existing SAP integration. Compliance team needs PDF audit reports.",
                        List.of(new AcceptanceCriterionDto("AC-080", "Reports export in PDF, CSV, and XLSX formats", true), new AcceptanceCriterionDto("AC-081", "Export includes all required compliance fields", true)),
                        List.of("Report templates are defined by finance team"), List.of("XLSX format must be compatible with Excel 2016+"))),
                SectionResultDto.ok(new LinkedStoriesSectionDto(List.of(new LinkedStoryDto("US-080", "As a finance user, I can export monthly cost reports", "Done", "SPEC-080", "Implemented"), new LinkedStoryDto("US-081", "As a compliance officer, I can generate PDF audit reports", "Done", null, null)), 2)),
                SectionResultDto.ok(new LinkedSpecsSectionDto(List.of(new LinkedSpecDto("SPEC-080", "Report Export API Specification", "Implemented", "v1.1")), 1)),
                SectionResultDto.ok(new AiAnalysisDto(100, List.of(), List.of(), "Low — archived. Feature delivered and stable.", List.of())),
                SectionResultDto.ok(new SdlcChainDto(List.of(new SdlcChainLinkDto("requirement", "REQ-0009", "Legacy Report Export", "/requirements/REQ-0009"), new SdlcChainLinkDto("code", "MR-1120", "feat: report export service", "/code"))))
        );
    }

    private static RequirementDetailDto buildDetail0010() {
        return new RequirementDetailDto(
                SectionResultDto.ok(new RequirementHeaderDto("REQ-0010", "AI-Powered Requirement Analysis", "High", "In Review", "Functional", "AI-Generated", "Sarah Chen", 35, 2, 0, "2026-04-15T07:00:00Z", "2026-04-16T07:00:00Z")),
                SectionResultDto.ok(new RequirementDescriptionDto("Integrate AI analysis capabilities into the requirement management module to automatically assess completeness, detect duplicates, estimate impact, and suggest improvements for incoming requirements.", "Manual requirement review takes 2-3 hours per requirement. AI analysis can reduce this to 15 minutes by pre-screening completeness and flagging issues. Reduces requirement rework by an estimated 40%.",
                        List.of(new AcceptanceCriterionDto("AC-090", "AI completeness scoring runs on requirement save", false), new AcceptanceCriterionDto("AC-091", "Similar requirement detection uses vector similarity", false), new AcceptanceCriterionDto("AC-092", "AI suggestions are actionable and editable", false)),
                        List.of("LLM API is available with sufficient quota", "Vector embeddings are stored for all existing requirements"), List.of("Analysis must complete within 30 seconds", "AI results must be clearly labeled as suggestions, not authoritative"))),
                SectionResultDto.ok(new LinkedStoriesSectionDto(List.of(new LinkedStoryDto("US-090", "As a product owner, I see AI analysis after saving a requirement", "Draft", null, null), new LinkedStoryDto("US-091", "As a product owner, I can accept or dismiss AI suggestions", "Draft", null, null)), 2)),
                SectionResultDto.ok(new LinkedSpecsSectionDto(List.of(), 0)),
                SectionResultDto.ok(new AiAnalysisDto(35, List.of("LLM model selection not specified", "Embedding dimension and storage strategy undefined", "Confidence threshold for suggestions not defined"), List.of(new SimilarRequirementDto("REQ-0001", 30)), "High — core differentiator for the product. Transforms requirement management from manual to AI-assisted.", List.of("Define fallback behavior when LLM API is unavailable", "Specify minimum confidence threshold for duplicate detection", "Add user feedback loop to improve AI accuracy over time"))),
                SectionResultDto.ok(new SdlcChainDto(List.of(new SdlcChainLinkDto("requirement", "REQ-0010", "AI-Powered Requirement Analysis", "/requirements/REQ-0010"))))
        );
    }
}
