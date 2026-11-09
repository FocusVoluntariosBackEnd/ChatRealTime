package com.focustecnologia.chat_real_time.user.service;

import com.focustecnologia.chat_real_time.auth.controller.response.UserResponse;
import com.focustecnologia.chat_real_time.exception.ResourceNotFoundException;
import com.focustecnologia.chat_real_time.user.entity.User;
import com.focustecnologia.chat_real_time.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository repository;
    @InjectMocks
    private UserService service;

    @Test
    @DisplayName("Should throw ResourceNotFound.class when user not found")
    void userNotFound() {
        UUID userId = UUID.randomUUID();

        when(repository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(userId));
    }

    @Test
    @DisplayName("Should get user by id successfully")
    void shouldGetUserByIdSuccessfully() {
        UUID userId = UUID.randomUUID();
        User user = new User("User", "user@gmail.com", "password");
        ReflectionTestUtils.setField(user, "id", userId);

        when(repository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse response = service.getById(userId);

        assertNotNull(response);
        assertEquals(userId.toString(), response.id());
        assertEquals("User", response.name());
        assertEquals("user@gmail.com", response.email());
    }

    @Test
    @DisplayName("Should get all users with pagination successfully")
    void shouldGetAllUsersSuccessfully() {
        UUID userId = UUID.randomUUID();
        User user = new User("User", "user@gmail.com", "password");
        ReflectionTestUtils.setField(user, "id", userId);

        Page<User> page = new PageImpl<>(List.of(user));

        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        Page<UserResponse> response = service.getAll(1, 10);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(userId.toString(), response.getContent().get(0).id());
        assertEquals("User", response.getContent().get(0).name());
    }
}
