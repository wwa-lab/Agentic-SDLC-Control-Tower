package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.AiNoteCountsDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.AiPrReviewDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.AiPrReviewNoteDto;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.AiNoteSeverity;
import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementEnums.AiRowStatus;
import com.sdlctower.domain.codebuildmanagement.persistence.AiPrReviewEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.AiPrReviewNoteEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.AiPrReviewNoteRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.AiPrReviewRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementPrAiReviewProjection")
public class PrAiReviewProjection {

    private final AiPrReviewRepository aiPrReviewRepository;
    private final AiPrReviewNoteRepository aiPrReviewNoteRepository;

    public PrAiReviewProjection(
            AiPrReviewRepository aiPrReviewRepository,
            AiPrReviewNoteRepository aiPrReviewNoteRepository
    ) {
        this.aiPrReviewRepository = aiPrReviewRepository;
        this.aiPrReviewNoteRepository = aiPrReviewNoteRepository;
    }

    public AiPrReviewDto load(String prId) {
        List<AiPrReviewEntity> reviews = aiPrReviewRepository
                .findByPrIdOrderByGeneratedAtDesc(prId);

        if (reviews.isEmpty()) {
            return null;
        }

        AiPrReviewEntity review = reviews.get(0);
        List<AiPrReviewNoteEntity> noteEntities = aiPrReviewNoteRepository
                .findByReviewId(review.getId());

        List<AiPrReviewNoteDto> noteDtos = noteEntities.stream()
                .map(this::toNoteDto)
                .toList();

        Map<AiNoteSeverity, List<AiPrReviewNoteDto>> notesBySeverity = new LinkedHashMap<>();
        for (AiNoteSeverity severity : AiNoteSeverity.values()) {
            List<AiPrReviewNoteDto> filtered = noteDtos.stream()
                    .filter(note -> note.severity() == severity)
                    .toList();
            if (!filtered.isEmpty()) {
                notesBySeverity.put(severity, filtered);
            }
        }

        AiNoteCountsDto noteCounts = computeNoteCounts(noteDtos);

        return new AiPrReviewDto(
                CodeBuildManagementEnums.parse(AiRowStatus.class, review.getStatus(), "status"),
                review.getHeadSha(),
                review.getSkillVersion(),
                review.getGeneratedAt(),
                noteCounts,
                notesBySeverity,
                review.getErrorJson()
        );
    }

    private AiPrReviewNoteDto toNoteDto(AiPrReviewNoteEntity note) {
        return new AiPrReviewNoteDto(
                note.getId(),
                CodeBuildManagementEnums.parse(AiNoteSeverity.class, note.getSeverity(), "severity"),
                note.getFilePath(),
                note.getStartLine(),
                note.getEndLine(),
                note.getMessage()
        );
    }

    private AiNoteCountsDto computeNoteCounts(List<AiPrReviewNoteDto> notes) {
        int blocker = 0;
        int major = 0;
        int minor = 0;
        int info = 0;
        for (AiPrReviewNoteDto note : notes) {
            switch (note.severity()) {
                case BLOCKER -> blocker++;
                case MAJOR -> major++;
                case MINOR -> minor++;
                case INFO -> info++;
            }
        }
        return new AiNoteCountsDto(blocker, major, minor, info);
    }
}
