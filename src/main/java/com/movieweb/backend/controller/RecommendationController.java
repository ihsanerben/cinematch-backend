package com.movieweb.backend.controller;

import com.movieweb.backend.model.*;
import com.movieweb.backend.repository.*;
import com.movieweb.backend.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final OldMovieRecommendationRepository oldMovieRecommendationRepository;
    private final OldSerieRecommendationRepository oldSerieRecommendationRepository;

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

            // 1️⃣ Gemini'den öneri başlıkları
            List<String> suggestedTitles =
                    geminiService.getRecommendationsFromTitles(List.of(originalTitle));

            // ======================
            // 🎬 MOVIE RECOMMENDATION
            // ======================
            if (fav.getType().equalsIgnoreCase("MOVIE")) {

                List<Movie> matchedMovies = geminiService.smartMatch(suggestedTitles);
                if (matchedMovies.isEmpty()) continue;

                // 🔹 SAVE (Movie)
                OldMovieRecommendation oldRec = new OldMovieRecommendation();
                oldRec.setUser(user);
                oldRec.setBasedOnTitle(originalTitle);
                oldRec.setRecommendedMovies(matchedMovies);
                oldRec.setCreatedAt(LocalDateTime.now());

                oldMovieRecommendationRepository.save(oldRec);

                // 🔹 FRONTEND RESPONSE
                Map<String, Object> block = new HashMap<>();
                block.put("type", "MOVIE");
                block.put("basedOn", originalTitle);
                block.put("recommendations", matchedMovies);

                responseBlocks.add(block);
            }

            // ======================
            // 📺 SERIE RECOMMENDATION
            // ======================
            else {

                List<Serie> matchedSeries =
                        geminiService.smartMatchSeries(suggestedTitles);

                if (matchedSeries.isEmpty()) continue;

                // 🔹 SAVE (Serie)
                OldSerieRecommendation oldRec = new OldSerieRecommendation();
                oldRec.setUser(user);
                oldRec.setBasedOnTitle(originalTitle);
                oldRec.setRecommendedSeries(matchedSeries);
                oldRec.setCreatedAt(LocalDateTime.now());

                oldSerieRecommendationRepository.save(oldRec);

                // 🔹 FRONTEND RESPONSE
                Map<String, Object> block = new HashMap<>();
                block.put("type", "SERIE");
                block.put("basedOn", originalTitle);
                block.put("recommendations", matchedSeries);

                responseBlocks.add(block);
            }
        }

        return ResponseEntity.ok(responseBlocks);
    }
}
