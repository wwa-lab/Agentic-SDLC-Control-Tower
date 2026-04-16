package com.sdlctower.shared.dto;

public record SectionResultDto<T>(T data, String error) {

    public static <T> SectionResultDto<T> ok(T data) {
        return new SectionResultDto<>(data, null);
    }

    public static <T> SectionResultDto<T> fail(String error) {
        return new SectionResultDto<>(null, error);
    }
}
