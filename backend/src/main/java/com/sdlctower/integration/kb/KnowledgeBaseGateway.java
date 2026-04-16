package com.sdlctower.integration.kb;

import java.util.List;

public interface KnowledgeBaseGateway {

    String providerName();

    KnowledgeBaseTarget resolveKnowledgeBase(String knowledgeBaseName);

    UploadResult uploadFile(KnowledgeBaseTarget target, String fileName, String contentType, byte[] fileBytes);

    DocumentStatus getDocumentStatus(KnowledgeBaseTarget target, String documentId);

    List<String> getDocumentSegments(KnowledgeBaseTarget target, String documentId);

    record KnowledgeBaseTarget(String name, String datasetId) {}

    record UploadResult(String documentId, String batchId, String indexingStatus) {}

    record DocumentStatus(String documentId, String indexingStatus, String errorMessage) {
        public boolean isCompleted() {
            return "completed".equalsIgnoreCase(indexingStatus);
        }

        public boolean isFailed() {
            return "error".equalsIgnoreCase(indexingStatus)
                    || "paused".equalsIgnoreCase(indexingStatus)
                    || "stopped".equalsIgnoreCase(indexingStatus);
        }
    }
}
