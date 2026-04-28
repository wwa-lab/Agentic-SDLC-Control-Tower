package com.sdlctower.domain.requirement;

import java.time.Instant;

public interface GitHubDocumentGateway {
    GitHubDocumentContent fetchMarkdown(String repoFullName, String branchOrRef, String path, String commitSha, String blobSha, String githubUrl, String title);

    record GitHubDocumentContent(String markdown, String commitSha, String blobSha, String githubUrl, Instant fetchedAt) {}
}
