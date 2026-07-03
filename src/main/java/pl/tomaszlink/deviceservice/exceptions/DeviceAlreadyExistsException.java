package pl.tomaszlink.deviceservice.exceptions;

public class DeviceAlreadyExistsException extends RuntimeException {
    public DeviceAlreadyExistsException(String uniqueIdentifier) {
        super("Device with identifier " + uniqueIdentifier + " already exists");
    }
}
