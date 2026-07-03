package pl.tomaszlink.deviceservice.domain.location;

import jakarta.validation.constraints.NotNull;
import pl.tomaszlink.deviceservice.domain.location.models.*;
import pl.tomaszlink.deviceservice.model.DeviceLocationModel;
import pl.tomaszlink.deviceservice.model.DeviceLocationRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

public class DeviceLocationMapper {
    private DeviceLocationMapper() {}

    public static SendDeviceLocationCommand toCommand(@NotNull DeviceLocationRequest sendDeviceLocationRequest) {
        return new SendDeviceLocationCommand(
                sendDeviceLocationRequest.getLatitude(),
                sendDeviceLocationRequest.getLongitude(),
                sendDeviceLocationRequest.getTimestamp().toInstant()
        );
    }

    public static DeviceLocationEvent toEvent(@NotNull UUID deviceId, @NotNull SendDeviceLocationCommand command) {
        return new DeviceLocationEvent(
                UUID.randomUUID(),
                deviceId,
                BigDecimal.valueOf(command.latitude()),
                BigDecimal.valueOf(command.longitude()),
                command.timestamp(),
                Instant.now()
        );
    }

    public static DeviceLocationResult toResult(@NotNull DeviceLocationEntity deviceLocationEntity) {
        return new DeviceLocationResult(
                deviceLocationEntity.getId(),
                deviceLocationEntity.getLatitude(),
                deviceLocationEntity.getLongitude(),
                deviceLocationEntity.getTimestamp(),
                deviceLocationEntity.getReceivedAt()
        );
    }

    public static DeviceLocationResult toResult(@NotNull DeviceLastLocationEntity deviceLastLocationEntity) {
        return new DeviceLocationResult(
                deviceLastLocationEntity.getLocationId(),
                deviceLastLocationEntity.getLatitude(),
                deviceLastLocationEntity.getLongitude(),
                deviceLastLocationEntity.getTimestamp(),
                deviceLastLocationEntity.getReceivedAt()
        );
    }

    public static DeviceLocationModel toModel(@NotNull DeviceLocationResult deviceLocationResult) {
        return new DeviceLocationModel()
                .id(deviceLocationResult.id())
                .latitude(deviceLocationResult.latitude().doubleValue())
                .longitude(deviceLocationResult.longitude().doubleValue())
                .timestamp(deviceLocationResult.timestamp().atOffset(ZoneOffset.UTC))
                .receivedAt(deviceLocationResult.receivedAt().atOffset(ZoneOffset.UTC));
    }

}
