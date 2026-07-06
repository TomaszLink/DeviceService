package pl.tomaszlink.deviceservice.domain.device

import org.hibernate.exception.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import pl.tomaszlink.deviceservice.domain.device.models.DeviceEntity
import pl.tomaszlink.deviceservice.domain.device.models.DeviceType
import pl.tomaszlink.deviceservice.domain.device.models.ModifyDeviceCommand
import pl.tomaszlink.deviceservice.domain.device.models.RegisterDeviceCommand
import pl.tomaszlink.deviceservice.domain.device.repositories.DeviceRepository
import pl.tomaszlink.deviceservice.exceptions.ConcurrentModificationException
import pl.tomaszlink.deviceservice.exceptions.DeviceAlreadyExistsException
import pl.tomaszlink.deviceservice.exceptions.DeviceNotFoundException
import spock.lang.Specification

import java.sql.SQLException

class DeviceServiceSpec extends Specification {

    DeviceRepository deviceRepository = Mock()
    DeviceService deviceService = new DeviceService(deviceRepository)

    def "getDevices returns an unsorted page when sortField is blank"() {
        given:
        def entity = deviceStub(UUID.randomUUID(), "Device 1", DeviceType.SMARTPHONE, "id-1")

        when:
        def result = deviceService.getDevices(0, 10, null, null)

        then:
        1 * deviceRepository.findAll({ Pageable p -> p.sort.isUnsorted() && p.pageNumber == 0 && p.pageSize == 10 }) >>
                new PageImpl<>([entity], PageRequest.of(0, 10), 1)
        result.content().size() == 1
        result.content()[0].name() == "Device 1"
        result.page() == 0
        result.size() == 10
        result.totalCount() == 1
        result.totalPages() == 1
    }

    def "getDevices treats a blank (non-null) sortField as unsorted"() {
        when:
        deviceService.getDevices(0, 10, "   ", null)

        then:
        1 * deviceRepository.findAll({ Pageable p -> p.sort.isUnsorted() }) >> new PageImpl<>([])
    }

    def "getDevices maps the sort field and direction onto the pageable"() {
        when:
        deviceService.getDevices(1, 5, "unique_identifier", "DESC")

        then:
        1 * deviceRepository.findAll({ Pageable p ->
            p.sort == Sort.by(Sort.Direction.DESC, "uniqueIdentifier") && p.pageNumber == 1 && p.pageSize == 5
        }) >> new PageImpl<>([])
    }

    def "getDevices defaults to ascending direction for an unrecognized sort direction"() {
        when:
        deviceService.getDevices(0, 5, "name", "sideways")

        then:
        1 * deviceRepository.findAll({ Pageable p -> p.sort == Sort.by(Sort.Direction.ASC, "name") }) >> new PageImpl<>([])
    }

    def "getSpecificDevice returns the mapped device when it exists"() {
        given:
        def id = UUID.randomUUID()
        def entity = deviceStub(id, "Device", DeviceType.TABLET, "unique-1")

        when:
        def result = deviceService.getSpecificDevice(id)

        then:
        1 * deviceRepository.findById(id) >> Optional.of(entity)
        result.id() == id
        result.name() == "Device"
        result.type() == DeviceType.TABLET
        result.uniqueIdentifier() == "unique-1"
    }

    def "getSpecificDevice throws DeviceNotFoundException when the device is missing"() {
        given:
        def id = UUID.randomUUID()

        when:
        deviceService.getSpecificDevice(id)

        then:
        1 * deviceRepository.findById(id) >> Optional.empty()
        thrown(DeviceNotFoundException)
    }

    def "modifyDevice updates and saves without re-checking the identifier when it is unchanged"() {
        given:
        def id = UUID.randomUUID()
        def entity = deviceStub(id, "Old name", DeviceType.SMARTPHONE, "same-id")
        def command = new ModifyDeviceCommand(id, "New name", DeviceType.TABLET, "same-id")

        when:
        def result = deviceService.modifyDevice(command)

        then:
        1 * deviceRepository.findById(id) >> Optional.of(entity)
        0 * deviceRepository.existsByUniqueIdentifier(_)
        1 * deviceRepository.saveAndFlush(entity) >> entity
        result.name() == "New name"
        result.type() == DeviceType.TABLET
        result.uniqueIdentifier() == "same-id"
    }

    def "modifyDevice checks identifier availability when it changes and saves when it is available"() {
        given:
        def id = UUID.randomUUID()
        def entity = deviceStub(id, "Old name", DeviceType.SMARTPHONE, "old-id")
        def command = new ModifyDeviceCommand(id, "New name", DeviceType.TABLET, "new-id")

        when:
        def result = deviceService.modifyDevice(command)

        then:
        1 * deviceRepository.findById(id) >> Optional.of(entity)
        1 * deviceRepository.existsByUniqueIdentifier("new-id") >> false
        1 * deviceRepository.saveAndFlush(entity) >> entity
        result.name() == "New name"
        result.type() == DeviceType.TABLET
        result.uniqueIdentifier() == "new-id"
    }

