package com.focustecnologia.chat_real_time.auth.service;

import com.focustecnologia.chat_real_time.auth.service.input.UserInput;
import com.focustecnologia.chat_real_time.auth.service.output.UserOutput;
import com.focustecnologia.chat_real_time.exception.EmailAlreadyInUseException;
import com.focustecnologia.chat_real_time.user.entity.User;
import com.focustecnologia.chat_real_time.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private RegisterService service;

    @Test
    @DisplayName("Should throw EmailAlreadyInUseException.class when email already exists")
    void emailAlreadyExists() {
        UserInput input = new UserInput("Test","test@gmail.com", "password");
        User user = new User("Test", "test@gmail.com", "password");

        when(userRepository.findByEmail(input.email())).thenReturn(Optional.of(user));

        assertThrows(EmailAlreadyInUseException.class, () -> service.execute(input));
    }

    @Test
    @DisplayName("Should register user successfully")
    void registerUserSuccessfully() {
        UserInput input = new UserInput("Test","test@gmail.com", "password");
        User user = new User("Test", "test@gmail.com", "passwordHash");
        UUID expectedId = UUID.randomUUID();
        ReflectionTestUtils.setField(user, "id", expectedId);

        when(userRepository.findByEmail(input.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(input.password())).thenReturn(user.getPasswordHash());
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserOutput output = service.execute(input);

        assertNotNull(output);
        assertEquals(expectedId.toString(), output.id());
        assertEquals(output.name(), input.name());
        assertEquals(output.email(), input.email());
    }

}