package com.movieweb.backend.repository;

import com.movieweb.backend.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RecommendationRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByTitleIgnoreCase(String title);

    List<Movie> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Movie> searchByKeyword(String keyword);

    List<Movie> findTop20ByOrderByRatingDesc();
    Optional<Movie> findByTmdbId(Long tmdbId);
}