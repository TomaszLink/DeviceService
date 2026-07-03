package pl.tomaszlink.deviceservice.exceptions;

public class DeviceLocationNotFoundException extends RuntimeException {
    public DeviceLocationNotFoundException(String id) {
        super("Device location with device id " + id + " not found");
    }
}
