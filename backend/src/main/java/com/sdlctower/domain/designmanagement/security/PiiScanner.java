package com.sdlctower.domain.designmanagement.security;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class PiiScanner {

    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}");
    private static final Pattern SSN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
    private static final Pattern CREDIT_CARD = Pattern.compile("\\b(?:\\d[ -]*?){13,19}\\b");

    public List<PiiMatch> scan(String html) {
        List<PiiMatch> matches = new ArrayList<>();
        collect(matches, EMAIL, "EMAIL", html);
        collect(matches, SSN, "US_SSN", html);
        Matcher creditCardMatcher = CREDIT_CARD.matcher(html);
        while (creditCardMatcher.find()) {
            String sample = creditCardMatcher.group();
            if (passesLuhn(sample)) {
                matches.add(new PiiMatch("CREDIT_CARD", sample.trim()));
            }
        }
        return matches;
    }

    private void collect(List<PiiMatch> matches, Pattern pattern, String kind, String html) {
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            matches.add(new PiiMatch(kind, matcher.group()));
        }
    }

    private boolean passesLuhn(String value) {
        String digits = value.replaceAll("[^0-9]", "");
        int sum = 0;
        boolean alternate = false;
        for (int index = digits.length() - 1; index >= 0; index--) {
            int digit = digits.charAt(index) - '0';
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return digits.length() >= 13 && digits.length() <= 19 && sum % 10 == 0;
    }

    public record PiiMatch(String kind, String sample) {}
}
