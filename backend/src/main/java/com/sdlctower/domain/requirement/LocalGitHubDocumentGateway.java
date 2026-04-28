package com.sdlctower.domain.requirement;

import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class LocalGitHubDocumentGateway implements GitHubDocumentGateway {
    @Override
    public GitHubDocumentContent fetchMarkdown(String repoFullName, String branchOrRef, String path, String commitSha, String blobSha, String githubUrl, String title) {
        String fallbackTitle = path.substring(path.lastIndexOf('/') + 1).replace(".md", "").replace('-', ' ');
        String documentTitle = title == null || title.isBlank() ? fallbackTitle : title;
        String markdown = "# " + documentTitle + "\n\n"
                + "Source of truth: `" + repoFullName + "` at `" + branchOrRef + "`.\n\n"
                + "- Path: `" + path + "`\n"
                + "- Commit: `" + commitSha + "`\n"
                + "- Blob: `" + blobSha + "`\n\n"
                + "This local gateway simulates GitHub-backed Markdown while preserving commit/blob version binding.";
        return new GitHubDocumentContent(markdown, commitSha, blobSha, githubUrl, Instant.now());
    }
}
