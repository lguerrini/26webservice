package com.example.webservice.user;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse findById(Integer id) {
        return toResponse(requireUser(id));
    }

    public UserResponse create(UserRequest request) {
        User user = new User(
                request.firstname(),
                request.lastname(),
                request.username(),
                passwordEncoder.encode(request.password()),
                request.email(),
                resolveRole(request.role()),
                request.tUsercol());
        return toResponse(repository.save(user));
    }

    public UserResponse update(Integer id, UserUpdateRequest request) {
        User user = requireUser(id);
        user.setFirstname(request.firstname());
        user.setLastname(request.lastname());
        user.setUsername(request.username());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        user.setEmail(request.email());
        user.setRole(resolveRole(request.role()));
        user.setTUsercol(request.tUsercol());
        return toResponse(repository.save(user));
    }

    public void delete(Integer id) {
        repository.delete(requireUser(id));
    }

    private User requireUser(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private String resolveRole(String role) {
        return role == null || role.isBlank() ? "admin" : role;
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