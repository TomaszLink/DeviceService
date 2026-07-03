package pl.tomaszlink.deviceservice.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.tomaszlink.deviceservice.domain.common.ListResult;
import pl.tomaszlink.deviceservice.domain.common.ResponseEntityHelper;
import pl.tomaszlink.deviceservice.domain.location.DeviceLocationService;
import pl.tomaszlink.deviceservice.domain.location.SendDeviceLocationService;
import pl.tomaszlink.deviceservice.domain.location.DeviceLocationMapper;
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationResult;
import pl.tomaszlink.deviceservice.domain.location.models.SendDeviceLocationCommand;
import pl.tomaszlink.deviceservice.model.DeviceLocationModel;
import pl.tomaszlink.deviceservice.model.DeviceLocationRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DeviceLocationController implements LocationsApi{
    private final SendDeviceLocationService sendDeviceLocationService;
    private final DeviceLocationService deviceLocationService;

    @Override
    public ResponseEntity<Void> sendDeviceLocation(UUID id, DeviceLocationRequest sendDeviceLocationRequest) {
        SendDeviceLocationCommand command = DeviceLocationMapper.toCommand(sendDeviceLocationRequest);
        this.sendDeviceLocationService.sendDeviceLocation(id, command);
        return ResponseEntity.accepted().build();
    }

    @Override
    public ResponseEntity<List<DeviceLocationModel>> getDeviceLocations(UUID id, Integer page, Integer size) {
        ListResult<DeviceLocationResult> deviceLocations = this.deviceLocationService.getDeviceLocations(id, page, size);

        return ResponseEntityHelper.createResponseEntityWithHeaders(deviceLocations, DeviceLocationMapper::toModel);
    }

    @Override
    public ResponseEntity<DeviceLocationModel> getLatestDeviceLocation(UUID id) {
        DeviceLocationResult deviceLastLocation = this.deviceLocationService.getDeviceLastLocation(id);

        return ResponseEntity.ok(DeviceLocationMapper.toModel(deviceLastLocation));
    }
}
