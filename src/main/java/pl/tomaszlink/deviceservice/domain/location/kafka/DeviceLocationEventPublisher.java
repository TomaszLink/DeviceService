package pl.tomaszlink.deviceservice.domain.location.kafka;

import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationReceivedEvent;

public interface DeviceLocationEventPublisher {
    void publish(DeviceLocationReceivedEvent event);
}
