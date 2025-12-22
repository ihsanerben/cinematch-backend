package com.movieweb.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_token")
@Getter
@Setter
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ✅ Veritabanı kolon adı "expires_at"
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // ✅ Boş bırakmayacağız, gerçek set işlemi yapıyoruz
    public void setExpiryDate(LocalDateTime localDateTime) {
        this.expiresAt = localDateTime;
    }
}