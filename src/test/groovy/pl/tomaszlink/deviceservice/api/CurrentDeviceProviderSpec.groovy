package pl.tomaszlink.deviceservice.api

import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import spock.lang.Specification

import java.time.Instant

class CurrentDeviceProviderSpec extends Specification {

    CurrentDeviceProvider provider = new CurrentDeviceProvider()

    def cleanup() {
        SecurityContextHolder.clearContext()
    }

    def "getCurrentDeviceId returns the device id from the JWT claim"() {
        given:
        def deviceId = UUID.randomUUID()
        authenticateWithJwt(deviceId.toString())

        expect:
        provider.getCurrentDeviceId() == deviceId
    }

    def "getCurrentDeviceId throws AuthorizationDeniedException when there is no authentication"() {
        given:
        SecurityContextHolder.clearContext()

        when:
        provider.getCurrentDeviceId()

        then:
        thrown(AuthorizationDeniedException)
    }

    def "getCurrentDeviceId throws AuthorizationDeniedException when the principal is not a JWT"() {
        given:
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", "password"))

        when:
        provider.getCurrentDeviceId()

        then:
        thrown(AuthorizationDeniedException)
    }

    def "getCurrentDeviceId throws AuthorizationDeniedException when the device_id claim is missing"() {
        given:
        authenticateWithJwt(null)

        when:
        provider.getCurrentDeviceId()

        then:
        thrown(AuthorizationDeniedException)
    }

    def "getCurrentDeviceId throws AuthorizationDeniedException when the device_id claim is not a valid UUID"() {
        given:
        authenticateWithJwt("not-a-uuid")

        when:
        provider.getCurrentDeviceId()

        then:
        thrown(AuthorizationDeniedException)
    }

    private static void authenticateWithJwt(String deviceId) {
        def builder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("dummy", "value")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
        if (deviceId != null) {
            builder.claim("device_id", deviceId)
        }
        def jwt = builder.build()
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(jwt, null))
    }
}
