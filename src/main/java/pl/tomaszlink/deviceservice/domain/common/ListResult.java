package pl.tomaszlink.deviceservice.domain.common;

import java.util.List;

public record ListResult<T>(
        List<T> content,
        int page,
        int size,
        long totalCount,
        int totalPages
) {
}
