package com.sdlctower.domain.reportcenter.render;

import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.DrilldownDto;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CsvWriter {

    public byte[] write(DrilldownDto drilldown) {
        StringBuilder builder = new StringBuilder();
        builder.append('\uFEFF');
        builder.append(String.join(",", drilldown.columns().stream().map(column -> escape(column.label())).toList()));
        builder.append('\n');
        for (Map<String, Object> row : drilldown.rows()) {
            builder.append(String.join(",", drilldown.columns().stream()
                    .map(column -> escape(String.valueOf(row.get(column.key()))))
                    .toList()));
            builder.append('\n');
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        if (value == null || "null".equals(value)) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
