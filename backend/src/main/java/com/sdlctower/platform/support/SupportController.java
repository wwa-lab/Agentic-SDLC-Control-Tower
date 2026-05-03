package com.sdlctower.platform.support;

import com.sdlctower.platform.auth.AuthService;
import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SupportController {

    private final SupportService supportService;
    private final AuthService authService;

    public SupportController(SupportService supportService, AuthService authService) {
        this.supportService = supportService;
        this.authService = authService;
    }

    @PostMapping(ApiConstants.SUPPORT_CONTACT)
    public ResponseEntity<ApiResponse<SupportRequestResultDto>> contact(HttpServletRequest servletRequest, @RequestBody SupportRequestDto request) {
        authService.requireUser(servletRequest);
        SupportRequestResultDto result = supportService.contact(request);
        HttpStatus status = "pending".equals(result.status()) ? HttpStatus.ACCEPTED : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(ApiResponse.ok(result));
    }
}
