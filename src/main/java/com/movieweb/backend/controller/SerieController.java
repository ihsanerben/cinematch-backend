package com.movieweb.backend.controller;

import com.movieweb.backend.model.Serie;
import com.movieweb.backend.repository.SerieRepository;
import com.movieweb.backend.security.JwtTokenProvider;
import com.movieweb.backend.service.TmdbShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import com.movieweb.backend.model.OldSerieRecommendation;
import com.movieweb.backend.model.User;
import com.movieweb.backend.repository.OldSerieRecommendationRepository;
import com.movieweb.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.List;

@RestController
@RequestMapping("/api/series")
@RequiredArgsConstructor
public class SerieController {

    private final TmdbShowService tmdbShowService;
    private final OldSerieRecommendationRepository oldSerieRecommendationRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;


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

    @GetMapping("/recommendations/history/serie")
    public ResponseEntity<List<OldSerieRecommendation>> getOldSerieRecommendations(
            @RequestHeader("Authorization") String authHeader
    ) {

        String token = authHeader.substring(7);
        String email = jwtTokenProvider.getEmailFromToken(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                oldSerieRecommendationRepository
                        .findByUserOrderByCreatedAtDesc(user)
        );
    }


}