package pl.tomaszlink.deviceservice.domain.location;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationEvent;
import pl.tomaszlink.deviceservice.domain.location.repositories.DeviceLastLocationRepository;
import pl.tomaszlink.deviceservice.domain.location.repositories.DeviceLocationRepository;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceLocationProcessingService {
    private final DeviceLastLocationRepository deviceLastLocationRepository;
    private final DeviceLocationRepository deviceLocationRepository;

    @Transactional
    public void process(@NotNull DeviceLocationEvent deviceLocationEvent) {
        Instant now = Instant.now();

        int insertedRows = this.deviceLocationRepository.saveIfNotProcessed(
                    deviceLocationEvent.id(),
                    deviceLocationEvent.deviceId(),
                    deviceLocationEvent.latitude(),
                    deviceLocationEvent.longitude(),
                    deviceLocationEvent.timestamp(),
                    deviceLocationEvent.receivedAt(),
                    now
            );
        if (insertedRows == 0) {
            log.info("Device location event with id {} already processed.", deviceLocationEvent.id());
            return;
        }

        int updatedRows = this.deviceLastLocationRepository.upsertLastLocation(
                deviceLocationEvent.deviceId(),
                deviceLocationEvent.id(),
                deviceLocationEvent.latitude(),
                deviceLocationEvent.longitude(),
                deviceLocationEvent.timestamp(),
                deviceLocationEvent.receivedAt(),
                now
        );

        if (updatedRows == 0) {
            log.info("Device location event with id {} saved to history, but last location was not updated because event is older.", deviceLocationEvent.id());
            return;
        }

        log.info("Device location event with id {} processed", deviceLocationEvent.id());
    }
}
