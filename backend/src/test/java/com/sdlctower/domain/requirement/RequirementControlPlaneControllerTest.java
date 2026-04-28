package com.sdlctower.domain.requirement;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sdlctower.shared.ApiConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:sdlctower-req-cp-${random.uuid};DB_CLOSE_DELAY=-1")
class RequirementControlPlaneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sourceAndDocumentEndpointsReturnProfileDrivenSections() throws Exception {
        mockMvc.perform(get(ApiConstants.REQUIREMENT_SOURCES, "REQ-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].sourceType").value("JIRA"));

        mockMvc.perform(get(ApiConstants.REQUIREMENT_SDD_DOCUMENTS, "REQ-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profileId").value("standard-sdd"))
                .andExpect(jsonPath("$.data.workspace.applicationName").value("Payment Gateway"))
                .andExpect(jsonPath("$.data.workspace.sddRepoFullName").value("wwa-lab/payment-gateway-sdd"))
                .andExpect(jsonPath("$.data.workspace.workingBranch").value("project/PAY-2026-sso-upgrade"))
                .andExpect(jsonPath("$.data.workspace.kbRepoFullName").value("wwa-lab/payment-gateway-knowledge-base"))
                .andExpect(jsonPath("$.data.stages.length()").value(9))
                .andExpect(jsonPath("$.data.stages[0].missing").value(false))
                .andExpect(jsonPath("$.data.stages[0].title").value("SSO Requirement"))
                .andExpect(jsonPath("$.data.stages[3].freshnessStatus").value("MISSING_DOCUMENT"));

