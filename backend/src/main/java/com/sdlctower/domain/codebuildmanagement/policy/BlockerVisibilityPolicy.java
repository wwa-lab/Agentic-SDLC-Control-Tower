package com.sdlctower.domain.codebuildmanagement.policy;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.AiNoteCountsDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.AiPrReviewDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.AiPrReviewNoteDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.AiNoteSeverity;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementBlockerVisibilityPolicy")
public class BlockerVisibilityPolicy {

    private static final String REDACTED_MESSAGE = "[Restricted — requires PM or Tech Lead role]";

    public AiPrReviewDto filter(AiPrReviewDto review, boolean canSeeBlockerBodies) {
        if (canSeeBlockerBodies || review == null) {
            return review;
        }

        Map<AiNoteSeverity, List<AiPrReviewNoteDto>> filteredNotes = new EnumMap<>(AiNoteSeverity.class);

        if (review.notesBySeverity() != null) {
            for (Map.Entry<AiNoteSeverity, List<AiPrReviewNoteDto>> entry : review.notesBySeverity().entrySet()) {
                if (entry.getKey() == AiNoteSeverity.BLOCKER) {
                    List<AiPrReviewNoteDto> redacted = entry.getValue().stream()
                            .map(note -> new AiPrReviewNoteDto(
                                    note.id(),
                                    note.severity(),
                                    note.filePath(),
                                    note.startLine(),
                                    note.endLine(),
                                    REDACTED_MESSAGE))
                            .toList();
                    filteredNotes.put(entry.getKey(), redacted);
                } else {
                    filteredNotes.put(entry.getKey(), entry.getValue());
                }
            }
        }

        AiNoteCountsDto counts = review.noteCounts() != null
                ? review.noteCounts()
                : new AiNoteCountsDto(0, 0, 0, 0);

        return new AiPrReviewDto(
                review.status(),
                review.keyedOnSha(),
                review.skillVersion(),
                review.generatedAt(),
                counts,
                filteredNotes,
                review.error());
    }
}
