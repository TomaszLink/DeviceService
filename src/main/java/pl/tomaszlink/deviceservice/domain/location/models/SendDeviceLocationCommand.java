package pl.tomaszlink.deviceservice.domain.location.models;

import java.time.Instant;

public record SendDeviceLocationCommand(
        Double latitude,
        Double longitude,
        Instant timestamp
) {
}
