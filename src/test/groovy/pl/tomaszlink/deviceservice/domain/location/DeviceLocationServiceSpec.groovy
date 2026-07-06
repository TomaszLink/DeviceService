package pl.tomaszlink.deviceservice.domain.location

import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import pl.tomaszlink.deviceservice.domain.device.repositories.DeviceRepository
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLastLocationEntity
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationEntity
import pl.tomaszlink.deviceservice.domain.location.models.DeviceLocationResult
import pl.tomaszlink.deviceservice.domain.location.repositories.DeviceLastLocationRepository
import pl.tomaszlink.deviceservice.domain.location.repositories.DeviceLocationRepository
import pl.tomaszlink.deviceservice.exceptions.DeviceLocationNotFoundException
import pl.tomaszlink.deviceservice.exceptions.DeviceNotFoundException
import spock.lang.Specification

import java.time.Instant

class DeviceLocationServiceSpec extends Specification {

    DeviceRepository deviceRepository = Mock()
    DeviceLocationRepository deviceLocationRepository = Mock()
    DeviceLastLocationRepository deviceLastLocationRepository = Mock()
    DeviceLocationService service = new DeviceLocationService(deviceRepository, deviceLocationRepository, deviceLastLocationRepository)

    def "getDeviceLocations returns mapped locations sorted by timestamp descending when the device exists"() {
        given:
        def deviceId = UUID.randomUUID()
        def locationId = UUID.randomUUID()
        def timestamp = Instant.parse("2026-01-01T00:00:00Z")
        def receivedAt = Instant.parse("2026-01-01T00:00:05Z")
        def entity = DeviceLocationEntity.from(locationId, deviceId, BigDecimal.valueOf(52.23), BigDecimal.valueOf(21.01), timestamp, receivedAt)

        when:
        def result = service.getDeviceLocations(deviceId, 0, 10)

        then:
        1 * deviceRepository.existsById(deviceId) >> true
        1 * deviceLocationRepository.findAllByDeviceId(deviceId, { Pageable p ->
            p.pageNumber == 0 && p.pageSize == 10 && p.sort == Sort.by(Sort.Direction.DESC, "timestamp")
        }) >> new PageImpl<>([entity], PageRequest.of(0, 10), 1)
        result.content() == [new DeviceLocationResult(locationId, BigDecimal.valueOf(52.23), BigDecimal.valueOf(21.01), timestamp, receivedAt)]
        result.page() == 0
        result.size() == 10
        result.totalCount() == 1
        result.totalPages() == 1
    }

    def "getDeviceLocations throws DeviceNotFoundException without querying locations when the device does not exist"() {
        given:
        def deviceId = UUID.randomUUID()

        when:
        service.getDeviceLocations(deviceId, 0, 10)

        then:
        1 * deviceRepository.existsById(deviceId) >> false
        0 * deviceLocationRepository.findAllByDeviceId(*_)
        thrown(DeviceNotFoundException)
    }

    def "getDeviceLastLocation returns the mapped last location when present"() {
        given:
        def deviceId = UUID.randomUUID()
        def locationId = UUID.randomUUID()
        def timestamp = Instant.parse("2026-01-01T00:00:00Z")
        def receivedAt = Instant.parse("2026-01-01T00:00:05Z")
        def entity = DeviceLastLocationEntity.from(deviceId, locationId, BigDecimal.valueOf(1), BigDecimal.valueOf(2), timestamp, receivedAt)

        when:
        def result = service.getDeviceLastLocation(deviceId)

        then:
        1 * deviceLastLocationRepository.findById(deviceId) >> Optional.of(entity)
        result.id() == locationId
        result.latitude() == BigDecimal.valueOf(1)
        result.longitude() == BigDecimal.valueOf(2)
        result.timestamp() == timestamp
        result.receivedAt() == receivedAt
    }

    def "getDeviceLastLocation throws DeviceLocationNotFoundException when there is no last location"() {
        given:
        def deviceId = UUID.randomUUID()

        when:
        service.getDeviceLastLocation(deviceId)

        then:
        1 * deviceLastLocationRepository.findById(deviceId) >> Optional.empty()
        thrown(DeviceLocationNotFoundException)
    }
}
