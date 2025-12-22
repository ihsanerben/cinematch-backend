package com.movieweb.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "favorites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Kullanıcıyla ilişki (Many-to-One)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Film veya dizi ID’si
    @Column(nullable = false)
    private Long contentId;

    // Tür (MOVIE veya SERIE)
    @Column(nullable = false)
    private String type;
}