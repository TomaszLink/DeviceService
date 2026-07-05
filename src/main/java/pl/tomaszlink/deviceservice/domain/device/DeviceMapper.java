package pl.tomaszlink.deviceservice.domain.device;

import jakarta.validation.constraints.NotNull;
import pl.tomaszlink.deviceservice.domain.device.models.*;
import pl.tomaszlink.deviceservice.model.DeviceModel;
import pl.tomaszlink.deviceservice.model.ModifyDeviceRequest;
import pl.tomaszlink.deviceservice.model.RegisterDeviceRequest;

import java.util.UUID;

public class DeviceMapper {
    private DeviceMapper() {}

    public static RegisterDeviceCommand toCommand(@NotNull RegisterDeviceRequest request) {
        return new RegisterDeviceCommand(
                request.getName(),
                DeviceType.valueOf(request.getType().name()),
                request.getUniqueIdentifier()
        );
    }

    public static ModifyDeviceCommand toCommand(@NotNull UUID id, @NotNull ModifyDeviceRequest request) {
        return new ModifyDeviceCommand(
                id,
                request.getName(),
                DeviceType.valueOf(request.getType().name()),
                request.getUniqueIdentifier()
        );
    }

    public static DeviceModel toDeviceModel(@NotNull DeviceResult deviceResult) {
        return new DeviceModel()
                .id(deviceResult.id())
                .name(deviceResult.name())
                .type(DeviceModel.TypeEnum.fromValue(deviceResult.type().name()))
                .uniqueIdentifier(deviceResult.uniqueIdentifier());

    }

    public static DeviceResult toResult(@NotNull DeviceEntity deviceEntity) {
        return new DeviceResult(
                deviceEntity.getId(),
                deviceEntity.getName(),
                deviceEntity.getType(),
                deviceEntity.getUniqueIdentifier()
        );
    }
}
