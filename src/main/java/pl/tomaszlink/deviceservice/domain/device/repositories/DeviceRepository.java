package pl.tomaszlink.deviceservice.domain.device.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.tomaszlink.deviceservice.domain.device.models.DeviceEntity;

import java.util.UUID;

public interface DeviceRepository extends JpaRepository<DeviceEntity, UUID> {
    boolean existsByUniqueIdentifier(String uniqueIdentifier);
}
