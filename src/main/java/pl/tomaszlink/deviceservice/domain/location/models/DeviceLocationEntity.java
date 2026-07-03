package pl.tomaszlink.deviceservice.domain.location.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "device_locations")
@Getter
public class DeviceLocationEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "device_id", nullable = false, updatable = false)
    private UUID deviceId;

    @Column(name = "latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public DeviceLocationEntity() {}

    private DeviceLocationEntity(@NotNull UUID id, @NotNull UUID deviceId, @NotNull BigDecimal latitude, @NotNull BigDecimal longitude, @NotNull Instant timestamp, @NotNull Instant receivedAt) {
        this.id = id;
        this.deviceId = deviceId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.receivedAt = receivedAt;
    }

    public static DeviceLocationEntity from(@NotNull UUID id, @NotNull UUID deviceId, @NotNull BigDecimal latitude, @NotNull BigDecimal longitude, @NotNull Instant timestamp, @NotNull Instant receivedAt) {
        return new DeviceLocationEntity(id, deviceId, latitude, longitude, timestamp, receivedAt);
    }
}
