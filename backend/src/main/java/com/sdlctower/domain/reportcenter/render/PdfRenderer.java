package com.sdlctower.domain.reportcenter.render;

import com.sdlctower.domain.reportcenter.dto.ReportCenterDtos.ReportRunResultDto;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PdfRenderer {

    public byte[] render(String title, ReportRunResultDto result) {
        List<String> lines = new ArrayList<>();
        lines.add(title);
        lines.add("Scope: " + result.scope() + " / " + String.join(", ", result.scopeIds()));
        lines.add("Grouping: " + result.grouping());
        lines.add("Snapshot: " + result.snapshotAt());
        if (result.headline().data() != null) {
            result.headline().data().forEach(metric -> lines.add(metric.label() + ": " + metric.value()));
        }
        if (result.drilldown().data() != null) {
            lines.add("Rows: " + result.drilldown().data().totalRows());
        }

        String content = buildContent(lines);
        StringBuilder pdf = new StringBuilder();
        List<Integer> offsets = new ArrayList<>();
        pdf.append("%PDF-1.4\n");
        offsets.add(pdf.length());
        pdf.append("1 0 obj<< /Type /Catalog /Pages 2 0 R >>endobj\n");
        offsets.add(pdf.length());
        pdf.append("2 0 obj<< /Type /Pages /Kids [3 0 R] /Count 1 >>endobj\n");
        offsets.add(pdf.length());
        pdf.append("3 0 obj<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>endobj\n");
        offsets.add(pdf.length());
        pdf.append("4 0 obj<< /Length ").append(content.getBytes(StandardCharsets.UTF_8).length).append(" >>stream\n")
                .append(content)
                .append("\nendstream endobj\n");
        offsets.add(pdf.length());
        pdf.append("5 0 obj<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>endobj\n");
        int xrefOffset = pdf.length();
        pdf.append("xref\n0 6\n0000000000 65535 f \n");
        for (Integer offset : offsets) {
            pdf.append(String.format(java.util.Locale.US, "%010d 00000 n %n", offset));
        }
        pdf.append("trailer<< /Size 6 /Root 1 0 R >>\nstartxref\n").append(xrefOffset).append("\n%%EOF");
        return pdf.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String buildContent(List<String> lines) {
        StringBuilder content = new StringBuilder("BT /F1 12 Tf 50 760 Td 14 TL\n");
        for (String line : lines) {
            content.append("(").append(escape(line)).append(") Tj T*\n");
        }
        content.append("ET");
        return content.toString();
    }

    private String escape(String line) {
        return line.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
