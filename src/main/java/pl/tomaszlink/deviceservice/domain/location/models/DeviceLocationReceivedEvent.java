package pl.tomaszlink.deviceservice.domain.location.models;

import java.time.Instant;
import java.util.UUID;

public record DeviceLocationReceivedEvent(
        UUID eventId,
        UUID deviceId,
        Double latitude,
        Double longitude,
        Instant gpsTimestamp,
        Instant receivedAt
) {
}