    def "modifyDevice checks identifier availability when it changes and throws when it is already taken"() {
        given:
        def id = UUID.randomUUID()
        def entity = deviceStub(id, "Name", DeviceType.OTHER, "old-id")
        def command = new ModifyDeviceCommand(id, "Name", DeviceType.OTHER, "new-id")

        when:
        deviceService.modifyDevice(command)

        then:
        1 * deviceRepository.findById(id) >> Optional.of(entity)
        1 * deviceRepository.existsByUniqueIdentifier("new-id") >> true
        0 * deviceRepository.saveAndFlush(_)
        thrown(DeviceAlreadyExistsException)
    }

    def "modifyDevice throws DeviceNotFoundException when the device does not exist"() {
        given:
        def id = UUID.randomUUID()
        def command = new ModifyDeviceCommand(id, "Name", DeviceType.OTHER, "id")

        when:
        deviceService.modifyDevice(command)

        then:
        1 * deviceRepository.findById(id) >> Optional.empty()
        thrown(DeviceNotFoundException)
    }

    def "modifyDevice translates optimistic locking failures into ConcurrentModificationException"() {
        given:
        def id = UUID.randomUUID()
        def entity = deviceStub(id, "Name", DeviceType.OTHER, "same-id")
        def command = new ModifyDeviceCommand(id, "Name", DeviceType.OTHER, "same-id")

        when:
        deviceService.modifyDevice(command)

        then:
        1 * deviceRepository.findById(id) >> Optional.of(entity)
        1 * deviceRepository.saveAndFlush(entity) >> { throw new OptimisticLockingFailureException("stale") }
        thrown(ConcurrentModificationException)
    }

    def "registerNewDevice saves and returns the new device when the identifier is available"() {
        given:
        def command = new RegisterDeviceCommand("New device", DeviceType.GPS_TRACKER, "gps-1")

        when:
        def result = deviceService.registerNewDevice(command)

        then:
        1 * deviceRepository.existsByUniqueIdentifier("gps-1") >> false
        1 * deviceRepository.saveAndFlush(_ as DeviceEntity) >> { DeviceEntity e -> e }
        result.name() == "New device"
        result.type() == DeviceType.GPS_TRACKER
        result.uniqueIdentifier() == "gps-1"
    }

    def "registerNewDevice throws DeviceAlreadyExistsException without saving when the identifier is taken"() {
        given:
        def command = new RegisterDeviceCommand("Device", DeviceType.OTHER, "dup-1")

        when:
        deviceService.registerNewDevice(command)

        then:
        1 * deviceRepository.existsByUniqueIdentifier("dup-1") >> true
        0 * deviceRepository.saveAndFlush(_)
        thrown(DeviceAlreadyExistsException)
    }

    def "registerNewDevice translates a unique constraint violation raised during save into DeviceAlreadyExistsException"() {
        given:
        def command = new RegisterDeviceCommand("Device", DeviceType.OTHER, "race-1")
        def constraintViolation = new ConstraintViolationException("duplicate", new SQLException(), "uk_devices_unique_identifier")

        when:
        deviceService.registerNewDevice(command)

        then:
        1 * deviceRepository.existsByUniqueIdentifier("race-1") >> false
        1 * deviceRepository.saveAndFlush(_ as DeviceEntity) >> { throw new DataIntegrityViolationException("dup", constraintViolation) }
        thrown(DeviceAlreadyExistsException)
    }

    def "registerNewDevice rethrows DataIntegrityViolationException for unrelated constraint violations"() {
        given:
        def command = new RegisterDeviceCommand("Device", DeviceType.OTHER, "other-1")
        def constraintViolation = new ConstraintViolationException("other", new SQLException(), "some_other_constraint")

        when:
        deviceService.registerNewDevice(command)

        then:
        1 * deviceRepository.existsByUniqueIdentifier("other-1") >> false
        1 * deviceRepository.saveAndFlush(_ as DeviceEntity) >> { throw new DataIntegrityViolationException("dup", constraintViolation) }
        thrown(DataIntegrityViolationException)
    }

    def "registerNewDevice translates optimistic locking failures into ConcurrentModificationException"() {
        given:
        def command = new RegisterDeviceCommand("Device", DeviceType.OTHER, "id-1")

        when:
        deviceService.registerNewDevice(command)

        then:
        1 * deviceRepository.existsByUniqueIdentifier("id-1") >> false
        1 * deviceRepository.saveAndFlush(_ as DeviceEntity) >> { throw new OptimisticLockingFailureException("stale") }
        thrown(ConcurrentModificationException)
    }

    private DeviceEntity deviceStub(UUID id, String name, DeviceType type, String uniqueIdentifier) {
        def entity = new DeviceEntity()
        entity.name = name
        entity.type = type
        entity.uniqueIdentifier = uniqueIdentifier
        entity.@id = id
        return entity
    }
}
