package com.sdlctower.platform.shared;

import java.util.List;

public record CursorPageDto<T>(
        List<T> data,
        PaginationDto pagination
) {
    public static <T> CursorPageDto<T> of(List<T> data) {
        return new CursorPageDto<>(data, new PaginationDto(null, data.size()));
    }

    public record PaginationDto(String nextCursor, Integer total) {}
}
