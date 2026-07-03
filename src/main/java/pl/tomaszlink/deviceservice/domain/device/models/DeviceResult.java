package pl.tomaszlink.deviceservice.domain.device.models;

import java.util.UUID;

public record DeviceResult(
        UUID id,
        String name,
        DeviceType type,
        String uniqueIdentifier
) {
}
