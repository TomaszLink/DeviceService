package pl.tomaszlink.deviceservice.domain.device.models;

import java.util.List;

public record DevicesListResult(
        List<DeviceResult> devices,
        int page,
        int size,
        long totalCount,
        int totalPages
) {
}
