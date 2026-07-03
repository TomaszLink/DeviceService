package pl.tomaszlink.deviceservice.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.tomaszlink.deviceservice.domain.common.ListResult;
import pl.tomaszlink.deviceservice.domain.common.ResponseEntityHelper;
import pl.tomaszlink.deviceservice.domain.device.DeviceMapper;
import pl.tomaszlink.deviceservice.domain.device.DeviceService;
import pl.tomaszlink.deviceservice.domain.device.models.DeviceResult;
import pl.tomaszlink.deviceservice.domain.device.models.RegisterDeviceCommand;
import pl.tomaszlink.deviceservice.model.DeviceModel;
import pl.tomaszlink.deviceservice.model.ModifyDeviceRequest;
import pl.tomaszlink.deviceservice.model.RegisterDeviceRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DeviceController implements DevicesApi {

    private final DeviceService deviceService;

    @Override
    public ResponseEntity<List<DeviceModel>> getDevices(Integer page, Integer size, String sortField, String sortDirection) {
        ListResult<DeviceResult> devicesListResult = this.deviceService.getDevices(page, size, sortField, sortDirection);

        return ResponseEntityHelper.createResponseEntityWithHeaders(devicesListResult, DeviceMapper::toDeviceModel);
    }

    @Override
    public ResponseEntity<DeviceModel> getSpecificDevice(UUID id) {
        DeviceModel deviceModel = DeviceMapper.toDeviceModel(this.deviceService.getSpecificDevice(id));
        return ResponseEntity.ok(deviceModel);
    }

    @Override
    public ResponseEntity<DeviceModel> modifyDevice(UUID id, ModifyDeviceRequest modifyDeviceRequest) {
        RegisterDeviceCommand command = DeviceMapper.toCommand(modifyDeviceRequest);
        DeviceModel deviceModel = DeviceMapper.toDeviceModel(this.deviceService.modifyDevice(id, command));
        return ResponseEntity.ok(deviceModel);
    }

    @Override
    public ResponseEntity<DeviceModel> registerNewDevice(RegisterDeviceRequest registerDeviceRequest) {
        RegisterDeviceCommand command = DeviceMapper.toCommand(registerDeviceRequest);
        DeviceModel deviceModel = DeviceMapper.toDeviceModel(
                this.deviceService.registerNewDevice(command)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceModel);
    }
}
