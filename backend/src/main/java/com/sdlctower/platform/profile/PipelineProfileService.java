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
            "Spec-Driven Development pipeline with 11-node chain, 2 skills, per-layer traceability",
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
                    new PipelineSkillBindingDto("req-to-user-story", "Generate Stories", "requirement"),
                    new PipelineSkillBindingDto("user-story-to-spec", "Generate Spec", "user-story")
            ),
            List.of(
                    new PipelineEntryPathDto("standard", "Standard", "Single entry path through requirement capture")
            ),
            null,
            false,
            "per-layer"
    );

    private static final PipelineProfileDto IBM_I_PROFILE = new PipelineProfileDto(
            "ibm-i",
            "IBM i",
            "IBM i pipeline with single orchestrator skill, L1/L2/L3 tiering, shared-br traceability",
            List.of(
                    new PipelineChainNodeDto("req", "Requirement", "requirement", false),
                    new PipelineChainNodeDto("story", "User Story", "user-story", false),
                    new PipelineChainNodeDto("spec", "Spec", "spec", true),
                    new PipelineChainNodeDto("arch", "Architecture", "architecture", false),
                    new PipelineChainNodeDto("design", "Design", "design", false),
                    new PipelineChainNodeDto("tasks", "Tasks", "tasks", false),
                    new PipelineChainNodeDto("code", "Code", "code", false),
                    new PipelineChainNodeDto("build", "Build", "code", false),
                    new PipelineChainNodeDto("test", "Test", "test", false),
                    new PipelineChainNodeDto("deploy", "Deploy", "deploy", false)
            ),
            List.of(
                    new PipelineSkillBindingDto("ibm-i-workflow-orchestrator", "Send to Orchestrator", "requirement")
            ),
            List.of(
                    new PipelineEntryPathDto("new-program", "New Program", "Create new RPG/COBOL program"),
                    new PipelineEntryPathDto("modification", "Modification", "Modify existing program"),
                    new PipelineEntryPathDto("conversion", "Conversion", "Convert from legacy format")
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
