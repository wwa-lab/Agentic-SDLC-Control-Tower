package com.sdlctower.domain.requirement;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.requirement-control-plane.github.provider", havingValue = "stub", matchIfMissing = true)
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

    @Override
    public List<GitHubDocumentMetadata> listMarkdownDocuments(String repoFullName, String branchOrRef, String docsRoot) {
        String root = docsRoot == null || docsRoot.isBlank() ? "docs/" : docsRoot;
        String normalizedRepo = repoFullName == null ? "" : repoFullName.toLowerCase(Locale.ROOT);
        List<String[]> documents = new ArrayList<>();

        if (normalizedRepo.contains("payment-gateway")) {
            documents.add(new String[] { "docs/01-requirements/sso-requirement.md", "SSO Requirement" });
            documents.add(new String[] { "docs/02-user-stories/sso-stories.md", "SSO User Stories" });
            documents.add(new String[] { "docs/03-spec/sso-functional-spec.md", "SSO Functional Spec" });
            documents.add(new String[] { "docs/04-architecture/sso-architecture.md", "SSO Architecture" });
            documents.add(new String[] { "docs/04-architecture/sso-architecture-data-flow.md", "SSO Data Flow" });
            documents.add(new String[] { "docs/04-architecture/sso-architecture-data-model.md", "SSO Data Model" });
            documents.add(new String[] { "docs/05-design/sso-design.md", "SSO Design" });
            documents.add(new String[] { "docs/05-design/contracts/sso-api.md", "SSO API Guide" });
            documents.add(new String[] { "docs/06-tasks/sso-tasks.md", "SSO Tasks" });
        } else if (normalizedRepo.contains("sdlc-tower")) {
            documents.add(new String[] { "docs/01-requirements/audit-trail.md", "Audit Trail Requirement" });
            documents.add(new String[] { "docs/02-user-stories/audit-trail-stories.md", "Audit Trail User Stories" });
            documents.add(new String[] { "docs/03-spec/audit-trail.md", "Audit Trail Spec" });
            documents.add(new String[] { "docs/04-architecture/audit-trail.md", "Audit Trail Architecture" });
            documents.add(new String[] { "docs/04-architecture/audit-trail-data-flow.md", "Audit Trail Data Flow" });
            documents.add(new String[] { "docs/04-architecture/audit-trail-data-model.md", "Audit Trail Data Model" });
            documents.add(new String[] { "docs/05-design/audit-trail.md", "Audit Trail Design" });
            documents.add(new String[] { "docs/05-design/contracts/audit-trail-api.md", "Audit Trail API Guide" });
            documents.add(new String[] { "docs/06-tasks/audit-trail-tasks.md", "Audit Trail Tasks" });
            documents.add(new String[] { "docs/01-requirements/BR-AUDIT-001-normalizer.md", "Audit Trail Requirement Normalizer" });
            documents.add(new String[] { "docs/02-functional-spec/BR-AUDIT-001.md", "Audit Trail Functional Spec" });
            documents.add(new String[] { "docs/03-technical-design/BR-AUDIT-001.md", "Audit Trail Technical Design" });
            documents.add(new String[] { "docs/04-program-spec/AUDITRPT.md", "AUDITRPT Program Spec" });
            documents.add(new String[] { "docs/05-file-spec/AUDITPF.md", "AUDITPF File Spec" });
            documents.add(new String[] { "docs/06-ut-plan/AUDITRPT.md", "AUDITRPT UT Plan" });
            documents.add(new String[] { "docs/07-test-scaffold/AUDITRPT.md", "AUDITRPT Test Scaffold" });
            documents.add(new String[] { "docs/08-reviews/spec/BR-AUDIT-001.md", "Audit Trail Spec Review" });
            documents.add(new String[] { "docs/08-reviews/dds/AUDITPF.md", "AUDITPF DDS Review" });
            documents.add(new String[] { "docs/08-reviews/code/AUDITRPT.md", "AUDITRPT Code Review" });
        }

        return documents.stream()
                .filter(document -> document[0].startsWith(root))
                .map(document -> metadata(repoFullName, branchOrRef, document[0], document[1]))
                .toList();
    }

    private GitHubDocumentMetadata metadata(String repoFullName, String branchOrRef, String path, String title) {
        String versionSeed = Integer.toHexString(Math.abs((repoFullName + branchOrRef + path).hashCode()));
        String shortSeed = versionSeed.length() > 10 ? versionSeed.substring(0, 10) : versionSeed;
        String githubUrl = "https://github.com/" + repoFullName + "/blob/" + branchOrRef + "/" + path;
        return new GitHubDocumentMetadata(path, title, "sync-" + shortSeed, "blob-" + shortSeed, githubUrl, Instant.now());
    }
}
