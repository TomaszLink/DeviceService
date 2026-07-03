package pl.tomaszlink.deviceservice.domain.location.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLastLocationEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface DeviceLastLocationRepository extends JpaRepository<DeviceLastLocationEntity, UUID> {

    @Modifying
    @Query(value = """
        INSERT INTO device_last_locations (
            device_id,
            location_id,
            latitude,
            longitude,
            timestamp,
            received_at,
            updated_at
        ) VALUES (
            :deviceId,
            :locationId,
            :latitude,
            :longitude,
            :timestamp,
            :receivedAt,
            :updatedAt
        )
        ON CONFLICT (device_id)
        DO UPDATE SET
            location_id = :locationId,
            latitude = :latitude,
            longitude = :longitude,
            timestamp = :timestamp,
            received_at = :receivedAt,
            updated_at = :updatedAt
        WHERE device_last_locations.timestamp <= :timestamp
        """, nativeQuery = true)
    int upsertLastLocation(
            UUID deviceId,
            UUID locationId,
            BigDecimal latitude,
            BigDecimal longitude,
            Instant timestamp,
            Instant receivedAt,
            Instant updatedAt
    );
}
