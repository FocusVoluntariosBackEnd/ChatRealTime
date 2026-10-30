package com.focustecnologia.chat_real_time.user.repository;

import com.focustecnologia.chat_real_time.user.entity.User;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    @NonNull Page<User> findAll(@NonNull Pageable pageable);
}
