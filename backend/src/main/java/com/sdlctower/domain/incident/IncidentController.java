package com.sdlctower.domain.incident;

import com.sdlctower.domain.incident.dto.ActionApprovalRequestDto;
import com.sdlctower.domain.incident.dto.ActionApprovalResultDto;
import com.sdlctower.domain.incident.dto.IncidentDetailDto;
import com.sdlctower.domain.incident.dto.IncidentListDto;
import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping(ApiConstants.INCIDENTS)
    public ApiResponse<IncidentListDto> listIncidents() {
        return ApiResponse.ok(incidentService.getIncidentList());
    }

    @GetMapping(ApiConstants.INCIDENT_DETAIL)
    public ApiResponse<IncidentDetailDto> getIncidentDetail(@PathVariable String incidentId) {
        return ApiResponse.ok(incidentService.getIncidentDetail(incidentId));
    }

    @PostMapping(ApiConstants.INCIDENT_ACTION_APPROVE)
    public ApiResponse<ActionApprovalResultDto> approveAction(
            @PathVariable String incidentId,
            @PathVariable String actionId
    ) {
        return ApiResponse.ok(incidentService.approveAction(incidentId, actionId));
    }

    @PostMapping(ApiConstants.INCIDENT_ACTION_REJECT)
    public ApiResponse<ActionApprovalResultDto> rejectAction(
            @PathVariable String incidentId,
            @PathVariable String actionId,
            @RequestBody(required = false) ActionApprovalRequestDto body
    ) {
        String reason = (body != null) ? body.reason() : null;
        return ApiResponse.ok(incidentService.rejectAction(incidentId, actionId, reason));
    }
}
