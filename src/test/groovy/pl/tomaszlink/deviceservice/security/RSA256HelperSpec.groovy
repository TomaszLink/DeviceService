package pl.tomaszlink.deviceservice.security

import spock.lang.Specification

import java.security.interfaces.RSAPublicKey
import java.security.spec.InvalidKeySpecException

class RSA256HelperSpec extends Specification {

    RSA256Helper helper = new RSA256Helper()

    def "loadPublicKey parses a PEM encoded RSA public key from the classpath"() {
        given:
        helper.@publicKeyPath = "certs/public_key.pem"

        when:
        RSAPublicKey key = helper.loadPublicKey()

        then:
        key != null
        key.algorithm == "RSA"
        key.modulus.bitLength() > 0
    }

    def "loadPublicKey throws when the key file cannot be found on the classpath"() {
        given:
        helper.@publicKeyPath = "certs/does-not-exist.pem"

        when:
        helper.loadPublicKey()

        then:
        def ex = thrown(IOException)
        ex.message.contains("does-not-exist.pem")
    }

    def "loadPublicKey throws when the PEM content is not a valid key"() {
        given:
        helper.@publicKeyPath = "certs/invalid-key.pem"

        when:
        helper.loadPublicKey()

        then:
        thrown(InvalidKeySpecException)
    }
}
