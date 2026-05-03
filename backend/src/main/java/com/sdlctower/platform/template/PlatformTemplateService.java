package com.sdlctower.platform.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.sdlctower.platform.shared.CursorPageDto;
import com.sdlctower.shared.exception.ResourceNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformTemplateService {

    private final PlatformTemplateRepository templates;
    private final PlatformTemplateVersionRepository versions;
    private final ObjectMapper objectMapper;

    public PlatformTemplateService(PlatformTemplateRepository templates, PlatformTemplateVersionRepository versions, ObjectMapper objectMapper) {
        this.templates = templates;
        this.versions = versions;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public CursorPageDto<TemplateSummaryDto> list(String kind, String status, String q) {
        return CursorPageDto.of(templates.search(blankToNull(kind), blankToNull(status), blankToNull(q))
                .stream()
                .map(this::toSummary)
                .toList());
    }

    @Transactional(readOnly = true)
    public TemplateDetailDto detail(String id) {
        PlatformTemplateEntity template = templates.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TEMPLATE_NOT_FOUND"));
        PlatformTemplateVersionEntity version = currentVersion(template);
        TemplateVersionDto versionDto = toVersion(version);
        TemplateSummaryDto summary = toSummary(template);
        return new TemplateDetailDto(
                new TemplateDetailDto.TemplateDto(
                        summary.id(),
                        summary.key(),
                        summary.name(),
                        summary.kind(),
                        summary.status(),
                        summary.version(),
                        summary.ownerId(),
                        summary.lastModifiedAt(),
                        null
                ),
                versionDto,
                inheritance(versionDto.body())
        );
    }

    @Transactional(readOnly = true)
    public List<TemplateVersionDto> versions(String templateId) {
        if (!templates.existsById(templateId)) {
            throw new ResourceNotFoundException("TEMPLATE_NOT_FOUND");
        }
        return versions.findByTemplateIdOrderByVersionNumberDesc(templateId)
                .stream()
                .map(this::toVersion)
                .toList();
    }

    private PlatformTemplateVersionEntity currentVersion(PlatformTemplateEntity template) {
        if (blankToNull(template.getCurrentVersionId()) != null) {
            return versions.findById(template.getCurrentVersionId())
                    .orElseThrow(() -> new ResourceNotFoundException("TEMPLATE_NOT_FOUND"));
        }
        return versions.findFirstByTemplateIdOrderByVersionNumberDesc(template.getId())
                .orElseThrow(() -> new ResourceNotFoundException("TEMPLATE_NOT_FOUND"));
    }

    private TemplateSummaryDto toSummary(PlatformTemplateEntity entity) {
        Integer version = versions.findFirstByTemplateIdOrderByVersionNumberDesc(entity.getId())
                .map(PlatformTemplateVersionEntity::getVersionNumber)
                .orElse(0);
        return new TemplateSummaryDto(
                entity.getId(),
                entity.getKey(),
                entity.getName(),
                entity.getKind(),
                entity.getStatus(),
                version,
                entity.getOwnerId(),
                entity.getUpdatedAt()
        );
    }

    private TemplateVersionDto toVersion(PlatformTemplateVersionEntity entity) {
        return new TemplateVersionDto(
                entity.getId(),
                entity.getTemplateId(),
                entity.getVersionNumber(),
                readJson(entity.getBody()),
                entity.getCreatedAt(),
                entity.getCreatedBy()
        );
    }

    private Map<String, InheritanceFieldDto> inheritance(JsonNode body) {
        Map<String, InheritanceFieldDto> fields = new LinkedHashMap<>();
        if (body == null || !body.isObject()) {
            return fields;
        }
        body.fields().forEachRemaining(entry -> fields.put(entry.getKey(), new InheritanceFieldDto(
                entry.getValue(),
                "platform",
                Map.of(
                        "platform", entry.getValue(),
                        "application", NullNode.getInstance(),
                        "snowGroup", NullNode.getInstance(),
                        "workspace", NullNode.getInstance(),
                        "project", NullNode.getInstance()
                )
        )));
        return fields;
    }

    private JsonNode readJson(String value) {
        if (value == null || value.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode node = objectMapper.readTree(value);
            return node == null || node.isNull() ? objectMapper.createObjectNode() : node;
        } catch (JsonProcessingException ex) {
            return objectMapper.createObjectNode().put("raw", value);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
