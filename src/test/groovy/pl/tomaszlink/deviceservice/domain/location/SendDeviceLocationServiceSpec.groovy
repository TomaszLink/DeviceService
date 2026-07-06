package pl.tomaszlink.deviceservice.domain.location

import pl.tomaszlink.deviceservice.domain.device.repositories.DeviceRepository
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationEvent
import pl.tomaszlink.deviceservice.domain.location.models.SendDeviceLocationCommand
import pl.tomaszlink.deviceservice.domain.location.rabbitmq.DeviceLocationEventPublisher
import pl.tomaszlink.deviceservice.exceptions.DeviceNotFoundException
import spock.lang.Specification

import java.time.Instant

class SendDeviceLocationServiceSpec extends Specification {

    DeviceRepository deviceRepository = Mock()
    DeviceLocationEventPublisher eventPublisher = Mock()
    SendDeviceLocationService service = new SendDeviceLocationService(deviceRepository, eventPublisher)

    def "sendDeviceLocation publishes an event for an existing device"() {
        given:
        def deviceId = UUID.randomUUID()
        def command = new SendDeviceLocationCommand(52.23d, 21.01d, Instant.parse("2026-01-01T00:00:00Z"))

        when:
        service.sendDeviceLocation(deviceId, command)

        then:
        1 * deviceRepository.existsById(deviceId) >> true
        1 * eventPublisher.publish({ DeviceLocationEvent event ->
            event.deviceId() == deviceId &&
                    event.latitude() == BigDecimal.valueOf(52.23d) &&
                    event.longitude() == BigDecimal.valueOf(21.01d) &&
                    event.timestamp() == command.timestamp() &&
                    event.id() != null &&
                    event.receivedAt() != null
        })
    }

    def "sendDeviceLocation throws DeviceNotFoundException and does not publish when the device is missing"() {
        given:
        def deviceId = UUID.randomUUID()
        def command = new SendDeviceLocationCommand(1d, 1d, Instant.now())

        when:
        service.sendDeviceLocation(deviceId, command)

        then:
        1 * deviceRepository.existsById(deviceId) >> false
        0 * eventPublisher.publish(_)
        thrown(DeviceNotFoundException)
    }
}
