package com.sdlctower.domain.codebuildmanagement.policy;

import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component("codeBuildManagementLogRedactor")
public class LogRedactor {

    private record RedactionRule(Pattern pattern, String replacement) {}

    private static final List<RedactionRule> RULES = List.of(
            new RedactionRule(
                    Pattern.compile("AKIA[0-9A-Z]{16}"),
                    "***REDACTED:AWS_KEY***"),
            new RedactionRule(
                    Pattern.compile("ghp_[A-Za-z0-9]{36}"),
                    "***REDACTED:GH_TOKEN***"),
            new RedactionRule(
                    Pattern.compile("gho_[A-Za-z0-9]{36}"),
                    "***REDACTED:GH_TOKEN***"),
            new RedactionRule(
                    Pattern.compile("ghs_[A-Za-z0-9]{36}"),
                    "***REDACTED:GH_TOKEN***"),
            new RedactionRule(
                    Pattern.compile("(?i)Bearer\\s+[A-Za-z0-9._\\-]{20,}"),
                    "Bearer ***REDACTED:BEARER***")
    );

    public String redact(String logText) {
        if (logText == null) {
            return null;
        }

        String result = logText;
        for (RedactionRule rule : RULES) {
            result = rule.pattern().matcher(result).replaceAll(rule.replacement());
        }
        return result;
    }
}
