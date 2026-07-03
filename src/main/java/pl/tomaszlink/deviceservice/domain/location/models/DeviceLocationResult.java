package pl.tomaszlink.deviceservice.domain.location.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DeviceLocationResult(
        UUID id,
        BigDecimal latitude,
        BigDecimal longitude,
        Instant timestamp,
        Instant receivedAt
) {

}
