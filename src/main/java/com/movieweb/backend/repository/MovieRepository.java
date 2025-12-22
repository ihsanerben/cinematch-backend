package com.movieweb.backend.repository;

import com.movieweb.backend.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // 1) EXACT MATCH (tam title)
    List<Movie> findByTitleIgnoreCase(String title);

    // 2) CONTAINS MATCH (title içinde geçen)
    List<Movie> findByTitleContainingIgnoreCase(String title);

    // 3) Keyword arama – overview + title içinde (opsiyonel)
    List<Movie> findByOverviewContainingIgnoreCase(String keyword);

    // 4) Category üzerinden öneri — kategori içeren ilk 10 film
    List<Movie> findTop10ByCategoryContainingIgnoreCase(String category);

    // 5) Fallback — en yüksek puanlı filmler
    List<Movie> findTop20ByOrderByRatingDesc();

    Optional<Object> findByTmdbId(Long tmdbId);
}