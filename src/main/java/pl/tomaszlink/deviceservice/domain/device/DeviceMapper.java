package pl.tomaszlink.deviceservice.domain.device;

import jakarta.validation.constraints.NotNull;
import pl.tomaszlink.deviceservice.domain.device.models.DeviceEntity;
import pl.tomaszlink.deviceservice.domain.device.models.DeviceResult;
import pl.tomaszlink.deviceservice.domain.device.models.DeviceType;
import pl.tomaszlink.deviceservice.domain.device.models.RegisterDeviceCommand;
import pl.tomaszlink.deviceservice.model.DeviceModel;
import pl.tomaszlink.deviceservice.model.ModifyDeviceRequest;
import pl.tomaszlink.deviceservice.model.RegisterDeviceRequest;

public class DeviceMapper {
    private DeviceMapper() {}

    public static RegisterDeviceCommand toCommand(@NotNull RegisterDeviceRequest request) {
        return new RegisterDeviceCommand(
                request.getName(),
                DeviceType.valueOf(request.getType().name()),
                request.getUniqueIdentifier()
        );
    }

    public static RegisterDeviceCommand toCommand(@NotNull ModifyDeviceRequest request) {
        return new RegisterDeviceCommand(
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
