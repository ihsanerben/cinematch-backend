package com.movieweb.backend.controller;

import com.movieweb.backend.model.Movie;
import com.movieweb.backend.service.TmdbService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


    @RestController
    @RequestMapping("/api/tmdb")
    @RequiredArgsConstructor
    public class TmdbController {
        private final TmdbService tmdbService;

        @PostMapping("/sync-all")
        public ResponseEntity<String> syncAll() {
            int total = tmdbService.syncAllMovies();
            return ResponseEntity.ok("Synced movies: " + total);
        }

        @GetMapping("/all")
        public ResponseEntity<List<Movie>> getAll() {
            return ResponseEntity.ok(tmdbService.getAllMovies());
        }

        @PostMapping("/scan")
        public ResponseEntity<?> scanMovies() {
            int count = tmdbService.scanById();
            return ResponseEntity.ok("Yeni eklenen film sayısı: " + count);
        }
    }