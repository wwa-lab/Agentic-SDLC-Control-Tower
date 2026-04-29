package com.sdlctower.domain.requirement;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "app.requirement-control-plane.github.provider", havingValue = "real")
public class RealGitHubDocumentGateway implements GitHubDocumentGateway {
    private final RestClient restClient;

    public RealGitHubDocumentGateway(
            RestClient.Builder builder,
            RequirementControlPlaneProperties properties
    ) {
        RequirementControlPlaneProperties.GitHubIntegration github = properties.getGithub();
        RestClient.Builder configured = builder
                .baseUrl(trimTrailingSlash(github.getApiBaseUrl()))
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28");
        if (github.getToken() != null && !github.getToken().isBlank()) {
            configured.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + github.getToken());
        }
        this.restClient = configured.build();
    }

    @Override
    public GitHubDocumentContent fetchMarkdown(String repoFullName, String branchOrRef, String path, String commitSha, String blobSha, String githubUrl, String title) {
        RepoParts repo = parseRepo(repoFullName);
        String ref = firstNonBlank(commitSha, branchOrRef);
        GitHubContentResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/repos/{owner}/{repo}/contents/{path}")
                        .queryParam("ref", ref)
                        .build(repo.owner(), repo.repo(), path))
                .retrieve()
                .body(GitHubContentResponse.class);

        if (response == null || response.content() == null) {
            throw new IllegalStateException("GitHub returned no Markdown content for " + repoFullName + "/" + path);
        }

        String markdown = new String(Base64.getMimeDecoder().decode(response.content()), StandardCharsets.UTF_8);
        return new GitHubDocumentContent(
                markdown,
                firstNonBlank(commitSha, branchOrRef),
                firstNonBlank(response.sha(), blobSha),
                firstNonBlank(response.htmlUrl(), githubUrl),
                Instant.now()
        );
    }

    @Override
    public List<GitHubDocumentMetadata> listMarkdownDocuments(String repoFullName, String branchOrRef, String docsRoot) {
        RepoParts repo = parseRepo(repoFullName);
        String root = normalizeRoot(docsRoot);
        GitHubCommitResponse commit = restClient.get()
                .uri("/repos/{owner}/{repo}/commits/{ref}", repo.owner(), repo.repo(), branchOrRef)
                .retrieve()
                .body(GitHubCommitResponse.class);
        String headSha = commit == null ? branchOrRef : firstNonBlank(commit.sha(), branchOrRef);

        GitHubTreeResponse tree = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/repos/{owner}/{repo}/git/trees/{treeSha}")
                        .queryParam("recursive", "1")
                        .build(repo.owner(), repo.repo(), branchOrRef))
                .retrieve()
                .body(GitHubTreeResponse.class);

        if (tree == null || tree.tree() == null) {
            return List.of();
        }

        return tree.tree().stream()
                .filter(item -> "blob".equals(item.type()))
                .filter(item -> item.path() != null && item.path().startsWith(root))
                .filter(item -> item.path().toLowerCase(Locale.ROOT).endsWith(".md"))
                .sorted(Comparator.comparing(GitHubTreeItem::path))
                .map(item -> new GitHubDocumentMetadata(
                        item.path(),
                        titleFromPath(item.path()),
                        headSha,
                        item.sha(),
                        "https://github.com/" + repoFullName + "/blob/" + branchOrRef + "/" + item.path(),
                        Instant.now()
                ))
                .toList();
    }

    private RepoParts parseRepo(String repoFullName) {
        if (repoFullName == null || repoFullName.isBlank() || !repoFullName.contains("/")) {
            throw new IllegalArgumentException("GitHub repo must be in owner/name form");
        }
        String[] parts = repoFullName.split("/", 2);
        return new RepoParts(parts[0], parts[1]);
    }

    private String normalizeRoot(String docsRoot) {
        if (docsRoot == null || docsRoot.isBlank()) return "docs/";
        return docsRoot.endsWith("/") ? docsRoot : docsRoot + "/";
    }

    private String titleFromPath(String path) {
        String fileName = path.substring(path.lastIndexOf('/') + 1).replaceFirst("\\.md$", "");
        String[] words = fileName.replace('-', ' ').replace('_', ' ').split("\\s+");
        StringBuilder title = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) continue;
            if (!title.isEmpty()) title.append(' ');
            title.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
        }
        return title.isEmpty() ? path : title.toString();
    }

    private static String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private static String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) return "https://api.github.com";
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private record RepoParts(String owner, String repo) {}

    private record GitHubCommitResponse(String sha) {}

    private record GitHubTreeResponse(List<GitHubTreeItem> tree) {}

    private record GitHubTreeItem(String path, String type, String sha) {}

    private record GitHubContentResponse(String sha, String content, @com.fasterxml.jackson.annotation.JsonProperty("html_url") String htmlUrl) {}
}
