package com.sdlctower.domain.designmanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public final class DesignManagementCommands {

    private DesignManagementCommands() {}

    public record RegisterArtifactRequest(
            @NotBlank String projectId,
            @NotBlank String title,
            @NotBlank String format,
            @NotBlank String lifecycle,
            @NotBlank String htmlPayload,
            String changeLogNote,
            List<String> authorIds
    ) {}

    public record PublishVersionRequest(
            @NotBlank String prevVersionId,
            @NotBlank String htmlPayload,
            String changeLogNote
    ) {}

    public record RetireArtifactRequest(
            @NotBlank String prevVersionId,
            @NotBlank String reason
    ) {}

    public record UpdateMetadataRequest(
            String title,
            String format,
            List<String> authorIds
    ) {}

    public record LinkSpecRequest(
            @NotBlank String specId,
            @NotBlank String declaredCoverage,
            @NotNull Integer coversRevision
    ) {}

    public record LinkSpecsRequest(
            @Valid @NotEmpty List<LinkSpecRequest> links
    ) {}

    public record UnlinkSpecRequest(
            @NotBlank String reason
    ) {}

    public record RegenerateAiSummaryRequest(
            String skillVersion
    ) {}
}
