package pl.tomaszlink.deviceservice.api

import org.springframework.http.HttpStatus
import pl.tomaszlink.deviceservice.domain.common.ListResult
import pl.tomaszlink.deviceservice.domain.device.DeviceService
import pl.tomaszlink.deviceservice.domain.device.models.DeviceResult
import pl.tomaszlink.deviceservice.domain.device.models.DeviceType
import pl.tomaszlink.deviceservice.domain.device.models.ModifyDeviceCommand
import pl.tomaszlink.deviceservice.domain.device.models.RegisterDeviceCommand
import pl.tomaszlink.deviceservice.models.DeviceModel
import pl.tomaszlink.deviceservice.models.ModifyDeviceRequest
import pl.tomaszlink.deviceservice.models.RegisterDeviceRequest
import spock.lang.Specification

class DeviceControllerSpec extends Specification {

    DeviceService deviceService = Mock()
    DeviceController controller = new DeviceController(deviceService)

    def "getDevices returns the mapped devices with pagination headers"() {
        given:
        def id = UUID.randomUUID()
        def deviceResult = new DeviceResult(id, "Device 1", DeviceType.SMARTPHONE, "id-1")
        def listResult = new ListResult<DeviceResult>([deviceResult], 0, 10, 1, 1)

        when:
        def response = controller.getDevices(0, 10, "name", "ASC")

        then:
        1 * deviceService.getDevices(0, 10, "name", "ASC") >> listResult
        response.statusCode == HttpStatus.OK
        response.body == [new DeviceModel().id(id).name("Device 1").type(DeviceModel.TypeEnum.SMARTPHONE).uniqueIdentifier("id-1")]
        response.headers.getFirst("X-Total-Count") == "1"
        response.headers.getFirst("X-Total-Pages") == "1"
    }

    def "getSpecificDevice returns the mapped device"() {
        given:
        def id = UUID.randomUUID()
        def deviceResult = new DeviceResult(id, "Device", DeviceType.TABLET, "unique-1")

        when:
        def response = controller.getSpecificDevice(id)

        then:
        1 * deviceService.getSpecificDevice(id) >> deviceResult
        response.statusCode == HttpStatus.OK
        response.body == new DeviceModel().id(id).name("Device").type(DeviceModel.TypeEnum.TABLET).uniqueIdentifier("unique-1")
    }

    def "modifyDevice maps the request into a command and returns the mapped device"() {
        given:
        def id = UUID.randomUUID()
        def request = new ModifyDeviceRequest("Renamed device", ModifyDeviceRequest.TypeEnum.VEHICLE_TRACKER, "vt-1")
        def deviceResult = new DeviceResult(id, "Renamed device", DeviceType.VEHICLE_TRACKER, "vt-1")

        when:
        def response = controller.modifyDevice(id, request)

        then:
        1 * deviceService.modifyDevice(new ModifyDeviceCommand(id, "Renamed device", DeviceType.VEHICLE_TRACKER, "vt-1")) >> deviceResult
        response.statusCode == HttpStatus.OK
        response.body == new DeviceModel().id(id).name("Renamed device").type(DeviceModel.TypeEnum.VEHICLE_TRACKER).uniqueIdentifier("vt-1")
    }

    def "registerNewDevice maps the request into a command and returns 201 with the mapped device"() {
        given:
        def request = new RegisterDeviceRequest("New device", RegisterDeviceRequest.TypeEnum.GPS_TRACKER, "gps-1")
        def id = UUID.randomUUID()
        def deviceResult = new DeviceResult(id, "New device", DeviceType.GPS_TRACKER, "gps-1")

        when:
        def response = controller.registerNewDevice(request)

        then:
        1 * deviceService.registerNewDevice(new RegisterDeviceCommand("New device", DeviceType.GPS_TRACKER, "gps-1")) >> deviceResult
        response.statusCode == HttpStatus.CREATED
        response.body == new DeviceModel().id(id).name("New device").type(DeviceModel.TypeEnum.GPS_TRACKER).uniqueIdentifier("gps-1")
    }
}
