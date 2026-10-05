package com.example.webservice;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class SecurityConfigurationTest {
    @Test
    void mapsReadonlyJwtRoleToSpringAuthority() {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject("test-user")
                .claim("roles", List.of("READONLY"))
                .build();

        var authentication = new SecurityConfiguration().jwtAuthenticationConverter().convert(jwt);

        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_READONLY");
    }
}