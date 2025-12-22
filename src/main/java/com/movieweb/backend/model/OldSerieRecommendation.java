package com.movieweb.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "old_serie_recommendations")
public class OldSerieRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String basedOnTitle; // Favori dizi adı

    @ManyToMany
    @JoinTable(
            name = "old_serie_recommendation_series",
            joinColumns = @JoinColumn(name = "recommendation_id"),
            inverseJoinColumns = @JoinColumn(name = "serie_id")
    )
    private List<Serie> recommendedSeries;

    private LocalDateTime createdAt;
}
