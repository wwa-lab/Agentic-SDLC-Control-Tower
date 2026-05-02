package com.sdlctower.platform.auth;

import com.sdlctower.platform.access.PlatformAccessService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public static final String COOKIE_NAME = "sdlc_session";

    private final AuthProperties properties;
    private final PlatformAccessService accessService;
    private final Map<String, FailedLoginState> failedLogins = new ConcurrentHashMap<>();

    public AuthService(AuthProperties properties, PlatformAccessService accessService) {
        this.properties = properties;
        this.accessService = accessService;
    }

    public List<AuthProviderDto> providers() {
        return List.of(
                new AuthProviderDto("teambook", "TeamBook SSO", properties.isTeamBookEnabled(), "/api/v1/auth/sso/teambook/start"),
                new AuthProviderDto("manual", "Staff ID", true, null),
                new AuthProviderDto("guest", "Guest", true, null)
        );
    }

    public CurrentUserDto login(LoginRequest request) {
        return login(request, "unknown");
    }

    public CurrentUserDto login(LoginRequest request, String source) {
        String key = loginKey(request.staffId(), source);
        if (isRateLimited(key)) {
            throw new PlatformAuthException(HttpStatus.TOO_MANY_REQUESTS, "LOGIN_RATE_LIMITED");
        }
        if (!accessService.isActiveUser(request.staffId())) {
            recordFailedLogin(key);
            throw new IllegalArgumentException("User is not provisioned or active");
        }
        failedLogins.remove(key);
        return accessService.currentUser(request.staffId(), "manual");
    }

    public CurrentUserDto guest() {
        return new CurrentUserDto(
                "guest",
                "guest",
                null,
                "Guest",
                null,
                null,
                List.of("GUEST"),
                true,
                List.of(new ScopeDto("demo", "public-demo"))
        );
    }

    public CurrentUserDto teambookCallback(String staffId, String staffName, String avatarUrl) {
        if (!properties.isTeamBookEnabled()) {
            throw new IllegalStateException("TeamBook provider is disabled");
        }
        return accessService.currentUser(staffId, "teambook");
    }

    public boolean isTeamBookEnabled() {
        return properties.isTeamBookEnabled();
    }

    public Optional<CurrentUserDto> fromRequest(HttpServletRequest request) {
        String token = readCookie(request);
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String[] parts = token.split("\\.", 2);
        if (parts.length != 2 || !sign(parts[0]).equals(parts[1])) {
            return Optional.empty();
        }
        String payload;
        String[] fields;
        try {
            payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            fields = payload.split("\\|", -1);
            if (fields.length != 4) {
                return Optional.empty();
            }
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
        long expiresAt;
        try {
            expiresAt = Long.parseLong(fields[3]);
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
        if (Instant.now().getEpochSecond() > expiresAt) {
            return Optional.empty();
        }
        String mode = fields[0];
        String authProvider = fields[1];
        String staffId = fields[2].isBlank() ? null : fields[2];
        if ("guest".equals(mode)) {
            return Optional.of(guest());
        }
        if (staffId == null) {
            return Optional.empty();
        }
        return Optional.of(accessService.currentUser(staffId, authProvider));
    }

    public CurrentUserDto requireUser(HttpServletRequest request) {
        return fromRequest(request)
                .orElseThrow(() -> new PlatformAuthException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED"));
    }

    public CurrentUserDto requirePlatformAdmin(HttpServletRequest request) {
        CurrentUserDto user = requireUser(request);
        if (user.readOnly() || !user.roles().contains("PLATFORM_ADMIN")) {
            throw new PlatformAuthException(HttpStatus.FORBIDDEN, "PLATFORM_ADMIN_REQUIRED");
        }
        return user;
    }

    public String token(CurrentUserDto user) {
        long expiresAt = Instant.now().plusSeconds(properties.getSessionTtlSeconds()).getEpochSecond();
        String payload = String.join("|",
                user.mode(),
                user.authProvider(),
                user.staffId() == null ? "" : user.staffId(),
                Long.toString(expiresAt)
        );
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encodedPayload + "." + sign(encodedPayload);
    }

    public long sessionTtlSeconds() {
        return properties.getSessionTtlSeconds();
    }

    private String readCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getSessionSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign auth token", ex);
        }
    }

    private boolean isRateLimited(String key) {
        FailedLoginState state = failedLogins.get(key);
        if (state == null) {
            return false;
        }
        Instant cutoff = Instant.now().minusSeconds(properties.getFailedLoginWindowSeconds());
        if (state.firstFailedAt().isBefore(cutoff)) {
            failedLogins.remove(key);
            return false;
        }
        return state.count() >= properties.getFailedLoginLimit();
    }

    private void recordFailedLogin(String key) {
        Instant now = Instant.now();
        failedLogins.compute(key, (ignored, existing) -> {
            if (existing == null || existing.firstFailedAt().isBefore(now.minusSeconds(properties.getFailedLoginWindowSeconds()))) {
                return new FailedLoginState(1, now);
            }
            return new FailedLoginState(existing.count() + 1, existing.firstFailedAt());
        });
    }

    private String loginKey(String staffId, String source) {
        return (staffId == null ? "" : staffId.trim()) + "|" + (source == null ? "unknown" : source);
    }

    private record FailedLoginState(int count, Instant firstFailedAt) {}
}
