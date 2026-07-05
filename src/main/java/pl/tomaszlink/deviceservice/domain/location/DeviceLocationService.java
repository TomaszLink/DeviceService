package pl.tomaszlink.deviceservice.domain.location;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pl.tomaszlink.deviceservice.domain.common.ListResult;
import pl.tomaszlink.deviceservice.domain.device.repositories.DeviceRepository;
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLastLocationEntity;
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationEntity;
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationResult;
import pl.tomaszlink.deviceservice.domain.location.repositories.DeviceLastLocationRepository;
import pl.tomaszlink.deviceservice.domain.location.repositories.DeviceLocationRepository;
import pl.tomaszlink.deviceservice.exceptions.DeviceLocationNotFoundException;
import pl.tomaszlink.deviceservice.exceptions.DeviceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceLocationService {
    private final DeviceRepository deviceRepository;
    private final DeviceLocationRepository deviceLocationRepository;
    private final DeviceLastLocationRepository deviceLastLocationRepository;

    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "timestamp");

    public ListResult<DeviceLocationResult> getDeviceLocations(UUID id, Integer page, Integer size) {
        boolean deviceExists = this.deviceRepository.existsById(id);

        if(!deviceExists) {
            throw new DeviceNotFoundException(id.toString());
        }

        Pageable pageable = PageRequest.of(page, size, SORT);
        Page<DeviceLocationEntity> resultPage = this.deviceLocationRepository.findAllByDeviceId(id, pageable);

        List<DeviceLocationResult> list = resultPage.getContent().stream()
                .map(DeviceLocationMapper::toResult)
                .toList();

        return new ListResult<>(
                list,
                page,
                size,
                resultPage.getTotalElements(),
                resultPage.getTotalPages()
        );
    }

    public DeviceLocationResult getDeviceLastLocation(@NotNull UUID deviceId) {
        DeviceLastLocationEntity deviceLastLocationEntity = this.deviceLastLocationRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceLocationNotFoundException(deviceId.toString()));

        return DeviceLocationMapper.toResult(deviceLastLocationEntity);
    }
}
