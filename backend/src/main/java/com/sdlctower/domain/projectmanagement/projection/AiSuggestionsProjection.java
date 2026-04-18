package com.sdlctower.domain.projectmanagement.projection;

import com.sdlctower.domain.projectmanagement.ProjectManagementConstants;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.AiSuggestionDto;
import com.sdlctower.domain.projectmanagement.persistence.AiSuggestionRepository;
import com.sdlctower.domain.projectmanagement.service.ProjectManagementMapper;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiSuggestionsProjection {

    private final AiSuggestionRepository repository;
    private final ProjectManagementMapper mapper;

    public AiSuggestionsProjection(AiSuggestionRepository repository, ProjectManagementMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<AiSuggestionDto> load(String projectId, String state) {
        List<com.sdlctower.domain.projectmanagement.persistence.AiSuggestionEntity> suggestions = state == null
                ? repository.findByProjectIdAndStateOrderByCreatedAtDesc(projectId, "PENDING")
                : repository.findByProjectIdAndStateOrderByCreatedAtDesc(projectId, state.toUpperCase());

        return suggestions.stream()
                .filter(suggestion -> suggestion.getSuppressUntil() == null || !suggestion.getSuppressUntil().isAfter(ProjectManagementConstants.REFERENCE_NOW))
                .map(mapper::toAiSuggestionDto)
                .toList();
    }
}
