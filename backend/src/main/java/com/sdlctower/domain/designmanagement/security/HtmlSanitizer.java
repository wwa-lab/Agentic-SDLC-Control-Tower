package com.sdlctower.domain.designmanagement.security;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class HtmlSanitizer {

    private static final Pattern SCRIPT_TAG = Pattern.compile(
            "<script\\b([^>]*)>(.*?)</script>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern SRC = Pattern.compile("src\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private static final List<String> ALLOWLIST = List.of(
            "cdn.tailwindcss.com",
            "fonts.googleapis.com",
            "fonts.gstatic.com"
    );

    public String sanitize(String html) {
        StringBuilder sanitized = new StringBuilder();
        Matcher matcher = SCRIPT_TAG.matcher(html);
        int last = 0;
        while (matcher.find()) {
            sanitized.append(html, last, matcher.start());
            String attributes = matcher.group(1) == null ? "" : matcher.group(1);
            Matcher srcMatcher = SRC.matcher(attributes);
            if (srcMatcher.find()) {
                String src = srcMatcher.group(1);
                if (ALLOWLIST.stream().anyMatch(src::contains)) {
                    sanitized.append(matcher.group());
                }
            }
            last = matcher.end();
        }
        sanitized.append(html.substring(last));
        return sanitized.toString();
    }
}
