package com.movieweb.backend.controller;

import ch.qos.logback.classic.Logger;
import com.movieweb.backend.model.Movie;
import com.movieweb.backend.repository.MovieRepository;
import com.movieweb.backend.service.TmdbService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final TmdbService tmdbService;
    private final MovieRepository movieRepository;

    @PostMapping("/sync-all")
    public ResponseEntity<String> syncAllMovies() {
        int count = tmdbService.syncAllMovies();
        return ResponseEntity.ok("Synced movies: " + count);
    }

    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies() {
        return ResponseEntity.ok(tmdbService.getAllMovies());
    }
    @DeleteMapping("/erotic")
    public ResponseEntity<?> deleteEroticMovies() {

        List<Movie> all = movieRepository.findAll();
        int deletedCount = 0;

        for (Movie m : all) {

            boolean isErotic = tmdbService.isEroticForCleaning(m.getTmdbId());

            if (isErotic) {
                movieRepository.delete(m);
                deletedCount++;
                log.warn("⛔ Erotik film silindi → {}", m.getTitle());
            }
        }

        return ResponseEntity.ok("Silinen erotik film sayısı: " + deletedCount);
    }
}