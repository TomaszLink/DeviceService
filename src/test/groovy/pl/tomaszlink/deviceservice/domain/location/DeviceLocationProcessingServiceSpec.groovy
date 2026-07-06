package pl.tomaszlink.deviceservice.domain.location

import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationEvent
import pl.tomaszlink.deviceservice.domain.location.repositories.DeviceLastLocationRepository
import pl.tomaszlink.deviceservice.domain.location.repositories.DeviceLocationRepository
import spock.lang.Specification

import java.time.Instant

class DeviceLocationProcessingServiceSpec extends Specification {

    DeviceLastLocationRepository deviceLastLocationRepository = Mock()
    DeviceLocationRepository deviceLocationRepository = Mock()
    DeviceLocationProcessingService service = new DeviceLocationProcessingService(deviceLastLocationRepository, deviceLocationRepository)

    def event = new DeviceLocationEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            BigDecimal.valueOf(52.23),
            BigDecimal.valueOf(21.01),
            Instant.parse("2026-01-01T00:00:00Z"),
            Instant.parse("2026-01-01T00:00:01Z")
    )

    def "process saves the location history and updates the last known location"() {
        when:
        service.process(event)

        then:
        1 * deviceLocationRepository.saveIfNotProcessed(
                event.id(), event.deviceId(), event.latitude(), event.longitude(), event.timestamp(), event.receivedAt(), _ as Instant
        ) >> 1
        1 * deviceLastLocationRepository.upsertLastLocation(
                event.deviceId(), event.id(), event.latitude(), event.longitude(), event.timestamp(), event.receivedAt(), _ as Instant
        ) >> 1
    }

    def "process skips updating the last location when the event was already processed"() {
        when:
        service.process(event)

        then:
        1 * deviceLocationRepository.saveIfNotProcessed(
                event.id(), event.deviceId(), event.latitude(), event.longitude(), event.timestamp(), event.receivedAt(), _ as Instant
        ) >> 0
        0 * deviceLastLocationRepository.upsertLastLocation(*_)
    }

    def "process does not fail when a newer location already won the last-location upsert"() {
        when:
        service.process(event)

        then:
        1 * deviceLocationRepository.saveIfNotProcessed(
                event.id(), event.deviceId(), event.latitude(), event.longitude(), event.timestamp(), event.receivedAt(), _ as Instant
        ) >> 1
        1 * deviceLastLocationRepository.upsertLastLocation(
                event.deviceId(), event.id(), event.latitude(), event.longitude(), event.timestamp(), event.receivedAt(), _ as Instant
        ) >> 0
        noExceptionThrown()
    }
}
