package com.sdlctower.domain.codebuildmanagement.projection;

import com.sdlctower.domain.codebuildmanagement.CodeBuildManagementConstants;
import com.sdlctower.domain.codebuildmanagement.persistence.LogExcerptRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineJobEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineJobRepository;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineStepEntity;
import com.sdlctower.domain.codebuildmanagement.persistence.PipelineStepRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementRunLogsProjection")
public class RunLogsProjection {

    private final PipelineJobRepository pipelineJobRepository;
    private final PipelineStepRepository pipelineStepRepository;
    private final LogExcerptRepository logExcerptRepository;

    public RunLogsProjection(
            PipelineJobRepository pipelineJobRepository,
            PipelineStepRepository pipelineStepRepository,
            LogExcerptRepository logExcerptRepository
    ) {
        this.pipelineJobRepository = pipelineJobRepository;
        this.pipelineStepRepository = pipelineStepRepository;
        this.logExcerptRepository = logExcerptRepository;
    }

    public String load(String runId) {
        List<PipelineJobEntity> jobs = pipelineJobRepository
                .findByRunIdOrderByJobNumberAsc(runId);

        String combinedLog = jobs.stream()
                .flatMap(job -> pipelineStepRepository
                        .findByJobIdOrderByOrderIndexAsc(job.getId())
                        .stream())
                .map(step -> logExcerptRepository.findByStepId(step.getId())
                        .map(log -> log.getText())
                        .orElse(""))
                .filter(text -> text != null && !text.isBlank())
                .collect(Collectors.joining("\n"));

        return tailLines(combinedLog, CodeBuildManagementConstants.COMBINED_LOG_TAIL_LINES);
    }

    private String tailLines(String text, int maxLines) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String[] lines = text.split("\n");
        if (lines.length <= maxLines) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        int startIndex = lines.length - maxLines;
        for (int i = startIndex; i < lines.length; i++) {
            if (i > startIndex) {
                sb.append('\n');
            }
            sb.append(lines[i]);
        }
        return sb.toString();
    }
}
