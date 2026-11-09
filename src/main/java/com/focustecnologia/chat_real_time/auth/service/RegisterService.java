package com.focustecnologia.chat_real_time.auth.service;

import com.focustecnologia.chat_real_time.auth.service.input.UserInput;
import com.focustecnologia.chat_real_time.auth.service.output.UserOutput;
import com.focustecnologia.chat_real_time.exception.EmailAlreadyInUseException;
import com.focustecnologia.chat_real_time.user.entity.User;
import com.focustecnologia.chat_real_time.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegisterService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserOutput execute(UserInput input) {
        boolean emailExists = userRepository.findByEmail(input.email()).isPresent();

        if(emailExists){
            throw new EmailAlreadyInUseException();
        }

        String passwordHash = passwordEncoder.encode(input.password());
        User user = userRepository.save(new User(input.name(),input.email(), passwordHash));

        return UserOutput.fromUser(user);

    }

}
