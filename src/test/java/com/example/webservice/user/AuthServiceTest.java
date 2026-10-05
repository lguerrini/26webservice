package com.example.webservice.user;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

class AuthServiceTest {
    @Test
    void registrationAlwaysCreatesReadonlyUserWithHashedPassword() {
        UserRepository repository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
        JwtEncoder jwtEncoder = mock(JwtEncoder.class);
        when(repository.existsByUsername("ada")).thenReturn(false);
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AuthService service = new AuthService(repository, passwordEncoder, jwtEncoder);

        UserResponse response = service.register(new RegisterRequest("Ada", "Lovelace", "ada", "secretpass", null));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(userCaptor.capture());
        assertThat(response.role()).isEqualTo("readonly");
        assertThat(passwordEncoder.matches("secretpass", userCaptor.getValue().getPasswordHash())).isTrue();
    }

    @Test
    void loginIssuesBearerTokenForValidPassword() {
        UserRepository repository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
        JwtEncoder jwtEncoder = mock(JwtEncoder.class);
        User user = new User("Ada", "Lovelace", "ada", passwordEncoder.encode("secretpass"), null, "readonly", null);
        when(repository.findByUsername("ada")).thenReturn(Optional.of(user));
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(
            Jwt.withTokenValue("signed-token").header("alg", "HS256").claim("sub", "ada").build());
        AuthService service = new AuthService(repository, passwordEncoder, jwtEncoder);

        AuthTokenResponse response = service.login(new LoginRequest("ada", "secretpass"));

        assertThat(response.accessToken()).isEqualTo("signed-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void mapsLegacyAdministratorRoleToAdminAuthority() {
        UserRepository repository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
        JwtEncoder jwtEncoder = mock(JwtEncoder.class);
        User user = new User("Ada", "Lovelace", "ada", passwordEncoder.encode("secretpass"), null,
            "administrator", null);
        when(repository.findByUsername("ada")).thenReturn(Optional.of(user));
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(
            Jwt.withTokenValue("signed-token").header("alg", "HS256").claim("sub", "ada").build());
        AuthService service = new AuthService(repository, passwordEncoder, jwtEncoder);

        service.login(new LoginRequest("ada", "secretpass"));

        ArgumentCaptor<JwtEncoderParameters> parametersCaptor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(parametersCaptor.capture());
        Object rolesClaim = parametersCaptor.getValue().getClaims().getClaim("roles");
        assertThat(rolesClaim).isEqualTo(List.of("ADMIN"));
    }

    @Test
    void loginUpgradesLegacyPlaintextPasswordToBcrypt() {
        UserRepository repository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
        JwtEncoder jwtEncoder = mock(JwtEncoder.class);
        User user = new User("Ada", "Lovelace", "ada", "legacy-password", null, "readonly", null);
        when(repository.findByUsername("ada")).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(
                Jwt.withTokenValue("signed-token").header("alg", "HS256").claim("sub", "ada").build());
        AuthService service = new AuthService(repository, passwordEncoder, jwtEncoder);

        service.login(new LoginRequest("ada", "legacy-password"));

        verify(repository).save(user);
        assertThat(passwordEncoder.matches("legacy-password", user.getPasswordHash())).isTrue();
        assertThat(user.getPasswordHash()).startsWith("$2");
    }
}