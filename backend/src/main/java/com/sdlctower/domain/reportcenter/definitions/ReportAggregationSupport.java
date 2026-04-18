package com.sdlctower.domain.reportcenter.definitions;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ReportAggregationSupport {

    private static final DateTimeFormatter ISO_DAY = DateTimeFormatter.ISO_LOCAL_DATE;

    private ReportAggregationSupport() {}

    public static String titleize(String raw) {
        if (raw == null || raw.isBlank()) {
            return "Unknown";
        }
        String normalized = raw.replace('_', ' ').replace('-', ' ');
        String[] parts = normalized.split("\\s+");
        List<String> titled = new ArrayList<>();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            titled.add(part.substring(0, 1).toUpperCase() + part.substring(1));
        }
        return String.join(" ", titled);
    }

    public static String formatMinutes(int minutes) {
        int days = minutes / 1440;
        int hours = (minutes % 1440) / 60;
        int remainder = minutes % 60;
        List<String> parts = new ArrayList<>();
        if (days > 0) {
            parts.add(days + "d");
        }
        if (hours > 0) {
            parts.add(hours + "h");
        }
        if (remainder > 0 || parts.isEmpty()) {
            parts.add(remainder + "m");
        }
        return String.join(" ", parts.subList(0, Math.min(parts.size(), 2)));
    }

    public static double percentile(List<Integer> values, double percentile) {
        if (values.isEmpty()) {
            return 0;
        }
        List<Integer> sorted = values.stream().sorted().toList();
        int index = (int) Math.ceil(percentile * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }

    public static double average(List<Integer> values) {
        if (values.isEmpty()) {
            return 0;
        }
        return values.stream().mapToInt(Integer::intValue).average().orElse(0);
    }

    public static Instant startOfDay(LocalDate date) {
        return date.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    public static String isoDay(LocalDate date) {
        return ISO_DAY.format(date);
    }

    public static <T> T maxBy(List<T> values, Comparator<T> comparator) {
        return values.stream().max(comparator).orElse(null);
    }
}
