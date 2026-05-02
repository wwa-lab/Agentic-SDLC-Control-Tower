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
            "Standard SDD synced from .claude/skills with 10 staged skills; Architecture and Design supporting docs stay in document flow",
            List.of(
                    new PipelineChainNodeDto("requirement", "Requirement", "requirement", false),
                    new PipelineChainNodeDto("user-story", "User Story", "user-story", false),
                    new PipelineChainNodeDto("spec", "Spec", "spec", true),
                    new PipelineChainNodeDto("architecture", "Architecture", "architecture", false),
                    new PipelineChainNodeDto("design", "Design", "design", false),
                    new PipelineChainNodeDto("tasks", "Tasks", "tasks", false),
                    new PipelineChainNodeDto("code", "Code", "code", false),
                    new PipelineChainNodeDto("review", "Review", "review", false)
            ),
            List.of(
                    new PipelineSkillBindingDto("req-to-user-story", "Requirement to Stories", "requirement"),
                    new PipelineSkillBindingDto("user-story-to-spec", "Stories to Spec", "user-story"),
                    new PipelineSkillBindingDto("spec-to-architecture", "Spec to Architecture", "spec"),
                    new PipelineSkillBindingDto("architecture-review", "Architecture Review", "architecture"),
                    new PipelineSkillBindingDto("architecture-to-design", "Architecture to Design", "architecture"),
                    new PipelineSkillBindingDto("design-to-tasks", "Design to Tasks", "design"),
                    new PipelineSkillBindingDto("tasks-to-code", "Tasks to Code", "tasks"),
                    new PipelineSkillBindingDto("tasks-to-implementation", "Tasks to Implementation", "tasks"),
                    new PipelineSkillBindingDto("review-code-against-design", "Code vs Design Review", "code"),
                    new PipelineSkillBindingDto("review-doc-quality", "Document Quality Review", "sdd-document")
            ),
            List.of(
                    new PipelineEntryPathDto("standard", "Standard", "Single entry path through requirement capture")
            ),
            List.of(
                    new PipelineDocumentStageDto("requirement", "Requirement", "docs/01-requirements/{slug}-requirements.md", "requirement", null, "REQ"),
                    new PipelineDocumentStageDto("user-story", "User Stories", "docs/02-user-stories/{slug}-stories.md", "user-story", null, "US"),
                    new PipelineDocumentStageDto("spec", "Spec", "docs/03-spec/{slug}-spec.md", "spec", null, "SPEC"),
                    new PipelineDocumentStageDto("architecture", "Architecture", "docs/04-architecture/{slug}-architecture.md", "architecture", null, "ARCH"),
                    new PipelineDocumentStageDto("data-flow", "Data Flow", "docs/04-architecture/{slug}-data-flow.md", "architecture-support", null, "FLOW"),
                    new PipelineDocumentStageDto("data-model", "Data Model", "docs/04-architecture/{slug}-data-model.md", "architecture-support", null, "MODEL"),
                    new PipelineDocumentStageDto("design", "Design", "docs/05-design/{slug}-design.md", "design", null, "DESIGN"),
                    new PipelineDocumentStageDto("api-guide", "API Implementation Guide", "docs/05-design/contracts/{slug}-API_IMPLEMENTATION_GUIDE.md", "design-support", null, "API"),
                    new PipelineDocumentStageDto("tasks", "Tasks", "docs/06-tasks/{slug}-tasks.md", "tasks", null, "TASK")
            ),
            null,
            false,
            "per-layer"
    );

    private static final PipelineProfileDto IBM_I_PROFILE = new PipelineProfileDto(
            "ibm-i",
            "IBM i",
            "IBM i pipeline synced from build-agent-skill with 16 visible skills, V2.x DDS/File/Program updates, L1/L2/L3 tiering, shared-br traceability",
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
                    new PipelineSkillBindingDto("ibm-i-requirement-normalizer", "Requirement Normalizer", "raw-input"),
                    new PipelineSkillBindingDto("ibm-i-program-analyzer", "Program Analyzer", "existing-source"),
                    new PipelineSkillBindingDto("ibm-i-impact-analyzer", "Impact Analyzer", "program-analysis"),
                    new PipelineSkillBindingDto("ibm-i-functional-spec", "Functional Spec Generator", "requirement-normalizer"),
                    new PipelineSkillBindingDto("ibm-i-technical-design", "Technical Design Generator", "functional-spec"),
                    new PipelineSkillBindingDto("ibm-i-program-spec", "Program Spec Generator", "technical-design"),
                    new PipelineSkillBindingDto("ibm-i-file-spec", "File Spec Generator", "technical-design"),
                    new PipelineSkillBindingDto("ibm-i-code-generator", "Code Generator", "program-spec"),
                    new PipelineSkillBindingDto("ibm-i-dds-generator", "DDS Generator", "file-spec"),
                    new PipelineSkillBindingDto("ibm-i-ut-plan-generator", "UT Plan Generator", "program-spec"),
                    new PipelineSkillBindingDto("ibm-i-test-scaffold", "Test Scaffold Generator", "ut-plan"),
                    new PipelineSkillBindingDto("ibm-i-compile-precheck", "Compile Precheck", "generated-code"),
                    new PipelineSkillBindingDto("ibm-i-spec-reviewer", "Spec Reviewer", "functional-spec"),
                    new PipelineSkillBindingDto("ibm-i-dds-reviewer", "DDS Reviewer", "dds-source"),
                    new PipelineSkillBindingDto("ibm-i-code-reviewer", "Code Reviewer", "generated-code"),
                    new PipelineSkillBindingDto("ibm-i-workflow-orchestrator", "Workflow Orchestrator", "any-stage")
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
