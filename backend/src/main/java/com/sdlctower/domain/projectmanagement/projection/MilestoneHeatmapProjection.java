package com.sdlctower.domain.projectmanagement.projection;

import com.sdlctower.domain.projectmanagement.ProjectManagementConstants;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PortfolioHeatmapCellDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PortfolioHeatmapDto;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.PortfolioHeatmapRowDto;
import com.sdlctower.domain.projectmanagement.service.ProjectManagementMapper;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.projectspace.persistence.MilestoneRepository;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class MilestoneHeatmapProjection {

    private final ProjectSpaceSeedCatalog projectSeedCatalog;
    private final MilestoneRepository milestoneRepository;
    private final ProjectManagementMapper mapper;

    public MilestoneHeatmapProjection(
            ProjectSpaceSeedCatalog projectSeedCatalog,
            MilestoneRepository milestoneRepository,
            ProjectManagementMapper mapper
    ) {
        this.projectSeedCatalog = projectSeedCatalog;
        this.milestoneRepository = milestoneRepository;
        this.mapper = mapper;
    }

    public PortfolioHeatmapDto load(String workspaceId, String window) {
        LocalDate now = ProjectManagementConstants.REFERENCE_NOW.atZone(java.time.ZoneOffset.UTC).toLocalDate();
        WeekFields weekFields = WeekFields.of(Locale.ROOT);
        List<String> columns = new ArrayList<>();
        List<LocalDate> starts = new ArrayList<>();
        for (int offset = -1; offset <= 2; offset++) {
            LocalDate start = now.plusWeeks(offset).with(weekFields.dayOfWeek(), 1);
            starts.add(start);
            columns.add(start.getYear() + "-W" + start.get(weekFields.weekOfWeekBasedYear()));
        }

        List<PortfolioHeatmapRowDto> rows = projectSeedCatalog.projectsForWorkspace(workspaceId).stream()
                .map(project -> {
                    var milestones = milestoneRepository.findByProjectIdOrderByTargetDateAsc(project.id());
                    List<PortfolioHeatmapCellDto> cells = new ArrayList<>();
                    for (int index = 0; index < starts.size(); index++) {
                        LocalDate start = starts.get(index);
                        LocalDate end = start.plusDays(6);
                        String dominant = ProjectManagementProjectionSupport.dominantMilestoneStatus(
                                milestones.stream()
                                        .filter(milestone -> !milestone.getTargetDate().isBefore(start) && !milestone.getTargetDate().isAfter(end))
                                        .map(mapper::milestoneState)
                                        .toList()
                        );
                        cells.add(new PortfolioHeatmapCellDto(columns.get(index), dominant));
                    }
                    return new PortfolioHeatmapRowDto(project.id(), project.name(), cells);
                })
                .toList();

        return new PortfolioHeatmapDto(window == null ? "WEEK" : window, columns, rows);
    }
}
