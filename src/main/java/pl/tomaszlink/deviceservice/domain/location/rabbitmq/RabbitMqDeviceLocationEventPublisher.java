package pl.tomaszlink.deviceservice.domain.location.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqDeviceLocationEventPublisher implements DeviceLocationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    @Override
    public void publish(DeviceLocationEvent event) {
        rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                event
        );
        log.info("Sent device location event with id: {}", event.id());
    }
}
