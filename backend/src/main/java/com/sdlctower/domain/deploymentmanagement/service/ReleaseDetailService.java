package com.sdlctower.domain.deploymentmanagement.service;

import com.sdlctower.domain.deploymentmanagement.dto.DeploymentDtos.*;
import com.sdlctower.domain.deploymentmanagement.dto.DeploymentEnums.*;
import com.sdlctower.domain.deploymentmanagement.persistence.repository.*;
import com.sdlctower.shared.dto.SectionResultDto;
import org.springframework.stereotype.Service;
import java.util.List;

@Service("deploymentReleaseDetailService")
public class ReleaseDetailService {

    private final ReleaseRepository releaseRepo;
    private final DeployRepository deployRepo;
    private final AiReleaseNotesRepository aiNotesRepo;

    public ReleaseDetailService(ReleaseRepository releaseRepo, DeployRepository deployRepo,
                                 AiReleaseNotesRepository aiNotesRepo) {
        this.releaseRepo = releaseRepo;
        this.deployRepo = deployRepo;
        this.aiNotesRepo = aiNotesRepo;
    }

    public ReleaseDetailAggregateDto loadAggregate(String releaseId) {
        var header = buildHeader(releaseId);
        var deploys = buildDeploys(releaseId);
        var aiNotes = buildAiNotes(releaseId);
        return new ReleaseDetailAggregateDto(header,
                SectionResultDto.ok(List.of()), SectionResultDto.ok(List.of()),
                deploys, aiNotes, null);
    }

    private SectionResultDto<ReleaseHeaderDto> buildHeader(String releaseId) {
        try {
            var r = releaseRepo.findById(releaseId).orElseThrow(() ->
                    new RuntimeException("Release not found: " + releaseId));
            return SectionResultDto.ok(new ReleaseHeaderDto(
                    r.getId(), r.getReleaseVersion(), r.getApplicationId(), "",
                    ReleaseState.valueOf(r.getState()), r.getCreatedAt(), r.getCreatedBy(),
                    new BuildArtifactRefDto(r.getBuildArtifactSliceId(), r.getBuildArtifactId()),
                    r.getBuildArtifactId() != null, null, r.getJenkinsSourceUrl()));
        } catch (Exception e) {
            return SectionResultDto.fail(e.getMessage());
        }
    }

    private SectionResultDto<List<ReleaseDeployRowDto>> buildDeploys(String releaseId) {
        try {
            var deploys = deployRepo.findByReleaseIdOrderByStartedAtDesc(releaseId);
            var rows = deploys.stream().map(d -> new ReleaseDeployRowDto(
                    d.getId(), d.getEnvironmentName(), DeployState.valueOf(d.getState()),
                    d.getStartedAt(), d.getDurationSec(), null, false, d.isRollback())).toList();
            return SectionResultDto.ok(rows);
        } catch (Exception e) {
            return SectionResultDto.fail(e.getMessage());
        }
    }

    private SectionResultDto<AiReleaseNotesDto> buildAiNotes(String releaseId) {
        try {
            var opt = aiNotesRepo.findTopByReleaseIdOrderByGeneratedAtDesc(releaseId);
            if (opt.isEmpty()) {
                return SectionResultDto.ok(new AiReleaseNotesDto(
                        AiRowStatus.PENDING, releaseId, null, null, null, null, null, null, null));
            }
            var n = opt.get();
            return SectionResultDto.ok(new AiReleaseNotesDto(
                    AiRowStatus.valueOf(n.getStatus()), n.getReleaseId(), n.getSkillVersion(),
                    n.getGeneratedAt(), n.getBodyMarkdown(), null, n.getRiskHint(), null, null));
        } catch (Exception e) {
            return SectionResultDto.fail(e.getMessage());
        }
    }
}
