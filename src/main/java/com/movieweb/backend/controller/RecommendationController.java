package com.movieweb.backend.controller;

import com.movieweb.backend.model.Favorite;
import com.movieweb.backend.model.Movie;
import com.movieweb.backend.model.User;
import com.movieweb.backend.repository.*;
import com.movieweb.backend.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final SerieRepository serieRepository;
    private final GeminiService geminiService;

    // ⭐ FRONTEND burayı çağırıyor → /personal
    @GetMapping("/personal")
    public ResponseEntity<?> getPersonalized(Authentication auth) {

        UserDetails userDetails = (UserDetails) auth.getPrincipal();


        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Favorite> favorites = favoriteRepository.findByUser(user);
        if (favorites.isEmpty())
            return ResponseEntity.ok(List.of());

        List<Map<String, Object>> responseBlocks = new ArrayList<>();

        // --- Her favoriden öneri üret ---
        for (Favorite fav : favorites) {

            String originalTitle = null;

            if (fav.getType().equalsIgnoreCase("MOVIE")) {
                originalTitle = movieRepository.findById(fav.getContentId())
                        .map(Movie::getTitle)
                        .orElse(null);

            } else {
                originalTitle = serieRepository.findById(fav.getContentId())
                        .map(s -> s.getName())
                        .orElse(null);
            }

            if (originalTitle == null) continue;

            // 1) Gemini'den öneri başlıkları
            List<String> suggestedTitles =
                    geminiService.getRecommendationsFromTitles(List.of(originalTitle));

            // 2) DB’de akıllı eşleşen öneriler
            List<Movie> matched = geminiService.smartMatch(suggestedTitles);

            if (matched.isEmpty()) continue;

            // 3) frontend için block formatı
            Map<String, Object> block = new HashMap<>();
            block.put("basedOn", originalTitle);
            block.put("recommendations", matched);

            responseBlocks.add(block);
        }

        return ResponseEntity.ok(responseBlocks);
    }
}