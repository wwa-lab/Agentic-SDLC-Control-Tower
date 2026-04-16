package com.sdlctower.integration.kb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@ConditionalOnProperty(name = "app.requirement-import.kb-provider", havingValue = "dify")
public class DifyKnowledgeBaseGateway implements KnowledgeBaseGateway {

    private final RestClient restClient;
    private final DifyKnowledgeBaseProperties properties;
    private final ObjectMapper objectMapper;

    public DifyKnowledgeBaseGateway(
            RestClient.Builder restClientBuilder,
            DifyKnowledgeBaseProperties properties,
            ObjectMapper objectMapper
    ) {
        if (properties.getBaseUrl() == null || properties.getBaseUrl().isBlank()) {
            throw new IllegalStateException("Dify knowledge base integration requires app.requirement-import.dify.base-url");
        }
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new IllegalStateException("Dify knowledge base integration requires app.requirement-import.dify.api-key");
        }
        this.restClient = restClientBuilder
                .baseUrl(trimTrailingSlash(properties.getBaseUrl()))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .build();
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public String providerName() {
        return "dify";
    }

    @Override
    public KnowledgeBaseTarget resolveKnowledgeBase(String knowledgeBaseName) {
        DifyDatasetListResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/datasets")
                        .queryParam("page", 1)
                        .queryParam("limit", properties.getDatasetLookupLimit())
                        .queryParam("keyword", knowledgeBaseName)
                        .build())
                .retrieve()
                .body(DifyDatasetListResponse.class);

        if (response == null || response.data() == null) {
            throw new IllegalArgumentException("No knowledge bases were returned by Dify for " + knowledgeBaseName);
        }

        return response.data().stream()
                .filter(dataset -> dataset.name() != null && dataset.name().equalsIgnoreCase(knowledgeBaseName))
                .findFirst()
                .map(dataset -> new KnowledgeBaseTarget(dataset.name(), dataset.id()))
                .orElseThrow(() -> new IllegalArgumentException("Knowledge base not found in Dify: " + knowledgeBaseName));
    }

    @Override
    public UploadResult uploadFile(KnowledgeBaseTarget target, String fileName, String contentType, byte[] fileBytes) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("data", buildUploadMetadata());
        body.add("file", new NamedByteArrayResource(fileBytes, fileName));

        DifyCreateDocumentResponse response = restClient.post()
                .uri("/datasets/{datasetId}/document/create-by-file", target.datasetId())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(DifyCreateDocumentResponse.class);

        if (response == null || response.document() == null || response.document().id() == null) {
            throw new IllegalArgumentException("Dify did not return a document id for " + fileName);
        }

        return new UploadResult(
                response.document().id(),
                response.batch(),
                response.document().indexingStatus() == null ? "waiting" : response.document().indexingStatus()
        );
    }

    @Override
    public DocumentStatus getDocumentStatus(KnowledgeBaseTarget target, String documentId) {
        DifyDocumentResponse response = restClient.get()
                .uri("/datasets/{datasetId}/documents/{documentId}", target.datasetId(), documentId)
                .retrieve()
                .body(DifyDocumentResponse.class);

        if (response == null) {
            return new DocumentStatus(documentId, "error", "Dify returned no document payload");
        }
        return new DocumentStatus(documentId, defaultStatus(response.indexingStatus()), response.error());
    }

    @Override
    public List<String> getDocumentSegments(KnowledgeBaseTarget target, String documentId) {
        List<String> segments = new ArrayList<>();
        int page = 1;
        boolean hasMore;
        do {
            int currentPage = page;
            DifySegmentsResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/datasets/{datasetId}/documents/{documentId}/segments")
                            .queryParam("page", currentPage)
                            .queryParam("limit", properties.getSegmentPageSize())
                            .build(target.datasetId(), documentId))
                    .retrieve()
                    .body(DifySegmentsResponse.class);

            if (response == null || response.data() == null) {
                break;
            }
            response.data().stream()
                    .map(DifySegment::content)
                    .filter(content -> content != null && !content.isBlank())
                    .forEach(segments::add);
            hasMore = response.hasMore();
            page += 1;
        } while (hasMore);
        return segments;
    }

    private String buildUploadMetadata() {
        LinkedHashMap<String, Object> payload = new LinkedHashMap<>();
        payload.put("indexing_technique", properties.getIndexingTechnique());
        payload.put("process_rule", java.util.Map.of("mode", "automatic"));
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to build Dify upload metadata", exception);
        }
    }

    private String defaultStatus(String indexingStatus) {
        return indexingStatus == null || indexingStatus.isBlank() ? "waiting" : indexingStatus.toLowerCase(Locale.ROOT);
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private static final class NamedByteArrayResource extends ByteArrayResource {

        private final String filename;

        private NamedByteArrayResource(byte[] bytes, String filename) {
            super(bytes);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }

    private record DifyDatasetListResponse(List<DifyDatasetItem> data) {}

    private record DifyDatasetItem(String id, String name) {}

    private record DifyCreateDocumentResponse(DifyCreatedDocument document, String batch) {}

    private record DifyCreatedDocument(String id, String indexingStatus) {}

    private record DifyDocumentResponse(String id, String indexingStatus, String error) {}

    private record DifySegmentsResponse(List<DifySegment> data, boolean hasMore) {}

    private record DifySegment(String content) {}
}
