package com.sdlctower.platform.profile;

import com.sdlctower.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PipelineProfileService {

    private static final PipelineProfileDto STANDARD_SDD_PROFILE = new PipelineProfileDto(
            "standard-sdd",
            "Standard SDD",
            "Spec-Driven Development pipeline with 11-node chain, quality gate skill, per-layer traceability",
            List.of(
                    new PipelineChainNodeDto("req", "Requirement", "requirement", false),
                    new PipelineChainNodeDto("story", "User Story", "user-story", false),
                    new PipelineChainNodeDto("spec", "Spec", "spec", true),
                    new PipelineChainNodeDto("arch", "Architecture", "architecture", false),
                    new PipelineChainNodeDto("data-flow", "Data Flow", "design", false),
                    new PipelineChainNodeDto("data-model", "Data Model", "design", false),
                    new PipelineChainNodeDto("design", "Design", "design", false),
                    new PipelineChainNodeDto("api", "API Guide", "design", false),
                    new PipelineChainNodeDto("tasks", "Tasks", "tasks", false),
                    new PipelineChainNodeDto("code", "Code", "code", false),
                    new PipelineChainNodeDto("test", "Test", "test", false)
            ),
            List.of(
                    new PipelineSkillBindingDto("req-to-user-story", "CLI Story Derivation", "requirement"),
                    new PipelineSkillBindingDto("user-story-to-spec", "CLI Spec Generation", "user-story"),
                    new PipelineSkillBindingDto("document-quality-gate", "Document Quality Gate", "sdd-document")
            ),
            List.of(
                    new PipelineEntryPathDto("standard", "Standard", "Single entry path through requirement capture")
            ),
            List.of(
                    new PipelineDocumentStageDto("requirement", "Requirement", "docs/01-requirements/{slug}.md", "requirement", null, "REQ"),
                    new PipelineDocumentStageDto("user-story", "User Stories", "docs/02-user-stories/{slug}.md", "user-story", null, "US"),
                    new PipelineDocumentStageDto("spec", "Spec", "docs/03-spec/{slug}.md", "spec", null, "SPEC"),
                    new PipelineDocumentStageDto("architecture", "Architecture", "docs/04-architecture/{slug}.md", "architecture", null, "ARCH"),
                    new PipelineDocumentStageDto("data-flow", "Data Flow", "docs/04-architecture/{slug}-data-flow.md", "design", null, "FLOW"),
                    new PipelineDocumentStageDto("data-model", "Data Model", "docs/04-architecture/{slug}-data-model.md", "design", null, "MODEL"),
                    new PipelineDocumentStageDto("design", "Design", "docs/05-design/{slug}.md", "design", null, "DESIGN"),
                    new PipelineDocumentStageDto("api-guide", "API Guide", "docs/05-design/contracts/{slug}-api.md", "design", null, "API"),
                    new PipelineDocumentStageDto("tasks", "Tasks", "docs/06-tasks/{slug}-tasks.md", "tasks", null, "TASK")
            ),
            null,
            false,
            "per-layer"
    );

    private static final PipelineProfileDto IBM_I_PROFILE = new PipelineProfileDto(
            "ibm-i",
            "IBM i",
            "IBM i pipeline with orchestrator and quality gate skills, L1/L2/L3 tiering, shared-br traceability",
            List.of(
                    new PipelineChainNodeDto("requirement-normalizer", "Requirement Normalizer", "requirement", false),
                    new PipelineChainNodeDto("functional-spec", "Functional Spec", "functional-spec", false),
                    new PipelineChainNodeDto("technical-design", "Technical Design", "technical-design", false),
                    new PipelineChainNodeDto("program-spec", "Program Spec", "program-spec", true),
                    new PipelineChainNodeDto("file-spec", "File Spec", "file-spec", false),
                    new PipelineChainNodeDto("ut-plan", "UT Plan", "test-plan", false),
                    new PipelineChainNodeDto("test-scaffold", "Test Scaffold", "test-scaffold", false),
                    new PipelineChainNodeDto("spec-review", "Spec Review", "review", false),
                    new PipelineChainNodeDto("dds-review", "DDS Review", "review", false),
                    new PipelineChainNodeDto("code-review", "Code Review", "review", false)
            ),
            List.of(
                    new PipelineSkillBindingDto("ibm-i-workflow-orchestrator", "IBM i CLI Orchestrator", "requirement"),
                    new PipelineSkillBindingDto("ibm-i-document-quality-gate", "Document Quality Gate", "sdd-document")
            ),
            List.of(
                    new PipelineEntryPathDto("full-chain", "Full Chain", "New RPG/COBOL program or major redesign"),
                    new PipelineEntryPathDto("enhancement", "Enhancement", "Modify existing RPG/COBOL or CL source"),
                    new PipelineEntryPathDto("fast-path", "Fast-Path", "Small, well-understood change with limited impact")
            ),
            List.of(
                    new PipelineDocumentStageDto("requirement-normalizer", "Requirement Normalizer", "docs/01-requirements/{br-id}-normalizer.md", "requirement", "L1", "BR-REQ"),
                    new PipelineDocumentStageDto("functional-spec", "Functional Spec", "docs/02-functional-spec/{br-id}.md", "spec", "L1", "BR-FS"),
                    new PipelineDocumentStageDto("technical-design", "Technical Design", "docs/03-technical-design/{br-id}.md", "design", "L2", "BR-TD"),
                    new PipelineDocumentStageDto("program-spec", "Program Spec", "docs/04-program-spec/{program}.md", "spec", "L2", "BR-PS"),
                    new PipelineDocumentStageDto("file-spec", "File Spec", "docs/05-file-spec/{file}.md", "design", "L2", "BR-FILE"),
                    new PipelineDocumentStageDto("ut-plan", "UT Plan", "docs/06-ut-plan/{program}.md", "test", "L2", "BR-UT"),
                    new PipelineDocumentStageDto("test-scaffold", "Test Scaffold", "docs/07-test-scaffold/{program}.md", "test", "L3", "BR-TEST"),
                    new PipelineDocumentStageDto("spec-review", "Spec Review", "docs/08-reviews/spec/{br-id}.md", "review", "L2", "BR-SR"),
                    new PipelineDocumentStageDto("dds-review", "DDS Review", "docs/08-reviews/dds/{file}.md", "review", "L2", "BR-DR"),
                    new PipelineDocumentStageDto("code-review", "Code Review", "docs/08-reviews/code/{program}.md", "review", "L3", "BR-CR")
            ),
            new PipelineSpecTieringDto(List.of("L1", "L2", "L3"), "L2"),
            true,
            "shared-br"
    );

    private static final Map<String, PipelineProfileDto> PROFILES = Map.of(
            STANDARD_SDD_PROFILE.id(), STANDARD_SDD_PROFILE,
            IBM_I_PROFILE.id(), IBM_I_PROFILE
    );

    public PipelineProfileDto getActiveProfile(String workspace, String application, String project) {
        String workspaceFingerprint = String.join(" ",
                safeLower(workspace),
                safeLower(application),
                safeLower(project));
        if (workspaceFingerprint.contains("ibm")
                || workspaceFingerprint.contains("as400")
                || workspaceFingerprint.contains("rpg")
                || workspaceFingerprint.contains("cobol")) {
            return IBM_I_PROFILE;
        }
        return STANDARD_SDD_PROFILE;
    }

    public PipelineProfileDto getProfile(String profileId) {
        PipelineProfileDto profile = PROFILES.get(profileId);
        if (profile == null) {
            throw new ResourceNotFoundException("Pipeline profile not found: " + profileId);
        }
        return profile;
    }

    private static String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
