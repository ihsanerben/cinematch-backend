package com.movieweb.backend.service;

import com.movieweb.backend.model.Movie;
import com.movieweb.backend.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final TmdbSearchService tmdbSearchService;
    private final RecommendationRepository recommendationRepository;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=";


    /** 1) Gemini’ye başlık gönder → sadece string öneriler döner */
    public List<String> getRecommendationsFromTitles(List<String> titles) {

        String prompt =
                "User likes these movies/series: " +
                        String.join(", ", titles) +
                        ". Recommend 5 similar movies or series. Return only titles.";

        return sendPrompt(prompt);
    }


    /** 2) Önerileri TMDB’de bul → DB’ye ekle → en doğru içerikleri döndür */
    public List<Movie> smartMatch(List<String> geminiTitles) {

        List<Movie> results = new ArrayList<>();

        for (String title : geminiTitles) {

            Map<String, Object> tmdbData = tmdbSearchService.searchOne(title);

            if (tmdbData == null) continue;

            Long tmdbId = Long.valueOf(tmdbData.get("id").toString());

            // DB’de varsa direkt kullan
            Optional<Movie> existing = recommendationRepository.findByTmdbId(tmdbId);
            if (existing.isPresent()) {
                results.add(existing.get());
                continue;
            }

            // Yoksa TMDB’den yeni movie oluştur
            Movie movie = tmdbSearchService.convertToMovie(tmdbData);
            recommendationRepository.save(movie);

            results.add(movie);
        }

        return results;
    }


    /** 3) Gemini API */
    public List<String> sendPrompt(String prompt) {

        try {
            RestTemplate rest = new RestTemplate();

            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<Map> response = rest.exchange(
                    GEMINI_URL + apiKey,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            Map candidate = (Map) ((List<?>) response.getBody().get("candidates")).get(0);
            Map content = (Map) candidate.get("content");
            Map part = (Map) ((List<?>) content.get("parts")).get(0);

            String text = (String) part.get("text");

            return Arrays.stream(text.split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();

        } catch (Exception e) {
            log.error("Gemini ERROR:", e);
            return List.of();
        }
    }
}