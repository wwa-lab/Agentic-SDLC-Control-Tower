package com.sdlctower.domain.projectmanagement.service;

import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.CounterSignDependencyRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.CreateDependencyRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.TransitionDependencyRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementCommands.UpdateDependencyRequest;
import com.sdlctower.domain.projectmanagement.dto.ProjectManagementDtos.DependencyDto;
import com.sdlctower.domain.projectmanagement.events.PlanChangeLogPublisher;
import com.sdlctower.domain.projectmanagement.policy.DependencyCounterSignPolicy;
import com.sdlctower.domain.projectmanagement.policy.PlanPolicy;
import com.sdlctower.domain.projectmanagement.policy.ProjectManagementException;
import com.sdlctower.domain.projectmanagement.policy.RevisionFencingPolicy;
import com.sdlctower.domain.projectmanagement.policy.TransitionPolicy;
import com.sdlctower.domain.projectspace.ProjectSpaceSeedCatalog;
import com.sdlctower.domain.projectspace.persistence.ProjectDependencyEntity;
import com.sdlctower.domain.projectspace.persistence.ProjectDependencyRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DependencyCommandService {

    private final ProjectDependencyRepository dependencyRepository;
    private final ProjectSpaceSeedCatalog projectSeedCatalog;
    private final RevisionFencingPolicy revisionFencingPolicy;
    private final TransitionPolicy transitionPolicy;
    private final DependencyCounterSignPolicy dependencyCounterSignPolicy;
    private final PlanPolicy planPolicy;
    private final ProjectManagementMapper mapper;
    private final PlanChangeLogPublisher changeLogPublisher;

    public DependencyCommandService(
            ProjectDependencyRepository dependencyRepository,
            ProjectSpaceSeedCatalog projectSeedCatalog,
            RevisionFencingPolicy revisionFencingPolicy,
            TransitionPolicy transitionPolicy,
            DependencyCounterSignPolicy dependencyCounterSignPolicy,
            PlanPolicy planPolicy,
            ProjectManagementMapper mapper,
            PlanChangeLogPublisher changeLogPublisher
    ) {
        this.dependencyRepository = dependencyRepository;
        this.projectSeedCatalog = projectSeedCatalog;
        this.revisionFencingPolicy = revisionFencingPolicy;
        this.transitionPolicy = transitionPolicy;
        this.dependencyCounterSignPolicy = dependencyCounterSignPolicy;
        this.planPolicy = planPolicy;
        this.mapper = mapper;
        this.changeLogPublisher = changeLogPublisher;
    }

    @Transactional
    public DependencyDto create(String projectId, CreateDependencyRequest request) {
        Instant now = Instant.now();
        boolean external = request.targetProjectId() == null || request.targetProjectId().isBlank();
        String targetName = external
                ? request.targetRef()
                : projectSeedCatalog.project(request.targetProjectId()).name();
        long newRevision = revisionFencingPolicy.bump(projectId);
        ProjectDependencyEntity entity = ProjectDependencyEntity.create(
                "dep-" + UUID.randomUUID(),
                projectId,
                targetName,
                request.targetRef(),
                request.targetProjectId(),
                request.direction().toUpperCase(),
                request.relationship().toUpperCase(),
                request.ownerTeam(),
                request.blockerReason() == null || request.blockerReason().isBlank() ? "GREEN" : "YELLOW",
                request.blockerReason(),
                external,
                now,
                now
        );
        entity.setPmState("PROPOSED");
        entity.setPlanRevisionAtUpdate(newRevision);
        dependencyRepository.save(entity);
        DependencyDto after = mapper.toDependencyDto(entity, newRevision);
        changeLogPublisher.publish(projectId, "CREATE", "DEPENDENCY", entity.getId(), null, after, null);
        return after;
    }

    @Transactional
    public DependencyDto update(String projectId, String dependencyId, UpdateDependencyRequest request) {
        ProjectDependencyEntity entity = dependencyRepository.findBySourceProjectIdAndId(projectId, dependencyId)
                .orElseThrow(() -> ProjectManagementException.notFound("PM_NOT_FOUND", "Dependency " + dependencyId + " not found"));
        long currentRevision = revisionFencingPolicy.currentRevision(projectId);
        revisionFencingPolicy.check(projectId, request.planRevision());
        DependencyDto before = mapper.toDependencyDto(entity, currentRevision);
        if (request.ownerTeam() != null) {
            entity.setOwnerTeam(request.ownerTeam());
        }
        if (request.blockerReason() != null) {
            entity.setBlockerReason(request.blockerReason());
            entity.setHealth(request.blockerReason().isBlank() ? "GREEN" : "YELLOW");
        }
        long newRevision = revisionFencingPolicy.bump(projectId);
        entity.setPlanRevisionAtUpdate(newRevision);
        entity.setUpdatedAt(Instant.now());
        dependencyRepository.save(entity);
        DependencyDto after = mapper.toDependencyDto(entity, newRevision);
        changeLogPublisher.publish(projectId, "UPDATE", "DEPENDENCY", dependencyId, before, after, null);
        return after;
    }

    @Transactional
    public DependencyDto transition(String projectId, String dependencyId, TransitionDependencyRequest request) {
        ProjectDependencyEntity entity = dependencyRepository.findBySourceProjectIdAndId(projectId, dependencyId)
                .orElseThrow(() -> ProjectManagementException.notFound("PM_NOT_FOUND", "Dependency " + dependencyId + " not found"));
        long currentRevision = revisionFencingPolicy.currentRevision(projectId);
        revisionFencingPolicy.check(projectId, request.planRevision());
        transitionPolicy.dependency(mapper.dependencyState(entity), request.to());
        transitionPolicy.requireDependencyRejectionReason(request.to(), request.rejectionReason());
        transitionPolicy.requireDependencyCommitment(entity.isExternal(), request.to(), request.contractCommitment());
        if (!entity.isExternal() && "APPROVED".equalsIgnoreCase(request.to())) {
            throw ProjectManagementException.conflict(
                    "PM_DEP_COUNTERSIGN_REQUIRED",
                    "Internal dependency approval requires target-side counter-sign"
            );
        }

        DependencyDto before = mapper.toDependencyDto(entity, currentRevision);
        String targetState = request.to().toUpperCase();
        entity.setPmState(targetState);
        entity.setRejectionReason(request.rejectionReason());
        entity.setContractCommitment(request.contractCommitment());
        entity.setHealth(switch (targetState) {
            case "APPROVED", "RESOLVED" -> "GREEN";
            case "REJECTED" -> "RED";
            default -> "YELLOW";
        });
        long newRevision = revisionFencingPolicy.bump(projectId);
        entity.setPlanRevisionAtUpdate(newRevision);
        entity.setUpdatedAt(Instant.now());
        dependencyRepository.save(entity);
        DependencyDto after = mapper.toDependencyDto(entity, newRevision);
        changeLogPublisher.publish(projectId, "TRANSITION", "DEPENDENCY", dependencyId, before, after, request.rejectionReason());
        return after;
    }

    @Transactional
    public DependencyDto counterSign(String projectId, String dependencyId, CounterSignDependencyRequest request) {
        ProjectDependencyEntity entity = dependencyRepository.findBySourceProjectIdAndId(projectId, dependencyId)
                .orElseThrow(() -> ProjectManagementException.notFound("PM_NOT_FOUND", "Dependency " + dependencyId + " not found"));
        if (entity.getTargetProjectId() == null || entity.getTargetProjectId().isBlank()) {
            throw ProjectManagementException.invalid(
                    "PM_DEP_COUNTERSIGN_REQUIRED",
                    "Only internal dependencies can be counter-signed"
            );
        }
        revisionFencingPolicy.check(projectId, request.planRevision());
        dependencyCounterSignPolicy.requireCounterSignAuthority(entity.getTargetProjectId());

        long currentRevision = revisionFencingPolicy.currentRevision(projectId);
        DependencyDto before = mapper.toDependencyDto(entity, currentRevision);
        long sourceRevision = revisionFencingPolicy.bump(projectId);
        long targetRevision = revisionFencingPolicy.bump(entity.getTargetProjectId());

        entity.setPmState("APPROVED");
        entity.setHealth("GREEN");
        entity.setCounterSignedBy(planPolicy.currentActor().memberId());
        entity.setCounterSignedAt(Instant.now());
        entity.setPlanRevisionAtUpdate(sourceRevision);
        entity.setUpdatedAt(Instant.now());
        dependencyRepository.save(entity);

        DependencyDto after = mapper.toDependencyDto(entity, sourceRevision);
        changeLogPublisher.publish(projectId, "COUNTERSIGN", "DEPENDENCY", dependencyId, before, after, null);
        changeLogPublisher.publish(
                entity.getTargetProjectId(),
                "COUNTERSIGN",
                "DEPENDENCY",
                dependencyId,
                null,
                mapper.toDependencyDto(entity, targetRevision),
                "Counter-sign mirrored from source project " + projectId
        );
        return after;
    }
}
