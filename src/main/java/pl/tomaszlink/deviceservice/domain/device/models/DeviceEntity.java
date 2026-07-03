package pl.tomaszlink.deviceservice.domain.device.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "devices")
@Getter
public class DeviceEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 128)
    @Setter
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    @Setter
    private DeviceType type;

    @Column(name = "unique_identifier", nullable = false, unique = true, length = 128)
    @Setter
    private String uniqueIdentifier;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public DeviceEntity() {}

    private DeviceEntity(@NotNull String name, @NotNull DeviceType type, @NotNull String uniqueIdentifier) {
        this.name = name;
        this.type = type;
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public static DeviceEntity from(@NotNull RegisterDeviceCommand command) {
        return new DeviceEntity(command.name(),  command.type(), command.uniqueIdentifier());
    }
}
