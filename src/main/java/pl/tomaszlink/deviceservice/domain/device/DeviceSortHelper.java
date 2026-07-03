package pl.tomaszlink.deviceservice.domain.device;

import jakarta.validation.constraints.NotNull;

public class DeviceSortHelper {
    private  DeviceSortHelper() {}

    public static String mapSortField(@NotNull String sortField) {
        return switch (sortField.toUpperCase()) {
            case "TYPE" -> "type";
            case "UNIQUE_IDENTIFIER" -> "uniqueIdentifier";
            default -> "name";
        };
    }
}
