package com.movieweb.backend.controller;

import com.movieweb.backend.dto.FavoriteResponseMovie;
import com.movieweb.backend.dto.FavoriteResponseSerie;
import com.movieweb.backend.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    // 🎬 Movies
    @PostMapping("/movie/{movieId}")
    public ResponseEntity<?> addMovie(@RequestHeader("Authorization") String authHeader, @PathVariable Long movieId) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(favoriteService.addMovieToFavorites(token, movieId));
    }

    @DeleteMapping("/movie/{movieId}")
    public ResponseEntity<?> removeMovie(@RequestHeader("Authorization") String authHeader, @PathVariable Long movieId) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(favoriteService.removeMovieFromFavorites(token, movieId));
    }

    @GetMapping("/movie")
    public ResponseEntity<List<FavoriteResponseMovie>> getMovies(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(favoriteService.getFavoriteMovies(token));
    }

    // 📺 Series
    @PostMapping("/serie/{serieId}")
    public ResponseEntity<?> addSerie(@RequestHeader("Authorization") String authHeader, @PathVariable Long serieId) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(favoriteService.addSerieToFavorites(token, serieId));
    }

    @DeleteMapping("/serie/{serieId}")
    public ResponseEntity<?> removeSerie(@RequestHeader("Authorization") String authHeader, @PathVariable Long serieId) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(favoriteService.removeSerieFromFavorites(token, serieId));
    }

    @GetMapping("/serie")
    public ResponseEntity<List<FavoriteResponseSerie>> getSeries(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(favoriteService.getFavoriteSeries(token));
    }
}