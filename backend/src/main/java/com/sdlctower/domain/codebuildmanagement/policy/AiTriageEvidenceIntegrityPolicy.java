package com.sdlctower.domain.codebuildmanagement.policy;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.AiTriageRowDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.AiRowStatus;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementAiTriageEvidenceIntegrityPolicy")
public class AiTriageEvidenceIntegrityPolicy {

    public record VerifiedRow(AiTriageRowDto row, AiRowStatus verifiedStatus) {}

    public List<VerifiedRow> verify(List<AiTriageRowDto> rows, Set<String> validStepIds) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        return rows.stream()
                .map(row -> {
                    AiRowStatus status = (row.stepId() != null && validStepIds.contains(row.stepId()))
                            ? row.status()
                            : AiRowStatus.FAILED_EVIDENCE;
                    return new VerifiedRow(row, status);
                })
                .toList();
    }
}
