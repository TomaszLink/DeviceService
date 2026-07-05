package pl.tomaszlink.deviceservice.exceptions;

import java.util.UUID;

public class DeviceLocationPublishException extends RuntimeException {
    public DeviceLocationPublishException(UUID eventId, Throwable cause) {
        super("Device location event with id " + eventId + " could not be confirmed by the broker", cause);
    }
}
