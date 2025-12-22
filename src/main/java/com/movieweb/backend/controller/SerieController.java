package com.movieweb.backend.controller;

import com.movieweb.backend.model.Serie;
import com.movieweb.backend.service.TmdbShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/series")
@RequiredArgsConstructor
public class SerieController {

    private final TmdbShowService tmdbShowService;

    // Tüm dizileri getir
    @GetMapping
    public ResponseEntity<List<Serie>> getAllSeries() {
        return ResponseEntity.ok(tmdbShowService.getAllSeries());
    }

    // TMDB'den dizileri senkronize et
    @PostMapping("/sync")
    public ResponseEntity<String> syncSeries() {
        int total = tmdbShowService.syncAllSeries();
        return ResponseEntity.ok("Synced series: " + total);
    }

    // Veritabanındaki tüm dizileri sil
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearAllSeries() {
        tmdbShowService.clearAllSeries();
        return ResponseEntity.ok("All series deleted successfully.");
    }
}