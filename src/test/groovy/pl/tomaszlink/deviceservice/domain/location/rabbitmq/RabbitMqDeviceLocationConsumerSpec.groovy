package pl.tomaszlink.deviceservice.domain.location.rabbitmq

import pl.tomaszlink.deviceservice.domain.location.DeviceLocationProcessingService
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationEvent
import spock.lang.Specification

import java.time.Instant

class RabbitMqDeviceLocationConsumerSpec extends Specification {

    DeviceLocationProcessingService processingService = Mock()
    RabbitMqDeviceLocationConsumer consumer = new RabbitMqDeviceLocationConsumer(processingService)

    def "consume delegates the received event to the processing service"() {
        given:
        def event = new DeviceLocationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(52.23),
                BigDecimal.valueOf(21.01),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:01Z")
        )

        when:
        consumer.consume(event)

        then:
        1 * processingService.process(event)
        0 * processingService._
    }
}
