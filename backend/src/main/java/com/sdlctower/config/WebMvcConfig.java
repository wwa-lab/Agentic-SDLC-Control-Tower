package com.sdlctower.config;

import com.sdlctower.platform.workspace.WorkspaceContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final WorkspaceContextInterceptor workspaceInterceptor;

    public WebMvcConfig(WorkspaceContextInterceptor workspaceInterceptor) {
        this.workspaceInterceptor = workspaceInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(workspaceInterceptor)
                .addPathPatterns("/api/v1/**");
    }
}
