package com.movieweb.backend.repository;

import com.movieweb.backend.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {
    Optional<Serie> findByTmdbId(Long tmdbId);
}