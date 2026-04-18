package com.sdlctower.domain.designmanagement.service;

import com.sdlctower.domain.designmanagement.dto.DesignManagementEnums.CoverageDeclaration;
import com.sdlctower.domain.designmanagement.dto.DesignManagementEnums.CoverageStatus;
import com.sdlctower.shared.integration.requirement.SpecRevisionLookup.SpecRevisionInfo;
import java.util.Collection;
import java.util.Comparator;
import org.springframework.stereotype.Service;

@Service
public class CoverageService {

    public CoverageStatus compute(String declaredCoverage, int coversRevision, SpecRevisionInfo spec) {
        if (spec == null) {
            return CoverageStatus.UNKNOWN;
        }
        String specState = spec.state().toUpperCase();
        if ("ARCHIVED".equals(specState) || "DELETED".equals(specState)) {
            return CoverageStatus.MISSING;
        }
        if (CoverageDeclaration.PARTIAL.name().equalsIgnoreCase(declaredCoverage)) {
            return CoverageStatus.PARTIAL;
        }
        if (coversRevision < spec.latestRevision()) {
            return CoverageStatus.STALE;
        }
        return CoverageStatus.OK;
    }

    public String explain(String declaredCoverage, int coversRevision, SpecRevisionInfo spec) {
        CoverageStatus status = compute(declaredCoverage, coversRevision, spec);
        return switch (status) {
            case UNKNOWN -> "UNKNOWN: linked spec could not be resolved";
            case MISSING -> "MISSING: spec is archived or deleted upstream";
            case PARTIAL -> "PARTIAL: link is explicitly marked as partial coverage";
            case STALE -> "STALE: link covers spec revision " + coversRevision + "; latest is " + spec.latestRevision();
            case OK -> "OK: link covers latest spec revision " + spec.latestRevision();
        };
    }

    public CoverageStatus worstStatus(Collection<CoverageStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return CoverageStatus.MISSING;
        }
        return statuses.stream()
                .max(Comparator.comparingInt(this::weight))
                .orElse(CoverageStatus.UNKNOWN);
    }

    private int weight(CoverageStatus status) {
        return switch (status) {
            case MISSING -> 5;
            case STALE -> 4;
            case PARTIAL -> 3;
            case UNKNOWN -> 2;
            case OK -> 1;
        };
    }
}
