package pl.tomaszlink.deviceservice.api;

import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentDeviceProvider {

    private static final String DEVICE_ID_CLAIM = "device_id";

    public UUID getCurrentDeviceId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthorizationDeniedException("Missing JWT authentication");
        }

        String deviceId = jwt.getClaimAsString(DEVICE_ID_CLAIM);

        if (deviceId == null || deviceId.isBlank()) {
            throw new AuthorizationDeniedException("Missing device_id claim");
        }

        try {
            return UUID.fromString(deviceId);
        } catch (IllegalArgumentException exception) {
            throw new AuthorizationDeniedException("Invalid device_id claim");
        }
    }
}
