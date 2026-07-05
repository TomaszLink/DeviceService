package pl.tomaszlink.deviceservice.domain.location.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pl.tomaszlink.deviceservice.domain.location.DeviceLocationProcessingService;
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqDeviceLocationConsumer {
    private final DeviceLocationProcessingService deviceLocationProcessingService;

    @RabbitListener(queues = "${app.rabbitmq.queue}", containerFactory = "rabbitListenerContainerFactory")
    public void consume(DeviceLocationEvent event) {
        log.info("Received device location event with id: {}", event.id());
        deviceLocationProcessingService.process(event);
    }
}
