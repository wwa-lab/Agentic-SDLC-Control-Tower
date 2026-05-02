package com.sdlctower.platform.auth;

import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping(ApiConstants.AUTH_PROVIDERS)
    public ApiResponse<List<AuthProviderDto>> providers() {
        return ApiResponse.ok(authService.providers());
    }

    @PostMapping(ApiConstants.AUTH_LOGIN)
    public ResponseEntity<ApiResponse<CurrentUserDto>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return withSession(authService.login(request, servletRequest.getRemoteAddr()), HttpStatus.OK);
    }

    @PostMapping(ApiConstants.AUTH_GUEST)
    public ResponseEntity<ApiResponse<CurrentUserDto>> guest() {
        return withSession(authService.guest(), HttpStatus.OK);
    }

    @GetMapping(ApiConstants.AUTH_ME)
    public ResponseEntity<ApiResponse<CurrentUserDto>> me(HttpServletRequest request) {
        return authService.fromRequest(request)
                .map(user -> ResponseEntity.ok(ApiResponse.ok(user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("UNAUTHENTICATED")));
    }

    @PostMapping(ApiConstants.AUTH_LOGOUT)
    public ResponseEntity<ApiResponse<LoggedOutDto>> logout() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredCookie().toString())
                .body(ApiResponse.ok(new LoggedOutDto(true)));
    }

    @GetMapping(ApiConstants.AUTH_TEAMBOOK_START)
    public ResponseEntity<Void> teambookStart() {
        if (!authService.isTeamBookEnabled()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/login?error=teambook_stub"))
                .build();
    }

    @GetMapping(ApiConstants.AUTH_TEAMBOOK_CALLBACK)
    public ResponseEntity<ApiResponse<CurrentUserDto>> teambookCallback(
            @RequestParam(defaultValue = "43910000") String staffId,
            @RequestParam(required = false) String staffName,
            @RequestParam(required = false) String avatarUrl
    ) {
        return withSession(authService.teambookCallback(staffId, staffName, avatarUrl), HttpStatus.OK);
    }

    private ResponseEntity<ApiResponse<CurrentUserDto>> withSession(CurrentUserDto user, HttpStatus status) {
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, sessionCookie(authService.token(user)).toString())
                .body(ApiResponse.ok(user));
    }

    private ResponseCookie sessionCookie(String token) {
        return ResponseCookie.from(AuthService.COOKIE_NAME, token)
                .httpOnly(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(authService.sessionTtlSeconds()))
                .build();
    }

    private ResponseCookie expiredCookie() {
        return ResponseCookie.from(AuthService.COOKIE_NAME, "")
                .httpOnly(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }
}
