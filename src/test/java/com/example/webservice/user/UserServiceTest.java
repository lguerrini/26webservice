package com.example.webservice.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {
    @Test
    void hashesPasswordDefaultsRoleAndReturnsNoPassword() {
        UserRepository repository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserService service = new UserService(repository, passwordEncoder);
        UserRequest request = new UserRequest("Ada", "Lovelace", "ada", "secret", null, null, null);

        UserResponse response = service.create(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(userCaptor.capture());
        assertThat(passwordEncoder.matches("secret", userCaptor.getValue().getPasswordHash())).isTrue();
        assertThat(response.role()).isEqualTo("admin");
        assertThat(response.username()).isEqualTo("ada");
    }

    @Test
    void updateWithoutPasswordChangePreservesExistingHash() {
        UserRepository repository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
        String existingHash = passwordEncoder.encode("existing-password");
        User user = new User("Ada", "Lovelace", "ada", existingHash, null, "admin", null);
        when(repository.findById(1)).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserService service = new UserService(repository, passwordEncoder);
        UserUpdateRequest request = new UserUpdateRequest(
                "Augusta", "Lovelace", "ada", null, null, "admin", null);

        service.update(1, request);

        assertThat(user.getFirstname()).isEqualTo("Augusta");
        assertThat(user.getPasswordHash()).isEqualTo(existingHash);
        assertThat(passwordEncoder.matches("existing-password", user.getPasswordHash())).isTrue();
    }
}