package pl.tomaszlink.deviceservice.domain.location.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationEvent;
import pl.tomaszlink.deviceservice.exceptions.DeviceLocationPublishException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqDeviceLocationEventPublisher implements DeviceLocationEventPublisher {

    private static final long CONFIRM_TIMEOUT_MS = 5_000;

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    @Override
    public void publish(DeviceLocationEvent event) {
        try {
            rabbitTemplate.invoke(operations -> {
                operations.convertAndSend(exchange, routingKey, event);
                operations.waitForConfirmsOrDie(CONFIRM_TIMEOUT_MS);
                return null;
            });
        } catch (AmqpException ex) {
            throw new DeviceLocationPublishException(event.id(), ex);
        }

        log.info("Sent device location event with id: {}", event.id());
    }
}
