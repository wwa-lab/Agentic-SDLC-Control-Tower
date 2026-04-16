package com.sdlctower.integration.kb;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.requirement-import.dify")
public class DifyKnowledgeBaseProperties {

    private String baseUrl;
    private String apiKey;
    private int datasetLookupLimit = 100;
    private int segmentPageSize = 100;
    private String indexingTechnique = "high_quality";

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public int getDatasetLookupLimit() { return datasetLookupLimit; }
    public void setDatasetLookupLimit(int datasetLookupLimit) { this.datasetLookupLimit = datasetLookupLimit; }

    public int getSegmentPageSize() { return segmentPageSize; }
    public void setSegmentPageSize(int segmentPageSize) { this.segmentPageSize = segmentPageSize; }

    public String getIndexingTechnique() { return indexingTechnique; }
    public void setIndexingTechnique(String indexingTechnique) { this.indexingTechnique = indexingTechnique; }
}
