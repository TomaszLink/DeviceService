package pl.tomaszlink.deviceservice.domain.location;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.tomaszlink.deviceservice.domain.device.repositories.DeviceRepository;
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationEvent;
import pl.tomaszlink.deviceservice.domain.location.models.SendDeviceLocationCommand;
import pl.tomaszlink.deviceservice.domain.location.rabbitmq.DeviceLocationEventPublisher;
import pl.tomaszlink.deviceservice.exceptions.DeviceNotFoundException;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendDeviceLocationService {
    private final DeviceRepository deviceRepository;
    private final DeviceLocationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public void sendDeviceLocation(UUID deviceId, SendDeviceLocationCommand command) {
        this.checkDeviceExists(deviceId);
        DeviceLocationEvent event = DeviceLocationMapper.toEvent(deviceId, command);
        this.eventPublisher.publish(event);
    }

    private void checkDeviceExists(UUID deviceId) {
        if (!this.deviceRepository.existsById(deviceId)) {
            throw new DeviceNotFoundException(deviceId.toString());
        }
    }
}