        mockMvc.perform(get(ApiConstants.REQUIREMENT_SDD_DOCUMENTS, "REQ-0001")
                        .param("profileId", "ibm-i"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profileId").value("ibm-i"))
                .andExpect(jsonPath("$.data.stages.length()").value(10))
                .andExpect(jsonPath("$.data.stages[0].stageLabel").value("Requirement Normalizer"))
                .andExpect(jsonPath("$.data.stages[0].missing").value(true))
                .andExpect(jsonPath("$.data.stages[1].path").value("docs/02-functional-spec/{br-id}.md"))
                .andExpect(jsonPath("$.data.stages[6].path").value("docs/07-test-scaffold/{program}.md"))
                .andExpect(jsonPath("$.data.stages[7].path").value("docs/08-reviews/spec/{br-id}.md"))
                .andExpect(jsonPath("$.data.stages[8].path").value("docs/08-reviews/dds/{file}.md"))
                .andExpect(jsonPath("$.data.stages[9].path").value("docs/08-reviews/code/{program}.md"));
    }

    @Test
    void documentViewerFetchesMarkdownFromGateway() throws Exception {
        mockMvc.perform(get(ApiConstants.REQUIREMENT_SDD_DOCUMENT_DETAIL, "DOC-REQ-0001-SPEC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.markdown", containsString("Source of truth")))
                .andExpect(jsonPath("$.data.markdown", containsString("# SSO Functional Spec")))
                .andExpect(jsonPath("$.data.document.title").value("SSO Functional Spec"))
                .andExpect(jsonPath("$.data.commitSha").value("c0ffee1003"))
                .andExpect(jsonPath("$.data.blobSha").value("blob-spec-0001-v2"));
    }

    @Test
    void refreshSddDocumentsReindexesFromGitHubBranch() throws Exception {
        mockMvc.perform(post(ApiConstants.REQUIREMENT_SDD_DOCUMENTS_REFRESH, "REQ-0005")
                        .param("profileId", "ibm-i"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profileId").value("ibm-i"))
                .andExpect(jsonPath("$.data.workspace.sddRepoFullName").value("wwa-lab/sdlc-tower-sdd"))
                .andExpect(jsonPath("$.data.workspace.workingBranch").value("project/TOWER-2026-audit-trail"))
                .andExpect(jsonPath("$.data.stages.length()").value(10))
                .andExpect(jsonPath("$.data.stages[0].missing").value(false))
                .andExpect(jsonPath("$.data.stages[0].path").value("docs/01-requirements/BR-AUDIT-001-normalizer.md"))
                .andExpect(jsonPath("$.data.stages[3].title").value("AUDITRPT Program Spec"))
                .andExpect(jsonPath("$.data.stages[9].latestCommitSha", containsString("sync-")));
    }

    @Test
    void reviewCreationIsBoundToCommitAndBlobVersion() throws Exception {
        mockMvc.perform(post(ApiConstants.REQUIREMENT_DOCUMENT_REVIEWS, "DOC-REQ-0001-SPEC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision": "APPROVED",
                                  "comment": "Approved against latest GitHub blob.",
                                  "commitSha": "c0ffee1003",
                                  "blobSha": "blob-spec-0001-v2"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.decision").value("APPROVED"))
                .andExpect(jsonPath("$.data.stale").value(false));

        mockMvc.perform(post(ApiConstants.REQUIREMENT_DOCUMENT_REVIEWS, "DOC-REQ-0001-SPEC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision": "COMMENT",
                                  "comment": "This uses an older blob.",
                                  "commitSha": "c0ffee1002",
                                  "blobSha": "blob-spec-0001-v1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.stale").value(true));
    }

    @Test
    void rejectedReviewRequiresReason() throws Exception {
        mockMvc.perform(post(ApiConstants.REQUIREMENT_DOCUMENT_REVIEWS, "DOC-REQ-0001-SPEC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision": "REJECTED",
                                  "comment": "   ",
                                  "commitSha": "c0ffee1003",
                                  "blobSha": "blob-spec-0001-v2"
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post(ApiConstants.REQUIREMENT_DOCUMENT_REVIEWS, "DOC-REQ-0001-SPEC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision": "REJECTED",
                                  "comment": "Acceptance criteria are not reflected in the functional flow.",
                                  "commitSha": "c0ffee1003",
                                  "blobSha": "blob-spec-0001-v2"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.decision").value("REJECTED"))
                .andExpect(jsonPath("$.data.comment").value("Acceptance criteria are not reflected in the functional flow."));
    }

    @Test
    void agentRunCallbackCreatesArtifactLinksAndTraceability() throws Exception {
        String body = mockMvc.perform(post(ApiConstants.REQUIREMENT_AGENT_RUNS, "REQ-0001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "skillKey": "req-to-user-story",
                                  "targetStage": "spec"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status").value("MANIFEST_READY"))
                .andExpect(jsonPath("$.data.manifest.sddWorkspace.workingBranch").value("project/PAY-2026-sso-upgrade"))
                .andExpect(jsonPath("$.data.manifest.knowledgeBase.repoFullName").value("wwa-lab/payment-gateway-knowledge-base"))
                .andExpect(jsonPath("$.data.manifest.sources.length()").value(2))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String executionId = body.replaceAll("(?s).*\"executionId\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(post(ApiConstants.REQUIREMENT_AGENT_RUN_CALLBACK, executionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "COMPLETED",
                                  "outputSummary": { "message": "PR opened" },
                                  "artifactLinks": [
                                    {
                                      "artifactType": "GITHUB_PR",
                                      "storageType": "GITHUB",
                                      "title": "Generate SDD update",
                                      "uri": "https://github.com/wwa-lab/payment-gateway-pro/pull/42",
                                      "repoFullName": "wwa-lab/payment-gateway-pro",
                                      "status": "IN_REVIEW"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.artifactLinks.length()").value(1));

        mockMvc.perform(get(ApiConstants.REQUIREMENT_TRACEABILITY, "REQ-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.agentRuns.length()", greaterThan(0)))
                .andExpect(jsonPath("$.data.artifactLinks.length()", greaterThan(0)))
                .andExpect(jsonPath("$.data.freshness.length()", greaterThan(0)));
    }
}
