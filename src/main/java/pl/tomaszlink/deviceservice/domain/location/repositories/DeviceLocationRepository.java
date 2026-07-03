package pl.tomaszlink.deviceservice.domain.location.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface DeviceLocationRepository extends JpaRepository<DeviceLocationEntity, UUID> {
    @Modifying
    @Query(value = """
            INSERT INTO device_locations (
                id,
                device_id,
                latitude,
                longitude,
                timestamp,
                received_at,
                created_at
            ) VALUES (
                :id,
                :deviceId,
                :latitude,
                :longitude,
                :timestamp,
                :receivedAt,
                :createdAt
            )
            ON CONFLICT (id) DO NOTHING
            """, nativeQuery = true)
    int saveIfNotProcessed(
            UUID id,
            UUID deviceId,
            BigDecimal latitude,
            BigDecimal longitude,
            Instant timestamp,
            Instant receivedAt,
            Instant createdAt
    );

    Page<DeviceLocationEntity> findAllByDeviceId(UUID deviceId, Pageable pageable);
}
