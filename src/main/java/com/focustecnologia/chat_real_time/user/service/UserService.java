package com.focustecnologia.chat_real_time.user.service;

import com.focustecnologia.chat_real_time.auth.controller.response.UserResponse;
import com.focustecnologia.chat_real_time.auth.service.output.UserOutput;
import com.focustecnologia.chat_real_time.user.entity.User;
import com.focustecnologia.chat_real_time.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;

    public UserResponse getById(UUID id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found."));

        return UserResponse.from(UserOutput.fromUser(user));
    }

    public Page<UserResponse> getAll(int page, int size) {
        page = Math.max(0, page - 1);
        size = Math.min(Math.max(1, size), 25);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").descending());

        return repository.findAll(pageable)
                .map(user -> UserResponse.from(UserOutput.fromUser(user)));
    }
}
