package com.sdlctower.domain.reportcenter.definitions;

import com.sdlctower.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ReportDefinitionRegistry {

    private final List<ReportDefinition> definitions;
    private final Map<String, ReportDefinition> byKey;
    private static final List<String> ORDER = List.of(
            "eff.lead-time",
            "eff.cycle-time",
            "eff.throughput",
            "eff.wip",
            "eff.flow-efficiency"
    );

    public ReportDefinitionRegistry(List<ReportDefinition> definitions) {
        this.byKey = definitions.stream().collect(Collectors.toMap(ReportDefinition::key, Function.identity()));
        this.definitions = ORDER.stream()
                .filter(byKey::containsKey)
                .map(byKey::get)
                .toList();
    }

    public List<ReportDefinition> all() {
        return definitions;
    }

    public ReportDefinition require(String reportKey) {
        ReportDefinition definition = byKey.get(reportKey);
        if (definition == null) {
            throw new ResourceNotFoundException("REPORT_NOT_FOUND: " + reportKey);
        }
        return definition;
    }
}
