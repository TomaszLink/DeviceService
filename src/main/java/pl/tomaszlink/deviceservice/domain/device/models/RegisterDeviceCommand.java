package pl.tomaszlink.deviceservice.domain.device.models;

public record RegisterDeviceCommand(
        String name,
        DeviceType type,
        String uniqueIdentifier
) {
}
