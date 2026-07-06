package pl.tomaszlink.deviceservice.domain.location.rabbitmq

import org.springframework.amqp.AmqpException
import org.springframework.amqp.rabbit.core.RabbitOperations
import org.springframework.amqp.rabbit.core.RabbitTemplate
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationEvent
import pl.tomaszlink.deviceservice.exceptions.DeviceLocationPublishException
import spock.lang.Specification

import java.time.Instant

class RabbitMqDeviceLocationEventPublisherSpec extends Specification {

    RabbitTemplate rabbitTemplate = Mock()
    RabbitMqDeviceLocationEventPublisher publisher = new RabbitMqDeviceLocationEventPublisher(rabbitTemplate)

    def event = new DeviceLocationEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            BigDecimal.valueOf(52.23),
            BigDecimal.valueOf(21.01),
            Instant.parse("2026-01-01T00:00:00Z"),
            Instant.parse("2026-01-01T00:00:01Z")
    )

    def setup() {
        publisher.@exchange = "device-location-exchange"
        publisher.@routingKey = "device-location-routing-key"
    }

    def "publish sends the event to the configured exchange and routing key and waits for broker confirmation"() {
        given:
        def rabbitOperations = Mock(RabbitOperations)

        when:
        publisher.publish(event)

        then:
        1 * rabbitTemplate.invoke(_ as RabbitOperations.OperationsCallback) >> {
            RabbitOperations.OperationsCallback callback -> callback.doInRabbit(rabbitOperations)
        }
        1 * rabbitOperations.convertAndSend("device-location-exchange", "device-location-routing-key", event)
        1 * rabbitOperations.waitForConfirmsOrDie(5000L)
    }

    def "publish wraps a broker failure into DeviceLocationPublishException"() {
        when:
        publisher.publish(event)

        then:
        1 * rabbitTemplate.invoke(_ as RabbitOperations.OperationsCallback) >> { throw new AmqpException("broker unavailable") }
        def ex = thrown(DeviceLocationPublishException)
        ex.cause instanceof AmqpException
        ex.message.contains(event.id().toString())
    }
}
