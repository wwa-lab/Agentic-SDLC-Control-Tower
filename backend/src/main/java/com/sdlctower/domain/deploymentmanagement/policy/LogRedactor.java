package com.sdlctower.domain.deploymentmanagement.policy;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.regex.Pattern;

@Component("deploymentLogRedactor")
public class LogRedactor {

    private static final List<Pattern> PATTERNS = List.of(
            Pattern.compile("AKIA[0-9A-Z]{16}"),
            Pattern.compile("gh[ps]_[A-Za-z0-9_]{36,}"),
            Pattern.compile("Bearer\\s+[A-Za-z0-9._\\-]+"),
            Pattern.compile("(?i)token[= ]+[A-Za-z0-9]{20,}")
    );

    private static final String REPLACEMENT = "***REDACTED***";

    public String redact(String logText) {
        if (logText == null) {
            return null;
        }
        String result = logText;
        for (Pattern p : PATTERNS) {
            result = p.matcher(result).replaceAll(REPLACEMENT);
        }
        return result;
    }
}
