package pl.tomaszlink.deviceservice.domain.common;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.function.Function;

public class ResponseEntityHelper {
    private  ResponseEntityHelper() {}

    private static final String X_TOTAL_COUNT_HEADER = "X-Total-Count";
    private static final String X_TOTAL_PAGES_HEADER = "X-Total-Pages";
    private static final String X_PAGE = "X-Page";
    private static final String X_SIZE = "X-Size";


    public static <S, T> ResponseEntity<List<T>> createResponseEntityWithHeaders(@NotNull ListResult<S> listResult, Function<S, T> mapper) {
        return ResponseEntity
                .ok()
                .header(X_TOTAL_COUNT_HEADER, String.valueOf(listResult.totalCount()))
                .header(X_TOTAL_PAGES_HEADER, String.valueOf(listResult.totalPages()))
                .header(X_PAGE, String.valueOf(listResult.page()))
                .header(X_SIZE, String.valueOf(listResult.size()))
                .body(listResult.content().stream().map(mapper).toList());
    }
}
