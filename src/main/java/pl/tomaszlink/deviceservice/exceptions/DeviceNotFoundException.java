package pl.tomaszlink.deviceservice.exceptions;

public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(String id) {
        super("Device with id " + id + " not found");
    }
}
