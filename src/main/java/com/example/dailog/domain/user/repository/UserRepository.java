package com.example.dailog.domain.user.repository;

import com.example.dailog.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByName(String username);

    Optional<User> findUserByEmail(String email);
}
