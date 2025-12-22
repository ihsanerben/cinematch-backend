package com.movieweb.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(length = 20)
    private String phoneNumber;
    // 🚻 Gender (String ya da Enum olarak saklanabilir — şimdilik basit tutuyoruz)
    @Column(length = 10)
    private String gender;
    private LocalDate birthDate;

    @Column(updatable = false)
    private LocalDate createdAt;
}