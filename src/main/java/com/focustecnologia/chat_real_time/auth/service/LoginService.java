package com.focustecnologia.chat_real_time.auth.service;

import com.focustecnologia.chat_real_time.auth.service.input.LoginInput;
import com.focustecnologia.chat_real_time.auth.service.output.LoginOutput;
import com.focustecnologia.chat_real_time.exception.InvalidCredentialsException;
import com.focustecnologia.chat_real_time.security.JwtProperties;
import com.focustecnologia.chat_real_time.security.JwtService;
import com.focustecnologia.chat_real_time.user.entity.User;
import com.focustecnologia.chat_real_time.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public LoginService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    public LoginOutput execute(LoginInput loginInput) {
        User user = userRepository.findByEmail(loginInput.email())
                .orElseThrow(InvalidCredentialsException::new);

        boolean validPassword = passwordEncoder.matches(
                loginInput.password(),
                user.getPasswordHash()
        );

        if(!validPassword) {
            throw new InvalidCredentialsException();
        }

        return new LoginOutput(
                jwtService.generateToken(user),
                "Bearer",
                jwtProperties.accessTokenTtl().toSeconds()
        );

    }

}
