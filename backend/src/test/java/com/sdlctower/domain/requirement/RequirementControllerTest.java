package com.sdlctower.domain.requirement;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdlctower.shared.ApiConstants;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mock.web.MockMultipartFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class RequirementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listRequirementsReturns200WithDistributionAndRequirements() throws Exception {
        mockMvc.perform(get(ApiConstants.REQUIREMENTS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.statusDistribution.draft").value(2))
                .andExpect(jsonPath("$.data.statusDistribution.DRAFT").value(2))
                .andExpect(jsonPath("$.data.statusDistribution.inReview").value(2))
                .andExpect(jsonPath("$.data.statusDistribution.approved").value(2))
                .andExpect(jsonPath("$.data.statusDistribution.inProgress").value(2))
                .andExpect(jsonPath("$.data.statusDistribution.delivered").value(1))
                .andExpect(jsonPath("$.data.statusDistribution.archived").value(1))
                .andExpect(jsonPath("$.data.items.length()").value(10))
                .andExpect(jsonPath("$.data.totalCount").value(10))
                .andExpect(jsonPath("$.data.requirements.length()").value(10))
                .andExpect(jsonPath("$.data.requirements[0].id").value("REQ-0001"))
                .andExpect(jsonPath("$.data.requirements[0].priority").value("Critical"))
                .andExpect(jsonPath("$.data.requirements[0].status").value("Approved"))
                .andExpect(jsonPath("$.data.requirements[0].category").value("Functional"))
                .andExpect(jsonPath("$.data.requirements[0].completeness").value(85))
                .andExpect(jsonPath("$.data.requirements[0].completenessScore").value(85))
                .andExpect(jsonPath("$.data.requirements[0].assignee").value("Sarah Chen"))
                .andExpect(jsonPath("$.data.requirements[0].createdAt").value("2026-04-10T09:00:00Z"))
                .andExpect(jsonPath("$.data.requirements[9].id").value("REQ-0010"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void listRequirementsFiltersByPriority() throws Exception {
        mockMvc.perform(get(ApiConstants.REQUIREMENTS).param("priority", "Critical"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requirements.length()").value(2))
                .andExpect(jsonPath("$.data.requirements[0].priority").value("Critical"))
                .andExpect(jsonPath("$.data.requirements[1].priority").value("Critical"));
    }

    @Test
    void listRequirementsFiltersByCategory() throws Exception {
        mockMvc.perform(get(ApiConstants.REQUIREMENTS).param("category", "Technical"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requirements.length()").value(2))
                .andExpect(jsonPath("$.data.requirements[0].category").value("Technical"))
                .andExpect(jsonPath("$.data.requirements[1].category").value("Technical"));
    }

    @Test
    void listRequirementsFiltersByStatus() throws Exception {
        mockMvc.perform(get(ApiConstants.REQUIREMENTS).param("status", "Draft"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requirements.length()").value(2))
                .andExpect(jsonPath("$.data.requirements[0].status").value("Draft"))
                .andExpect(jsonPath("$.data.requirements[1].status").value("Draft"));
    }

    @Test
    void listRequirementsFiltersBySearch() throws Exception {
        mockMvc.perform(get(ApiConstants.REQUIREMENTS).param("search", "SSO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requirements.length()").value(1))
                .andExpect(jsonPath("$.data.requirements[0].id").value("REQ-0001"));
    }

    @Test
    void getRequirementDetailReturns200WithAllSixSections() throws Exception {
        mockMvc.perform(get("/api/v1/requirements/REQ-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.header.data.id").value("REQ-0001"))
                .andExpect(jsonPath("$.data.header.data.title").value("User Authentication and SSO Integration"))
                .andExpect(jsonPath("$.data.header.data.priority").value("Critical"))
                .andExpect(jsonPath("$.data.header.data.source").value("Manual"))
                .andExpect(jsonPath("$.data.header.data.assignee").value("Sarah Chen"))
                .andExpect(jsonPath("$.data.header.data.completenessScore").value(85))
                .andExpect(jsonPath("$.data.header.data.storyCount").value(4))
                .andExpect(jsonPath("$.data.header.data.specCount").value(2))
                .andExpect(jsonPath("$.data.header.error").value(nullValue()))
                .andExpect(jsonPath("$.data.description.data.acceptanceCriteria.length()").value(5))
                .andExpect(jsonPath("$.data.description.data.acceptanceCriteria[0].isMet").value(true))
                .andExpect(jsonPath("$.data.description.data.assumptions.length()").value(3))
                .andExpect(jsonPath("$.data.description.data.constraints.length()").value(3))
                .andExpect(jsonPath("$.data.description.error").value(nullValue()))
                .andExpect(jsonPath("$.data.linkedStories.data.stories.length()").value(4))
                .andExpect(jsonPath("$.data.linkedStories.data.totalCount").value(4))
                .andExpect(jsonPath("$.data.linkedStories.data.stories[0].specId").value("SPEC-001"))
                .andExpect(jsonPath("$.data.linkedStories.error").value(nullValue()))
                .andExpect(jsonPath("$.data.linkedSpecs.data.specs.length()").value(2))
                .andExpect(jsonPath("$.data.linkedSpecs.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.linkedSpecs.error").value(nullValue()))
                .andExpect(jsonPath("$.data.aiAnalysis.data.completenessScore").value(85))
                .andExpect(jsonPath("$.data.aiAnalysis.data.missingElements.length()").value(2))
                .andExpect(jsonPath("$.data.aiAnalysis.data.similarRequirements.length()").value(2))
                .andExpect(jsonPath("$.data.aiAnalysis.data.suggestions.length()").value(4))
                .andExpect(jsonPath("$.data.aiAnalysis.error").value(nullValue()))
                .andExpect(jsonPath("$.data.sdlcChain.data.links.length()").value(6))
                .andExpect(jsonPath("$.data.sdlcChain.error").value(nullValue()))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void getRequirementDetailReturns404ForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/requirements/REQ-9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Requirement not found: REQ-9999"));
    }

    @Test
    void getRequirementDetailReturnsNullAiAnalysisForDraftRequirement() throws Exception {
        mockMvc.perform(get("/api/v1/requirements/REQ-0003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.header.data.status").value("Draft"))
                .andExpect(jsonPath("$.data.linkedStories.data.stories.length()").value(0))
                .andExpect(jsonPath("$.data.linkedSpecs.data.specs.length()").value(0))
                .andExpect(jsonPath("$.data.aiAnalysis.data").value(nullValue()));
    }

    @Test
    void generateStoriesReturns202WithExecutionMetadata() throws Exception {
        mockMvc.perform(post("/api/v1/requirements/REQ-0001/generate-stories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.executionId").value("EXEC-STORY-REQ-0001"))
                .andExpect(jsonPath("$.data.skillName").value("req-to-user-story"))
                .andExpect(jsonPath("$.data.status").value("RUNNING"))
                .andExpect(jsonPath("$.data.requirementId").value("REQ-0001"))
                .andExpect(jsonPath("$.data.message").value("Story generation initiated for REQ-0001"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void generateSpecReturns202ForRequirementScope() throws Exception {
        mockMvc.perform(post("/api/v1/requirements/REQ-0001/generate-spec")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"storyIds\":[\"US-001\",\"US-002\"]}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.executionId").value("EXEC-SPEC-REQ-0001"))
                .andExpect(jsonPath("$.data.skillName").value("user-story-to-spec"))
                .andExpect(jsonPath("$.data.status").value("RUNNING"))
                .andExpect(jsonPath("$.data.requirementId").value("REQ-0001"))
                .andExpect(jsonPath("$.data.inputStoryIds.length()").value(2))
                .andExpect(jsonPath("$.data.inputStoryIds[0]").value("US-001"))
                .andExpect(jsonPath("$.data.inputStoryIds[1]").value("US-002"))
                .andExpect(jsonPath("$.data.message").value("Spec generation initiated for 2 stories under REQ-0001"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void generateSpecReturns202ForLegacyStoryScope() throws Exception {
        mockMvc.perform(post("/api/v1/requirements/stories/US-001/generate-spec")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.executionId").value("EXEC-SPEC-REQ-0001"))
                .andExpect(jsonPath("$.data.requirementId").value("REQ-0001"))
                .andExpect(jsonPath("$.data.inputStoryIds.length()").value(1))
                .andExpect(jsonPath("$.data.inputStoryIds[0]").value("US-001"))
                .andExpect(jsonPath("$.data.message").value("Spec generation initiated for 1 story under REQ-0001"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void analyzeRequirementReturns202WithExecutionMetadata() throws Exception {
        mockMvc.perform(post("/api/v1/requirements/REQ-0001/analyze")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.executionId").value("EXEC-ANALYSIS-REQ-0001"))
                .andExpect(jsonPath("$.data.skillName").value("requirement-analysis"))
                .andExpect(jsonPath("$.data.status").value("RUNNING"))
                .andExpect(jsonPath("$.data.requirementId").value("REQ-0001"))
                .andExpect(jsonPath("$.data.message").value("AI analysis initiated for REQ-0001"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void generateStoriesReturns404ForUnknownRequirement() throws Exception {
        mockMvc.perform(post("/api/v1/requirements/REQ-9999/generate-stories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Requirement not found: REQ-9999"));
    }

    @Test
    void generateSpecReturns400WhenStoryIdsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/requirements/REQ-0001/generate-spec")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("At least one story ID is required"));
    }

    @Test
    void generateSpecReturns400WhenStoryNotLinkedToRequirement() throws Exception {
        mockMvc.perform(post("/api/v1/requirements/REQ-0001/generate-spec")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"storyIds\":[\"US-010\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Story US-010 is not linked to requirement REQ-0001"));
    }

    @Test
    void generateSpecReturns404ForUnknownStoryOnLegacyRoute() throws Exception {
        mockMvc.perform(post("/api/v1/requirements/stories/US-999/generate-spec")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Story not found: US-999"));
    }

    @Test
    void getActivePipelineProfileReturnsStandardSdd() throws Exception {
        mockMvc.perform(get(ApiConstants.PIPELINE_PROFILES_ACTIVE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("standard-sdd"))
                .andExpect(jsonPath("$.data.name").value("Standard SDD"))
                .andExpect(jsonPath("$.data.usesOrchestrator").value(false))
                .andExpect(jsonPath("$.data.chainNodes.length()").value(8))
                .andExpect(jsonPath("$.data.skills.length()").value(10))
                .andExpect(jsonPath("$.data.skills[2].skillId").value("spec-to-architecture"))
                .andExpect(jsonPath("$.data.skills[9].skillId").value("review-doc-quality"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void invokeSkillReturnsOrchestratorResultForIbmI() throws Exception {
        mockMvc.perform(post("/api/v1/requirements/REQ-0001/invoke-skill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skillId\":\"ibm-i-workflow-orchestrator\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.executionId").value("EXEC-SKILL-REQ-0001-IBM-I-WORKFLOW-ORCHESTRATOR"))
                .andExpect(jsonPath("$.data.status").value("TRIGGERED"))
                .andExpect(jsonPath("$.data.orchestratorResult.determinedPathId").value("enhancement"))
                .andExpect(jsonPath("$.data.orchestratorResult.determinedTier").value("L2"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void normalizeRequirementReturnsDraft() throws Exception {
        mockMvc.perform(post("/api/v1/requirements/normalize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rawInput": {
                                    "sourceType": "FILE",
                                    "text": "[Image upload: screenshot.png]",
                                    "fileName": "screenshot.png",
                                    "fileSize": 2048
                                  },
                                  "profileId": "standard-sdd"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.title").value("Imported requirement from screenshot.png"))
                .andExpect(jsonPath("$.data.prioritySuggestion").value("Medium"))
                .andExpect(jsonPath("$.data.categorySuggestion").value("Functional"))
                .andExpect(jsonPath("$.data.missingInfo.length()").value(2))
                .andExpect(jsonPath("$.data.missingInfo[1]").value("Image OCR is not implemented in V1 — review the attached image manually"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void normalizeRequirementReturnsZipAwareDraft() throws Exception {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put(
                "requirements/brief.txt",
                utf8("Payment system must support multi-currency settlement.\n- Support USD and EUR settlements\n- Reconcile exchange rates daily")
        );
        entries.put(
                "requirements/specification.docx",
                createMinimalDocx(
                        "Finance wants one imported requirement draft from mixed source material.",
                        "- Capture exchange rate at posting time"
                )
        );
        entries.put(
                "attachments/estimate.xlsx",
                createMinimalXlsx(new String[][] {
                        {"Requirement", "Priority"},
                        {"Support APAC local-currency settlement", "High"}
                })
        );
        entries.put("attachments/screenshot.png", new byte[] {0x01, 0x02, 0x03});

        MockMultipartFile archive = new MockMultipartFile(
                "file",
                "mixed-sources.zip",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                createZipArchive(entries)
        );

        mockMvc.perform(multipart("/api/v1/requirements/normalize")
                        .file(archive)
                        .param("kb_name", "requirement-intake")
                        .param("profileId", "standard-sdd"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.title").value("Imported requirement package from mixed-sources.zip"))
                .andExpect(jsonPath("$.data.summary").value(org.hamcrest.Matchers.containsString("brief.txt")))
                .andExpect(jsonPath("$.data.summary").value(org.hamcrest.Matchers.containsString("specification.docx")))
                .andExpect(jsonPath("$.data.summary").value(org.hamcrest.Matchers.containsString("estimate.xlsx")))
                .andExpect(jsonPath("$.data.summary").value(org.hamcrest.Matchers.containsString("screenshot.png")))
                .andExpect(jsonPath("$.data.acceptanceCriteria[0]").value("Support USD and EUR settlements"))
                .andExpect(jsonPath("$.data.acceptanceCriteria[1]").value("Reconcile exchange rates daily"))
                .andExpect(jsonPath("$.data.importInspection.sourceKind").value("ZIP"))
                .andExpect(jsonPath("$.data.importInspection.totalFiles").value(4))
                .andExpect(jsonPath("$.data.importInspection.parsedFiles").value(3))
                .andExpect(jsonPath("$.data.importInspection.manualReviewFiles").value(1))
                .andExpect(jsonPath("$.data.importInspection.files[1].fileName").value("requirements/specification.docx"))
                .andExpect(jsonPath("$.data.importInspection.files[1].processingStatus").value("PARSED"))
                .andExpect(jsonPath("$.data.importInspection.files[2].fileName").value("attachments/estimate.xlsx"))
                .andExpect(jsonPath("$.data.importInspection.files[2].preview").value(org.hamcrest.Matchers.containsString("Support APAC local-currency settlement")))
                .andExpect(jsonPath("$.data.missingInfo[0]").value("Manual review is still required for: screenshot.png"))
                .andExpect(jsonPath("$.data.openQuestions[0]").value("Which files inside the ZIP package should be treated as the source of truth?"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void normalizeRequirementSupportsBatchMultipartUpload() throws Exception {
        MockMultipartFile summary = new MockMultipartFile(
                "file",
                "summary.md",
                "text/markdown",
                utf8("Treasury intake must accept KB-aligned multipart uploads.\n- Accept repeated file parts\n- Keep total request size below 100 MB")
        );
        MockMultipartFile scope = new MockMultipartFile(
                "file",
                "scope.html",
                "text/html",
                utf8("<html><body><h1>Scope</h1><p>Priority: High</p><p>Capture mixed raw requirement sources.</p></body></html>")
        );

        mockMvc.perform(multipart("/api/v1/requirements/normalize")
                        .file(summary)
                        .file(scope)
                        .param("kb_name", "requirement-intake")
                        .param("profileId", "standard-sdd"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.title").value("Imported requirement package from 2 files"))
                .andExpect(jsonPath("$.data.summary").value(org.hamcrest.Matchers.containsString("summary.md")))
                .andExpect(jsonPath("$.data.summary").value(org.hamcrest.Matchers.containsString("scope.html")))
                .andExpect(jsonPath("$.data.importInspection.sourceKind").value("BATCH"))
                .andExpect(jsonPath("$.data.importInspection.totalFiles").value(2))
                .andExpect(jsonPath("$.data.importInspection.parsedFiles").value(2))
                .andExpect(jsonPath("$.data.openQuestions[0]").value("Which uploaded file should be treated as the source of truth for this requirement?"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void normalizeRequirementRequiresKbNameForMultipartUpload() throws Exception {
        MockMultipartFile summary = new MockMultipartFile(
                "file",
                "summary.md",
                "text/markdown",
                utf8("Need KB name")
        );

        mockMvc.perform(multipart("/api/v1/requirements/normalize")
                        .file(summary)
                        .param("profileId", "standard-sdd"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("kb_name is required"));
    }

    @Test
    void startImportReturnsReceiptAndStatusEndpointReturnsDraftWhenReady() throws Exception {
        MockMultipartFile summary = new MockMultipartFile(
                "file",
                "summary.md",
                "text/markdown",
                utf8("Need a batch monitor for overnight IBM i failures.")
        );
        MockMultipartFile scope = new MockMultipartFile(
                "file",
                "scope.html",
                "text/html",
                utf8("<html><body><p>Alert support within five minutes.</p></body></html>")
        );

        String responseBody = mockMvc.perform(multipart(ApiConstants.REQUIREMENT_IMPORTS)
                        .file(summary)
                        .file(scope)
                        .param("kb_name", "example_kb")
                        .param("profileId", "standard-sdd"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status").value("PROCESSING"))
                .andExpect(jsonPath("$.data.knowledgeBaseName").value("example_kb"))
                .andExpect(jsonPath("$.data.totalNumberOfFiles").value(2))
                .andExpect(jsonPath("$.data.numberOfSuccesses").value(2))
                .andExpect(jsonPath("$.data.numberOfFailures").value(0))
                .andExpect(jsonPath("$.data.files.length()").value(2))
                .andExpect(jsonPath("$.data.draft").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String importId = objectMapper.readTree(responseBody).path("data").path("importId").asText();

        mockMvc.perform(get(ApiConstants.REQUIREMENT_IMPORTS + "/" + importId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.importId").value(importId))
                .andExpect(jsonPath("$.data.status").value("DRAFT_READY"))
                .andExpect(jsonPath("$.data.draft.title").value("Imported requirement package from 2 files"))
                .andExpect(jsonPath("$.data.draft.summary").value(org.hamcrest.Matchers.containsString("example_kb")))
                .andExpect(jsonPath("$.data.draft.importInspection.totalFiles").value(2))
                .andExpect(jsonPath("$.data.draft.importInspection.parsedFiles").value(2))
                .andExpect(jsonPath("$.data.files[0].providerStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void startImportReportsUnsupportedFilesInReceipt() throws Exception {
        MockMultipartFile executable = new MockMultipartFile(
                "file",
                "payload.exe",
                "application/octet-stream",
                new byte[] {0x01, 0x02, 0x03}
        );

        mockMvc.perform(multipart(ApiConstants.REQUIREMENT_IMPORTS)
                        .file(executable)
                        .param("kb_name", "example_kb")
                        .param("profileId", "standard-sdd"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status").value("FAILED"))
                .andExpect(jsonPath("$.data.numberOfSuccesses").value(0))
                .andExpect(jsonPath("$.data.numberOfFailures").value(1))
                .andExpect(jsonPath("$.data.unsupportedFileTypes[0]").value(".exe"))
                .andExpect(jsonPath("$.data.files[0].supported").value(false))
                .andExpect(jsonPath("$.data.files[0].providerStatus").value("UNSUPPORTED"))
                .andExpect(jsonPath("$.data.files[0].errorMessage").value("Unsupported file type for knowledge base ingestion"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void createRequirementReturnsCreatedItem() throws Exception {
        mockMvc.perform(post("/api/v1/requirements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Support multi-currency settlements",
                                  "priority": "High",
                                  "category": "Business",
                                  "summary": "Support settlement in USD, EUR, GBP, and JPY.",
                                  "businessJustification": "APAC finance requires local-currency settlement support.",
                                  "acceptanceCriteria": ["System supports USD, EUR, GBP, and JPY"],
                                  "assumptions": ["Treasury rates API is available"],
                                  "constraints": ["Must launch in Q3"],
                                  "sourceAttachment": {
                                    "sourceType": "TEXT",
                                    "text": "Need multi-currency settlement support",
                                    "fileName": null,
                                    "fileSize": null
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("REQ-0011"))
                .andExpect(jsonPath("$.data.status").value("Draft"))
                .andExpect(jsonPath("$.data.storyCount").value(0))
                .andExpect(jsonPath("$.data.specCount").value(0))
                .andExpect(jsonPath("$.data.completenessScore").value(100))
                .andExpect(jsonPath("$.error").doesNotExist());
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
}
