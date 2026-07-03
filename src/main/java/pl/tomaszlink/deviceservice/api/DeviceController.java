package pl.tomaszlink.deviceservice.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.tomaszlink.deviceservice.domain.device.DeviceMapper;
import pl.tomaszlink.deviceservice.domain.device.DeviceService;
import pl.tomaszlink.deviceservice.domain.device.models.DevicesListResult;
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

    private static final String X_TOTAL_COUNT_HEADER = "X-Total-Count";
    private static final String X_TOTAL_PAGES_HEADER = "X-Total-Pages";
    private static final String X_PAGE = "X-Page";
    private static final String X_SIZE = "X-Size";

    @Override
    public ResponseEntity<List<DeviceModel>> getDevices(Integer page, Integer size, String sortField, String sortDirection) {
        DevicesListResult devicesListResult = this.deviceService.getDevices(page, size, sortField, sortDirection);

        return ResponseEntity
                .ok()
                .header(X_TOTAL_COUNT_HEADER, String.valueOf(devicesListResult.totalCount()))
                .header(X_TOTAL_PAGES_HEADER, String.valueOf(devicesListResult.totalPages()))
                .header(X_PAGE, String.valueOf(devicesListResult.page()))
                .header(X_SIZE, String.valueOf(devicesListResult.size()))
                .body(devicesListResult.devices().stream().map(DeviceMapper::toDeviceModel).toList());
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
