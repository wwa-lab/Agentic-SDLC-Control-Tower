package com.sdlctower.domain.requirement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sdlctower.domain.requirement.dto.CreateRequirementRequestDto;
import com.sdlctower.domain.requirement.dto.RawRequirementInputDto;
import com.sdlctower.domain.requirement.dto.RequirementNormalizeRequestDto;
import com.sdlctower.shared.exception.ResourceNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class RequirementServiceTest {

    @Autowired
    private RequirementService requirementService;

    @Test
    void getRequirementListReturnsAllSeedRequirements() {
        var list = requirementService.getRequirementList(null, null, null, null, null, null);

        assertEquals(10, list.items().size());
        assertEquals(10, list.totalCount());
        assertEquals(2, list.statusDistribution().draft());
    }

    @Test
    void getRequirementListFiltersByPriority() {
        var list = requirementService.getRequirementList("Critical", null, null, null, null, null);

        assertEquals(2, list.items().size());
        assertTrue(list.items().stream().allMatch(item -> item.priority().equals("Critical")));
    }

    @Test
    void getRequirementDetailThrowsForUnknownId() {
        assertThrows(ResourceNotFoundException.class, () -> requirementService.getRequirementDetail("REQ-9999"));
    }

    @Test
    void normalizeRequirementFlagsImageUploads() {
        var draft = requirementService.normalizeRequirement(new RequirementNormalizeRequestDto(
                new RawRequirementInputDto("FILE", "[Image upload: screen.png]", "screen.png", 1024L, List.of("screen.png"), 1, "requirement-intake"),
                "standard-sdd"
        ));

        assertEquals("Imported requirement from screen.png", draft.title());
        assertTrue(draft.missingInfo().stream().anyMatch(item -> item.contains("Image OCR")));
        assertEquals("standard-sdd-normalizer", draft.normalizedBy());
    }

    @Test
    void normalizeRequirementRecognizesZipPackages() throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put(
                "requirements.txt",
                utf8("IBM i batch jobs need monitoring.\n- Alert within five minutes\n- Attach failing job details")
        );
        entries.put(
                "briefing.docx",
                createMinimalDocx(
                        "Operations reported overnight failures in APAC.",
                        "- Notify the support team before start of business"
                )
        );
        entries.put(
                "sizing.xlsx",
                createMinimalXlsx(new String[][] {
                        {"Requirement", "Priority"},
                        {"Attach failing job details", "High"}
                })
        );
        entries.put("evidence/screenshot.png", new byte[] {0x01, 0x02, 0x03});

        var archive = new MockMultipartFile(
                "file",
                "mixed-requirement-bundle.zip",
                "application/zip",
                createZipArchive(entries)
        );
        var draft = requirementService.normalizeUploadedRequirement("requirement-intake", "standard-sdd", List.of(archive));

        assertEquals("Imported requirement package from mixed-requirement-bundle.zip", draft.title());
        assertTrue(draft.summary().contains("requirements.txt"));
        assertTrue(draft.summary().contains("briefing.docx"));
        assertTrue(draft.summary().contains("sizing.xlsx"));
        assertTrue(draft.acceptanceCriteria().contains("Alert within five minutes"));
        assertTrue(draft.missingInfo().stream().anyMatch(item -> item.contains("screenshot.png")));
        assertTrue(draft.openQuestions().stream().anyMatch(item -> item.contains("source of truth")));
        assertNotNull(draft.importInspection());
        assertEquals(4, draft.importInspection().totalFiles());
        assertEquals(3, draft.importInspection().parsedFiles());
        assertEquals(1, draft.importInspection().manualReviewFiles());
        assertTrue(draft.importInspection().files().stream().anyMatch(file -> file.fileName().equals("briefing.docx") && "PARSED".equals(file.processingStatus())));
        assertTrue(draft.importInspection().files().stream().anyMatch(file -> file.fileName().equals("sizing.xlsx") && file.preview() != null && file.preview().contains("Attach failing job details")));
    }

    @Test
    void normalizeRequirementSupportsBatchUploads() {
        var summary = new MockMultipartFile(
                "file",
                "summary.md",
                "text/markdown",
                utf8("Treasury upload must support 100 MB request bundles.\n- Accept repeated multipart file fields\n- Require kb_name for downstream KB routing")
        );
        var notes = new MockMultipartFile(
                "file",
                "scope.html",
                "text/html",
                utf8("<html><body><h1>Scope</h1><p>Priority: High</p><p>Capture mixed raw requirement sources.</p></body></html>")
        );

        var draft = requirementService.normalizeUploadedRequirement("requirement-intake", "standard-sdd", List.of(summary, notes));

        assertEquals("Imported requirement package from 2 files", draft.title());
        assertTrue(draft.summary().contains("summary.md"));
        assertTrue(draft.summary().contains("scope.html"));
        assertNotNull(draft.importInspection());
        assertEquals("BATCH", draft.importInspection().sourceKind());
        assertEquals(2, draft.importInspection().totalFiles());
        assertEquals(2, draft.importInspection().parsedFiles());
        assertTrue(draft.openQuestions().stream().anyMatch(item -> item.contains("source of truth")));
        assertTrue(draft.openQuestions().stream().anyMatch(item -> item.contains("split into multiple requirements")));
    }

    @Test
    void normalizeRequirementRejectsOversizedBatchUpload() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> requirementService.normalizeUploadedRequirement(
                        "requirement-intake",
                        "standard-sdd",
                        List.of(oversizedMultipart("bulk.zip", 101L * 1024L * 1024L))
                )
        );

        assertTrue(exception.getMessage().contains("100 MB"));
    }

    @Test
    void createRequirementPersistsRequirement() {
        var created = requirementService.createRequirement(new CreateRequirementRequestDto(
                "Create IBM i batch monitor",
                "High",
                "Technical",
                "Introduce an IBM i batch monitor for overnight job failures.",
                "Operations needs faster detection of overnight batch failures.",
                List.of("Batch monitor captures failed jobs within five minutes"),
                List.of("IBM i event feed is available"),
                List.of("Must reuse existing notification channel"),
                new RawRequirementInputDto("TEXT", "Need IBM i batch monitor", null, null, List.of(), 0, "requirement-intake")
        ));

        assertEquals("REQ-0011", created.id());
        assertEquals(11, requirementService.getRequirementList(null, null, null, null, null, null).items().size());
        assertNotNull(requirementService.getRequirementDetail(created.id()));
        assertEquals("Create IBM i batch monitor", requirementService.getRequirementDetail(created.id()).header().data().title());
    }

    private static byte[] createZipArchive(Map<String, byte[]> entries) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                zipOutputStream.putNextEntry(new ZipEntry(entry.getKey()));
                zipOutputStream.write(entry.getValue());
                zipOutputStream.closeEntry();
            }
        }
        return outputStream.toByteArray();
    }

    private static byte[] utf8(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] createMinimalDocx(String... paragraphs) throws IOException {
        StringBuilder body = new StringBuilder();
        for (String paragraph : paragraphs) {
            body.append("<w:p><w:r><w:t>")
                    .append(escapeXml(paragraph))
                    .append("</w:t></w:r></w:p>");
        }
        return createZipArchive(Map.of(
                "word/document.xml",
                utf8("""
                        <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                          <w:body>%s</w:body>
                        </w:document>
                        """.formatted(body))
        ));
    }

    private static byte[] createMinimalXlsx(String[][] rows) throws IOException {
        StringBuilder sheetData = new StringBuilder();
        for (int rowIndex = 0; rowIndex < rows.length; rowIndex += 1) {
            sheetData.append("<row r=\"").append(rowIndex + 1).append("\">");
            for (int cellIndex = 0; cellIndex < rows[rowIndex].length; cellIndex += 1) {
                String cellRef = Character.toString((char) ('A' + cellIndex)) + (rowIndex + 1);
                sheetData.append("<c r=\"").append(cellRef).append("\" t=\"inlineStr\"><is><t>")
                        .append(escapeXml(rows[rowIndex][cellIndex]))
                        .append("</t></is></c>");
            }
            sheetData.append("</row>");
        }
        return createZipArchive(Map.of(
                "xl/worksheets/sheet1.xml",
                utf8("""
                        <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                          <sheetData>%s</sheetData>
                        </worksheet>
                        """.formatted(sheetData))
        ));
    }

    private static String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static MultipartFile oversizedMultipart(String fileName, long size) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return fileName;
            }

            @Override
            public String getContentType() {
                return "application/zip";
            }

            @Override
            public boolean isEmpty() {
                return size <= 0;
            }

            @Override
            public long getSize() {
                return size;
            }

            @Override
            public byte[] getBytes() {
                return new byte[0];
            }

            @Override
            public java.io.InputStream getInputStream() {
                return java.io.InputStream.nullInputStream();
            }

            @Override
            public void transferTo(File dest) {
                throw new UnsupportedOperationException("Not needed for test");
            }
        };
    }
}
