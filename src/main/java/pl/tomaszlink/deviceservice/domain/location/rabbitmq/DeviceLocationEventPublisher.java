package pl.tomaszlink.deviceservice.domain.location.rabbitmq;

import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationEvent;

public interface DeviceLocationEventPublisher {
    void publish(DeviceLocationEvent event);
}
