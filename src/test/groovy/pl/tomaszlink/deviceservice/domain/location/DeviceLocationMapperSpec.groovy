package pl.tomaszlink.deviceservice.domain.location

import pl.tomaszlink.deviceservice.domain.location.models.DeviceLastLocationEntity
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationEntity
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationResult
import pl.tomaszlink.deviceservice.domain.location.models.SendDeviceLocationCommand
import pl.tomaszlink.deviceservice.models.DeviceLocationRequest
import spock.lang.Specification

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class DeviceLocationMapperSpec extends Specification {

    def "toCommand maps a DeviceLocationRequest into a SendDeviceLocationCommand"() {
        given:
        def timestamp = OffsetDateTime.parse("2026-01-01T12:00:00Z")
        def request = new DeviceLocationRequest(52.23d, 21.01d, timestamp)

        when:
        def command = DeviceLocationMapper.toCommand(request)

        then:
        command.latitude() == 52.23d
        command.longitude() == 21.01d
        command.timestamp() == timestamp.toInstant()
    }

    def "toEvent generates a fresh id and a receivedAt timestamp"() {
        given:
        def deviceId = UUID.randomUUID()
        def command = new SendDeviceLocationCommand(52.23d, 21.01d, Instant.parse("2026-01-01T00:00:00Z"))

        when:
        def event = DeviceLocationMapper.toEvent(deviceId, command)

        then:
        event.id() != null
        event.deviceId() == deviceId
        event.latitude() == BigDecimal.valueOf(52.23d)
        event.longitude() == BigDecimal.valueOf(21.01d)
        event.timestamp() == command.timestamp()
        event.receivedAt() != null
    }

    def "toResult maps a DeviceLocationEntity into a DeviceLocationResult"() {
        given:
        def id = UUID.randomUUID()
        def deviceId = UUID.randomUUID()
        def timestamp = Instant.parse("2026-01-01T00:00:00Z")
        def receivedAt = Instant.parse("2026-01-01T00:00:05Z")
        def entity = DeviceLocationEntity.from(id, deviceId, BigDecimal.valueOf(1), BigDecimal.valueOf(2), timestamp, receivedAt)

        when:
        def result = DeviceLocationMapper.toResult(entity)

        then:
        result.id() == id
        result.latitude() == BigDecimal.valueOf(1)
        result.longitude() == BigDecimal.valueOf(2)
        result.timestamp() == timestamp
        result.receivedAt() == receivedAt
    }

    def "toResult maps a DeviceLastLocationEntity into a DeviceLocationResult"() {
        given:
        def deviceId = UUID.randomUUID()
        def locationId = UUID.randomUUID()
        def timestamp = Instant.parse("2026-01-01T00:00:00Z")
        def receivedAt = Instant.parse("2026-01-01T00:00:05Z")
        def entity = DeviceLastLocationEntity.from(deviceId, locationId, BigDecimal.valueOf(3), BigDecimal.valueOf(4), timestamp, receivedAt)

        when:
        def result = DeviceLocationMapper.toResult(entity)

        then:
        result.id() == locationId
        result.latitude() == BigDecimal.valueOf(3)
        result.longitude() == BigDecimal.valueOf(4)
        result.timestamp() == timestamp
        result.receivedAt() == receivedAt
    }

    def "toModel maps a DeviceLocationResult into a DeviceLocationModel using UTC offsets"() {
        given:
        def id = UUID.randomUUID()
        def timestamp = Instant.parse("2026-01-01T00:00:00Z")
        def receivedAt = Instant.parse("2026-01-01T00:00:05Z")
        def result = new DeviceLocationResult(id, BigDecimal.valueOf(1.5), BigDecimal.valueOf(2.5), timestamp, receivedAt)

        when:
        def model = DeviceLocationMapper.toModel(result)

        then:
        model.id == id
        model.latitude == 1.5d
        model.longitude == 2.5d
        model.timestamp == timestamp.atOffset(ZoneOffset.UTC)
        model.receivedAt == receivedAt.atOffset(ZoneOffset.UTC)
    }
}
