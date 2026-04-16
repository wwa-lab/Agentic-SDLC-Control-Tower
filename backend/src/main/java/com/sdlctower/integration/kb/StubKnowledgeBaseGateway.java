package com.sdlctower.integration.kb;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.requirement-import.kb-provider", havingValue = "stub", matchIfMissing = true)
public class StubKnowledgeBaseGateway implements KnowledgeBaseGateway {

    private final Map<String, StubDocument> documents = new ConcurrentHashMap<>();

    @Override
    public String providerName() {
        return "stub";
    }

    @Override
    public KnowledgeBaseTarget resolveKnowledgeBase(String knowledgeBaseName) {
        return new KnowledgeBaseTarget(knowledgeBaseName, "stub-" + knowledgeBaseName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-"));
    }

    @Override
    public UploadResult uploadFile(KnowledgeBaseTarget target, String fileName, String contentType, byte[] fileBytes) {
        String documentId = UUID.randomUUID().toString();
        documents.put(documentId, new StubDocument(extractContent(fileName, fileBytes)));
        return new UploadResult(documentId, UUID.randomUUID().toString(), "completed");
    }

    @Override
    public DocumentStatus getDocumentStatus(KnowledgeBaseTarget target, String documentId) {
        if (!documents.containsKey(documentId)) {
            return new DocumentStatus(documentId, "error", "Document not found in stub knowledge base");
        }
        return new DocumentStatus(documentId, "completed", null);
    }

    @Override
    public List<String> getDocumentSegments(KnowledgeBaseTarget target, String documentId) {
        StubDocument document = documents.get(documentId);
        return document == null ? List.of() : document.segments();
    }

    private List<String> extractContent(String fileName, byte[] fileBytes) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".html") || lower.endsWith(".htm")) {
            String html = new String(fileBytes, StandardCharsets.UTF_8);
            return List.of(html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim());
        }
        if (lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".csv")) {
            return List.of(new String(fileBytes, StandardCharsets.UTF_8).trim());
        }
        return List.of("[Stub KB captured " + fileName + "]");
    }

    private record StubDocument(List<String> segments) {}
}
