package pl.tomaszlink.deviceservice.domain.location.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DeviceLocationEvent(
        UUID id,
        UUID deviceId,
        BigDecimal latitude,
        BigDecimal longitude,
        Instant timestamp,
        Instant receivedAt
) {

}
