package pl.tomaszlink.deviceservice.domain.location.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "device_last_locations")
@Getter
public class DeviceLastLocationEntity {

    @Id
    @Column(name = "device_id", nullable = false, updatable = false)
    private UUID deviceId;

    @Column(name = "location_id", nullable = false)
    private UUID locationId;

    @Column(name = "latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public DeviceLastLocationEntity() {}

    private DeviceLastLocationEntity(@NotNull UUID deviceId, @NotNull UUID locationId, @NotNull BigDecimal latitude, @NotNull BigDecimal longitude, @NotNull Instant timestamp, @NotNull Instant receivedAt) {
        this.deviceId = deviceId;
        this.locationId = locationId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.receivedAt = receivedAt;
    }

    public static DeviceLastLocationEntity from(@NotNull UUID deviceId, @NotNull UUID locationId, @NotNull BigDecimal latitude, @NotNull BigDecimal longitude, @NotNull Instant timestamp, @NotNull Instant receivedAt) {
        return new DeviceLastLocationEntity(deviceId, locationId, latitude, longitude, timestamp, receivedAt);
    }

}
