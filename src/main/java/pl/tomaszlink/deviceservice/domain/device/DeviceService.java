package pl.tomaszlink.deviceservice.domain.device;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.tomaszlink.deviceservice.domain.common.ListResult;
import pl.tomaszlink.deviceservice.domain.device.models.DeviceEntity;
import pl.tomaszlink.deviceservice.domain.device.models.DeviceResult;
import pl.tomaszlink.deviceservice.domain.device.models.RegisterDeviceCommand;
import pl.tomaszlink.deviceservice.domain.device.repositories.DeviceRepository;
import pl.tomaszlink.deviceservice.exceptions.DeviceAlreadyExistsException;
import pl.tomaszlink.deviceservice.exceptions.DeviceNotFoundException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;

    @Transactional(readOnly = true)
    public ListResult<DeviceResult> getDevices(int page, int size, String sortField, String sortDirection) {
        Sort sort = Sort.unsorted();
        if (sortField != null && !sortField.isBlank()) {
            Sort.Direction direction = Sort.Direction.DESC.toString().equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direction, DeviceSortHelper.mapSortField(sortField));
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DeviceEntity> resultPage = deviceRepository.findAll(pageable);

        List<DeviceResult> list = resultPage.getContent().stream()
                .map(DeviceMapper::toResult)
                .toList();

        return new ListResult<>(
                list,
                page,
                size,
                resultPage.getTotalElements(),
                resultPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public DeviceResult getSpecificDevice(@NotNull UUID id) {
        return DeviceMapper.toResult(
                this.findDeviceById(id)
        );
    }

    @Transactional
    public DeviceResult modifyDevice(@NotNull UUID id, @NotNull RegisterDeviceCommand registerDeviceCommand) {
        DeviceEntity deviceEntity = this.findDeviceById(id);
        if(!registerDeviceCommand.uniqueIdentifier().equals(deviceEntity.getUniqueIdentifier())) {
            this.checkDeviceUniqueIdentifierAvailability(registerDeviceCommand.uniqueIdentifier());
        }
        deviceEntity.setName(registerDeviceCommand.name());
        deviceEntity.setType(registerDeviceCommand.type());
        deviceEntity.setUniqueIdentifier(registerDeviceCommand.uniqueIdentifier());

        deviceEntity = this.saveDevice(deviceEntity);

        log.info("Device {} has been modified", deviceEntity.getId());

        return DeviceMapper.toResult(deviceEntity);
    }

    @Transactional
    public DeviceResult registerNewDevice(RegisterDeviceCommand registerDeviceCommand) {
        this.checkDeviceUniqueIdentifierAvailability(registerDeviceCommand.uniqueIdentifier());
        DeviceEntity deviceEntity = DeviceEntity.from(registerDeviceCommand);
        deviceEntity = this.saveDevice(deviceEntity);

        log.info("Device {} has been registered with id {}", deviceEntity.getUniqueIdentifier(), deviceEntity.getId());

        return DeviceMapper.toResult(deviceEntity);
    }

    private void checkDeviceUniqueIdentifierAvailability(@NotNull String uniqueIdentifier) {
        if(deviceRepository.existsByUniqueIdentifier(uniqueIdentifier)) {
            throw new DeviceAlreadyExistsException(uniqueIdentifier);
        }
    }

    private DeviceEntity findDeviceById(@NotNull UUID id) {
        return this.deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id.toString()));
    }

    private DeviceEntity saveDevice(@NotNull DeviceEntity deviceEntity) {
        try {
            return this.deviceRepository.saveAndFlush(deviceEntity);
        } catch (DataIntegrityViolationException ex) {
            if(ex.getCause() != null && ex.getCause() instanceof ConstraintViolationException cve && "uk_devices_unique_identifier".equals(cve.getConstraintName())){
                throw new DeviceAlreadyExistsException(deviceEntity.getUniqueIdentifier());
            }
            throw ex;
        }
    }
}
