package com.focustecnologia.chat_real_time.auth.service;

import com.focustecnologia.chat_real_time.auth.service.input.LoginInput;
import com.focustecnologia.chat_real_time.auth.service.output.LoginOutput;
import com.focustecnologia.chat_real_time.exception.InvalidCredentialsException;
import com.focustecnologia.chat_real_time.security.JwtProperties;
import com.focustecnologia.chat_real_time.security.JwtService;
import com.focustecnologia.chat_real_time.user.entity.User;
import com.focustecnologia.chat_real_time.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private JwtProperties jwtProperties;
    @InjectMocks
    private LoginService service;

    @Test
    @DisplayName("Should throw InvalidCredentialsException.class when user not found")
    void userNotFound() {
        LoginInput input = new LoginInput("test@gmail.com", "password");

        when(userRepository.findByEmail(input.email())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> service.execute(input));
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException.class when password is incorrect")
    void passwordIncorrect() {
        LoginInput input = new LoginInput("test@gmail.com", "incorrect");
        User user = new User("Test","test@gmail.com", "password");

        when(userRepository.findByEmail(input.email())).thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class, () -> service.execute(input));
    }

    @Test
    @DisplayName("Should login successfully")
    void loginSuccessfully() {
        LoginInput input = new LoginInput("test@gmail.com", "password");
        User user = new User("Test","test@gmail.com", "password");
        String expectedToken = UUID.randomUUID().toString();

        when(userRepository.findByEmail(input.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(input.password(), user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn(expectedToken);
        when(jwtProperties.accessTokenTtl()).thenReturn(Duration.ofSeconds(900));


        LoginOutput output = service.execute(input);

        assertNotNull(output);
        assertEquals(expectedToken, output.accessToken());
        assertEquals("Bearer", output.tokenType());
        assertEquals(900L, output.expiresIn());
    }

}