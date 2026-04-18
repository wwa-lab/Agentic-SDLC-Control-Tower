package com.sdlctower.domain.projectmanagement.projection;

import com.sdlctower.domain.projectmanagement.ProjectManagementConstants;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.ChangeLogPageDto;
import com.sdlctower.domain.projectmanagement.persistence.PlanChangeLogEntryRepository;
import com.sdlctower.domain.projectmanagement.service.ProjectManagementMapper;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PlanChangeLogProjection {

    private final PlanChangeLogEntryRepository repository;
    private final ProjectManagementMapper mapper;

    public PlanChangeLogProjection(PlanChangeLogEntryRepository repository, ProjectManagementMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public ChangeLogPageDto load(
            String projectId,
            String actorType,
            String targetType,
            Instant from,
            Instant to,
            Integer page,
            Integer pageSize
    ) {
        int safePage = page == null ? 0 : Math.max(page, 0);
        int safePageSize = pageSize == null
                ? ProjectManagementConstants.DEFAULT_CHANGE_LOG_PAGE_SIZE
                : Math.min(ProjectManagementConstants.MAX_CHANGE_LOG_PAGE_SIZE, Math.max(1, pageSize));

        List<com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.ChangeLogEntryDto> filtered = repository
                .findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .filter(entry -> actorType == null || actorType.equalsIgnoreCase(entry.getActorType()))
                .filter(entry -> targetType == null || targetType.equalsIgnoreCase(entry.getTargetType()))
                .filter(entry -> from == null || !entry.getCreatedAt().isBefore(from))
                .filter(entry -> to == null || !entry.getCreatedAt().isAfter(to))
                .map(mapper::toChangeLogEntryDto)
                .toList();

        int fromIndex = Math.min(filtered.size(), safePage * safePageSize);
        int toIndex = Math.min(filtered.size(), fromIndex + safePageSize);
        return new ChangeLogPageDto(filtered.subList(fromIndex, toIndex), safePage, filtered.size());
    }
}
