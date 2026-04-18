package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.dto.CodeBuildManagementDtos.RunRerunDto;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementRunRerunProjection")
public class RunRerunProjection {

    public RunRerunDto load(String runId) {
        // Placeholder for V1: no rerun data available by default
        return null;
    }
}
