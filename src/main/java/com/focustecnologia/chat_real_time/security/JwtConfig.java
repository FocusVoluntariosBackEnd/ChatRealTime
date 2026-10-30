package com.focustecnologia.chat_real_time.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Value("${JWT_SECRET}")
    private String secret;

    @Bean
    public SecretKey secretKey() {
        byte[] decodeKey = Base64.getDecoder().decode(secret);

        if(decodeKey.length < 32) {
            throw new IllegalStateException("A chave JWT deve ter pelo menos 32 bytes");
        }

        return new SecretKeySpec(decodeKey, "HmacSHA256");

    }

    @Bean
    public JwtDecoder jwtDecoder (SecretKey secretKey, JwtProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(properties.issuer())
        );

        return decoder;

    }

}
