package pl.tomaszlink.deviceservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class RSA256Helper {

    @Value("${jwt.public-key}")
    private String publicKeyPath;

    private String readKeyFromResources(String path) throws IOException {
        try (InputStream inputStream = RSA256Helper.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IOException("Cannot find key file: " + path);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private byte[] parsePem(String pemContent) {
        String normalized = pemContent
                .replaceAll("-----BEGIN [A-Z ]+-----", "")
                .replaceAll("-----END [A-Z ]+-----", "")
                .replaceAll("\\s", "");

        return Base64.getDecoder().decode(normalized);
    }

    public RSAPublicKey loadPublicKey() throws Exception {
        String publicKeyPEM = readKeyFromResources(publicKeyPath);
        byte[] decoded = parsePem(publicKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }
}
