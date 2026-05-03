package com.sdlctower.platform.shell;

import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShellController {

    private final String userGuidelineUrl;

    public ShellController(@Value("${app.shell.user-guideline-url:https://confluence.company.com/display/SDLC/User+Guideline}") String userGuidelineUrl) {
        this.userGuidelineUrl = userGuidelineUrl;
    }

    @GetMapping(ApiConstants.SHELL_HELP_LINKS)
    public ApiResponse<HelpLinksDto> helpLinks() {
        return ApiResponse.ok(new HelpLinksDto(userGuidelineUrl));
    }
}
