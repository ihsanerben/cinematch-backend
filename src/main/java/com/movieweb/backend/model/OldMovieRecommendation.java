package com.movieweb.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "old_movie_recommendations")
public class OldMovieRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String basedOnTitle; // Favori film adı

    @ManyToMany
    @JoinTable(
            name = "old_movie_recommendation_movies",
            joinColumns = @JoinColumn(name = "recommendation_id"),
            inverseJoinColumns = @JoinColumn(name = "movie_id")
    )
    private List<Movie> recommendedMovies;

    private LocalDateTime createdAt;
}
