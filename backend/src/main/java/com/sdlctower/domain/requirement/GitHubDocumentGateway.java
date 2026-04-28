package com.sdlctower.domain.requirement;

import java.time.Instant;
import java.util.List;

public interface GitHubDocumentGateway {
    GitHubDocumentContent fetchMarkdown(String repoFullName, String branchOrRef, String path, String commitSha, String blobSha, String githubUrl, String title);

    record GitHubDocumentContent(String markdown, String commitSha, String blobSha, String githubUrl, Instant fetchedAt) {}

    List<GitHubDocumentMetadata> listMarkdownDocuments(String repoFullName, String branchOrRef, String docsRoot);

    record GitHubDocumentMetadata(String path, String title, String commitSha, String blobSha, String githubUrl, Instant fetchedAt) {}
}
