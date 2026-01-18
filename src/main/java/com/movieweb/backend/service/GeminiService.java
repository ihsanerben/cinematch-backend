package com.movieweb.backend.service;

import com.movieweb.backend.model.Movie;
import com.movieweb.backend.model.Serie;
import com.movieweb.backend.repository.MovieRepository;
import com.movieweb.backend.repository.SerieRepository;
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
    private final MovieRepository movieRepository;
    private final SerieRepository serieRepository;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=";

    // =====================================================
    // 1️⃣ Gemini’ye başlık gönder → sadece STRING döner
    // =====================================================
    public List<String> getRecommendationsFromTitles(List<String> titles) {

        String prompt =
                "The user likes the following movies or TV series:\n" +
                        String.join(", ", titles) +
                        "\n" +
                        "Recommend exactly 2 similar movies or TV series.\n" +
                        "\n" +
                        "STRICT RULES:\n" +
                        "- Only recommend well-known and globally popular titles.\n" +
                        "- Titles must be in English.\n" +
                        "- IMDb rating should generally be 7.5 or higher.\n" +
                        "- Return ONLY the titles.\n";

        return sendPrompt(prompt);
    }

    // =====================================================
    // 2️⃣ MOVIE MATCH
    // =====================================================
    public List<Movie> smartMatch(List<String> geminiTitles) {

        List<Movie> results = new ArrayList<>();

        for (String title : geminiTitles) {

            Map<String, Object> tmdbData = tmdbSearchService.searchOne(title);
            if (tmdbData == null) continue;

            // ❗ Eğer TMDB sonucu dizi ise atla
            if ("tv".equals(tmdbData.get("media_type"))) continue;

            Long tmdbId = Long.valueOf(tmdbData.get("id").toString());

            Optional<Object> existing = movieRepository.findByTmdbId(tmdbId);
            if (existing.isPresent()) {
                results.add((Movie) existing.get());
                continue;
            }

            Movie movie = tmdbSearchService.convertToMovie(tmdbData);
            movieRepository.save(movie);
            results.add(movie);
        }

        return results;
    }

    // =====================================================
    // 3️⃣ SERIE MATCH ✅ (SORUNUN ÇÖZÜLDÜĞÜ YER)
    // =====================================================
    public List<Serie> smartMatchSeries(List<String> geminiTitles) {

        List<Serie> results = new ArrayList<>();

        for (String title : geminiTitles) {

            Map<String, Object> tmdbData = tmdbSearchService.searchOne(title);
            if (tmdbData == null) continue;

            // ❗ Eğer TMDB sonucu film ise atla
            if ("movie".equals(tmdbData.get("media_type"))) continue;

            Long tmdbId = Long.valueOf(tmdbData.get("id").toString());

            Optional<Serie> existing = serieRepository.findByTmdbId(tmdbId);
            if (existing.isPresent()) {
                results.add(existing.get());
                continue;
            }

            Serie serie = tmdbSearchService.convertToSerie(tmdbData);
            serieRepository.save(serie);
            results.add(serie);
        }

        return results;
    }

    // =====================================================
    // 4️⃣ Gemini API
    // =====================================================
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
