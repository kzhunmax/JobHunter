package com.github.kzhunmax.jobsearch.user.repository;

import com.github.kzhunmax.jobsearch.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByResetPasswordToken(String token);
    Optional<User> findByEmailVerifyToken(String token);
}
