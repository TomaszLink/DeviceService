package pl.tomaszlink.deviceservice.api

import org.springframework.http.HttpStatus
import pl.tomaszlink.deviceservice.domain.common.ListResult
import pl.tomaszlink.deviceservice.domain.location.DeviceLocationService
import pl.tomaszlink.deviceservice.domain.location.SendDeviceLocationService
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationResult
import pl.tomaszlink.deviceservice.domain.location.models.SendDeviceLocationCommand
import pl.tomaszlink.deviceservice.models.DeviceLocationModel
import pl.tomaszlink.deviceservice.models.DeviceLocationRequest
import spock.lang.Specification

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class DeviceLocationControllerSpec extends Specification {

    SendDeviceLocationService sendDeviceLocationService = Mock()
    DeviceLocationService deviceLocationService = Mock()
    CurrentDeviceProvider currentDeviceProvider = Mock()
    DeviceLocationController controller = new DeviceLocationController(sendDeviceLocationService, deviceLocationService, currentDeviceProvider)

    def "sendDeviceLocation resolves the current device and forwards the mapped command"() {
        given:
        def deviceId = UUID.randomUUID()
        def timestamp = OffsetDateTime.parse("2026-01-01T12:00:00Z")
        def request = new DeviceLocationRequest(52.23d, 21.01d, timestamp)

        when:
        def response = controller.sendDeviceLocation(request)

        then:
        1 * currentDeviceProvider.getCurrentDeviceId() >> deviceId
        1 * sendDeviceLocationService.sendDeviceLocation(deviceId, new SendDeviceLocationCommand(52.23d, 21.01d, timestamp.toInstant()))
        response.statusCode == HttpStatus.ACCEPTED
        response.body == null
    }

    def "getDeviceLocations returns the mapped locations with pagination headers"() {
        given:
        def deviceId = UUID.randomUUID()
        def locationId = UUID.randomUUID()
        def timestamp = Instant.parse("2026-01-01T00:00:00Z")
        def receivedAt = Instant.parse("2026-01-01T00:00:05Z")
        def locationResult = new DeviceLocationResult(locationId, BigDecimal.valueOf(1), BigDecimal.valueOf(2), timestamp, receivedAt)
        def listResult = new ListResult<DeviceLocationResult>([locationResult], 0, 10, 1, 1)

        when:
        def response = controller.getDeviceLocations(deviceId, 0, 10)

        then:
        1 * deviceLocationService.getDeviceLocations(deviceId, 0, 10) >> listResult
        response.statusCode == HttpStatus.OK
        response.body == [new DeviceLocationModel()
                .id(locationId)
                .latitude(1.0d)
                .longitude(2.0d)
                .timestamp(timestamp.atOffset(ZoneOffset.UTC))
                .receivedAt(receivedAt.atOffset(ZoneOffset.UTC))]
        response.headers.getFirst("X-Total-Count") == "1"
        response.headers.getFirst("X-Total-Pages") == "1"
    }

    def "getLatestDeviceLocation returns the mapped last location"() {
        given:
        def deviceId = UUID.randomUUID()
        def locationId = UUID.randomUUID()
        def timestamp = Instant.parse("2026-01-01T00:00:00Z")
        def receivedAt = Instant.parse("2026-01-01T00:00:05Z")
        def locationResult = new DeviceLocationResult(locationId, BigDecimal.valueOf(3), BigDecimal.valueOf(4), timestamp, receivedAt)

        when:
        def response = controller.getLatestDeviceLocation(deviceId)

        then:
        1 * deviceLocationService.getDeviceLastLocation(deviceId) >> locationResult
        response.statusCode == HttpStatus.OK
        response.body == new DeviceLocationModel()
                .id(locationId)
                .latitude(3.0d)
                .longitude(4.0d)
                .timestamp(timestamp.atOffset(ZoneOffset.UTC))
                .receivedAt(receivedAt.atOffset(ZoneOffset.UTC))
    }
}
