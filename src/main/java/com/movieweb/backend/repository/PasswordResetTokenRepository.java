package com.movieweb.backend.repository;

import com.movieweb.backend.model.PasswordResetToken;
import com.movieweb.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUser(User user); // ✅ Bunu ekle
}