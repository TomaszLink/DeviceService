package pl.tomaszlink.deviceservice.domain.location.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationReceivedEvent;

@Component
@RequiredArgsConstructor
public class KafkaDeviceLocationEventPublisher implements DeviceLocationEventPublisher {
    private final KafkaTemplate<String, DeviceLocationReceivedEvent> kafkaTemplate;

    @Value("${app.kafka.topics.device-locations}")
    private String deviceLocationsTopic;

    @Override
    public void publish(DeviceLocationReceivedEvent event) {
        String key = event.deviceId().toString();
        kafkaTemplate.send(deviceLocationsTopic, key, event);
    }
}
