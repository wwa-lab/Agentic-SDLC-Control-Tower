package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.AiTriageDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.AiTriageRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.AiRowStatus;
import com.sdlctower.domain.codebuildmanagement.persistence.AiTriageRowEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.AiTriageRowRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineJobEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineJobRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementRunAiTriageProjection")
public class RunAiTriageProjection {

    private final AiTriageRowRepository aiTriageRowRepository;
    private final PipelineJobRepository pipelineJobRepository;

    public RunAiTriageProjection(
            AiTriageRowRepository aiTriageRowRepository,
            PipelineJobRepository pipelineJobRepository
    ) {
        this.aiTriageRowRepository = aiTriageRowRepository;
        this.pipelineJobRepository = pipelineJobRepository;
    }

    public AiTriageDto load(String runId) {
        List<AiTriageRowEntity> rows = aiTriageRowRepository
                .findByRunIdOrderByGeneratedAtDesc(runId);

        if (rows.isEmpty()) {
            return new AiTriageDto(
                    AiRowStatus.PENDING,
                    null,
                    null,
                    List.of(),
                    null
            );
        }

        String skillVersion = rows.get(0).getSkillVersion();
        AiRowStatus overallStatus = resolveOverallStatus(rows);

        List<AiTriageRowDto> rowDtos = rows.stream()
                .map(this::toRowDto)
                .toList();

        return new AiTriageDto(
                overallStatus,
                skillVersion,
                rows.get(0).getGeneratedAt(),
                rowDtos,
                null
        );
    }

    private AiTriageRowDto toRowDto(AiTriageRowEntity entity) {
        String jobId = resolveJobId(entity);

        return new AiTriageRowDto(
                entity.getId(),
                CodeBuildManagementEnums.parse(AiRowStatus.class, entity.getStatus(), "status"),
                entity.getRunId(),
                jobId,
                entity.getStepId(),
                entity.getLikelyCause(),
                parseJsonList(entity.getCandidateOwnersJson()),
                entity.getConfidence(),
                parseJsonList(entity.getEvidenceJson()),
                entity.getErrorJson()
        );
    }

    private AiRowStatus resolveOverallStatus(List<AiTriageRowEntity> rows) {
        boolean anyFailed = rows.stream()
                .anyMatch(row -> "FAILED".equals(row.getStatus()));
        boolean anyPending = rows.stream()
                .anyMatch(row -> "PENDING".equals(row.getStatus()));
        if (anyFailed) {
            return AiRowStatus.FAILED;
        }
        if (anyPending) {
            return AiRowStatus.PENDING;
        }
        return AiRowStatus.SUCCESS;
    }

    private String resolveJobId(AiTriageRowEntity entity) {
        if (entity.getStepId() == null) {
            return null;
        }
        List<PipelineJobEntity> jobs = pipelineJobRepository
                .findByRunIdOrderByJobNumberAsc(entity.getRunId());
        // For V1, we don't have a direct step-to-job link in the entity,
        // so return the first job's ID as a placeholder
        return jobs.isEmpty() ? null : jobs.get(0).getId();
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        // Simple JSON array parsing for V1 seed data: ["a","b","c"]
        String trimmed = json.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String inner = trimmed.substring(1, trimmed.length() - 1).trim();
            if (inner.isEmpty()) {
                return List.of();
            }
            return Arrays.stream(inner.split(","))
                    .map(String::trim)
                    .map(s -> s.startsWith("\"") && s.endsWith("\"") ? s.substring(1, s.length() - 1) : s)
                    .toList();
        }
        return List.of(trimmed);
    }
}
