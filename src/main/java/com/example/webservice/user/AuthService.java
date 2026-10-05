package com.example.webservice.user;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private static final Duration TOKEN_LIFETIME = Duration.ofMinutes(30);
    private static final Pattern BCRYPT_HASH = Pattern.compile("\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}");

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AuthService(UserRepository repository, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    public UserResponse register(RegisterRequest request) {
        if (repository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        User user = new User(
                request.firstname(),
                request.lastname(),
                request.username(),
                passwordEncoder.encode(request.password()),
                request.email(),
                "readonly",
                null);
        return toResponse(repository.save(user));
    }

    public AuthTokenResponse login(LoginRequest request) {
        User user = repository.findByUsername(request.username())
            .filter(candidate -> passwordMatchesAndUpgrade(candidate, request.password()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(TOKEN_LIFETIME);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("mysql-rest-service")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getUsername())
                .claim("roles", List.of(resolveAuthorityRole(user.getRole())))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new AuthTokenResponse(token, "Bearer", expiresAt);
    }

    private String resolveAuthorityRole(String role) {
        if (role == null) return "READONLY";
        return switch (role.trim().toLowerCase(Locale.ROOT)) {
            case "admin", "administrator" -> "ADMIN";
            case "readonly", "read-only", "read_only" -> "READONLY";
            default -> "READONLY";
        };
    }

    private boolean passwordMatchesAndUpgrade(User user, String rawPassword) {
        String storedPassword = user.getPasswordHash();
        if (storedPassword == null) return false;
        if (BCRYPT_HASH.matcher(storedPassword).matches()) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        boolean matches = MessageDigest.isEqual(
                rawPassword.getBytes(StandardCharsets.UTF_8),
                storedPassword.getBytes(StandardCharsets.UTF_8));
        if (matches) {
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            repository.save(user);
        }
        return matches;
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getTUsercol());
    }
}