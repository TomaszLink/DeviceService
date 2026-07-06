package pl.tomaszlink.deviceservice.domain.device

import pl.tomaszlink.deviceservice.domain.device.models.DeviceEntity
import pl.tomaszlink.deviceservice.domain.device.models.DeviceResult
import pl.tomaszlink.deviceservice.domain.device.models.DeviceType
import pl.tomaszlink.deviceservice.models.DeviceModel
import pl.tomaszlink.deviceservice.models.ModifyDeviceRequest
import pl.tomaszlink.deviceservice.models.RegisterDeviceRequest
import spock.lang.Specification

class DeviceMapperSpec extends Specification {

    def "toCommand maps a RegisterDeviceRequest into a RegisterDeviceCommand"() {
        given:
        def request = new RegisterDeviceRequest("Courier phone", RegisterDeviceRequest.TypeEnum.SMARTPHONE, "phone-1")

        when:
        def command = DeviceMapper.toCommand(request)

        then:
        command.name() == "Courier phone"
        command.type() == DeviceType.SMARTPHONE
        command.uniqueIdentifier() == "phone-1"
    }

    def "toCommand maps an id and a ModifyDeviceRequest into a ModifyDeviceCommand"() {
        given:
        def id = UUID.randomUUID()
        def request = new ModifyDeviceRequest("Renamed device", ModifyDeviceRequest.TypeEnum.TABLET, "tablet-1")

        when:
        def command = DeviceMapper.toCommand(id, request)

        then:
        command.id() == id
        command.name() == "Renamed device"
        command.type() == DeviceType.TABLET
        command.uniqueIdentifier() == "tablet-1"
    }

    def "toDeviceModel maps a DeviceResult into a DeviceModel"() {
        given:
        def id = UUID.randomUUID()
        def result = new DeviceResult(id, "Device", DeviceType.VEHICLE_TRACKER, "vt-1")

        when:
        def model = DeviceMapper.toDeviceModel(result)

        then:
        model.id == id
        model.name == "Device"
        model.type == DeviceModel.TypeEnum.VEHICLE_TRACKER
        model.uniqueIdentifier == "vt-1"
    }

    def "toResult maps a DeviceEntity into a DeviceResult"() {
        given:
        def id = UUID.randomUUID()
        def entity = new DeviceEntity()
        entity.name = "Entity device"
        entity.type = DeviceType.OTHER
        entity.uniqueIdentifier = "other-1"
        entity.@id = id

        when:
        def result = DeviceMapper.toResult(entity)

        then:
        result == new DeviceResult(id, "Entity device", DeviceType.OTHER, "other-1")
    }
}
