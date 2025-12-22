package com.movieweb.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieweb.backend.model.Movie;
import com.movieweb.backend.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmdbService {

    private final MovieRepository movieRepository;

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.themoviedb.org/3")
            .build();

    private final File progressFile = new File("tmdb_progress.json");
    private final ObjectMapper mapper = new ObjectMapper();


    /*───────────────────────────────*
     *         EROTİK FİLTRE
     *───────────────────────────────*/
    private final List<Integer> eroticGenreIds = List.of(
            17018, 9840, 13027, 14635
    );

    private final List<String> bannedWords = List.of(
            "sex", "sexual", "erotic", "porn", "xxx", "hardcore", "softcore",
            "fetish", "nudity", "nude", "explicit", "adult"
    );

    private boolean isErotic(Map<String, Object> movieData) {

        // 1) Adult flag
        if (Boolean.TRUE.equals(movieData.get("adult"))) return true;

        // 2) Genre ID filtering
        List<Integer> genres = (List<Integer>) movieData.getOrDefault("genre_ids", List.of());
        for (Integer id : genres) {
            if (eroticGenreIds.contains(id)) return true;
        }

        // 3) Keyword scanning
        String title = Optional.ofNullable((String) movieData.get("title")).orElse("").toLowerCase();
        String overview = Optional.ofNullable((String) movieData.get("overview")).orElse("").toLowerCase();

        return bannedWords.stream().anyMatch(
                w -> title.contains(w) || overview.contains(w)
        );
    }



    /*───────────────────────────────*
     *     POPULAR / TOP-RATED SYNC
     *───────────────────────────────*/
    public int syncAllMovies() {

        int totalSaved = 0;
        int totalPages = 1000;

        List<String> endpoints = List.of(
                "/movie/popular",
                "/movie/top_rated",
                "/movie/upcoming",
                "/trending/movie/week"
        );

        // DB’de olanlar
        Set<Long> existingIds = movieRepository.findAll().stream()
                .map(Movie::getTmdbId)
                .collect(Collectors.toSet());

        // ilerleme dosyası
        Map<String, Integer> progress = loadProgress();


        for (String endpoint : endpoints) {
            String category = detectCategory(endpoint);
            int startPage = progress.getOrDefault(category, 1);

            log.info("🚀 {} kategorisi {}. sayfadan başlıyor.", category, startPage);

            for (int page = startPage; page <= totalPages; page++) {

                final int pageFinal = page;

                try {
                    Map<String, Object> response = webClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path(endpoint)
                                    .queryParam("api_key", apiKey)
                                    .queryParam("language", "en-US")
                                    .queryParam("include_adult", "false")
                                    .queryParam("page", pageFinal)
                                    .build())
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                            .block();

                    if (response == null || !response.containsKey("results")) break;

                    List<Map<String, Object>> results =
                            (List<Map<String, Object>>) response.get("results");

                    if (results == null || results.isEmpty()) break;

                    List<Movie> newMovies = new ArrayList<>();

                    for (Map<String, Object> movieData : results) {

                        // 🔥 Erotik içerik → atla
                        if (isErotic(movieData)) {
                            log.warn("⛔ Erotik film atlandı: {}", movieData.get("title"));
                            continue;
                        }

                        Long tmdbId = ((Number) movieData.get("id")).longValue();

                        if (existingIds.contains(tmdbId)) continue;

                        Movie movie = new Movie();
                        movie.setTmdbId(tmdbId);
                        movie.setTitle((String) movieData.getOrDefault("title", "Untitled"));
                        movie.setOverview((String) movieData.getOrDefault("overview", "No description."));
                        movie.setPosterUrl(movieData.get("poster_path") != null
                                ? "https://image.tmdb.org/t/p/w500" + movieData.get("poster_path")
                                : null);
                        movie.setRating(((Number) movieData.getOrDefault("vote_average", 0)).doubleValue());
                        movie.setReleaseDate((String) movieData.get("release_date"));
                        movie.setCategory(category);

                        newMovies.add(movie);
                        existingIds.add(tmdbId);
                    }

                    if (!newMovies.isEmpty()) {
                        movieRepository.saveAll(newMovies);
                        totalSaved += newMovies.size();
                        log.info("✅ {} → Sayfa {}: {} film eklendi", category, page, newMovies.size());
                    }

                    progress.put(category, page);
                    saveProgress(progress);

                    Thread.sleep(300);

                } catch (Exception e) {
                    log.error("❌ Hata ({} page {}): {}", category, page, e.getMessage());
                    break;
                }
            }
        }

        log.info("🏁 Film senkron tamamlandı → Toplam eklenen: {}", totalSaved);
        return totalSaved;
    }



    /*───────────────────────────────*
     *        PROGRESS SAVE/LOAD
     *───────────────────────────────*/
    private void saveProgress(Map<String, Integer> progress) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(progressFile, progress);
        } catch (IOException ignored) {}
    }

    private Map<String, Integer> loadProgress() {
        if (!progressFile.exists()) return new HashMap<>();
        try {
            return mapper.readValue(progressFile, new TypeReference<>() {});
        } catch (IOException ignored) {
            return new HashMap<>();
        }
    }



    /*───────────────────────────────*
     *        YARDIMCI METODLAR
     *───────────────────────────────*/
    private String detectCategory(String url) {
        if (url.contains("popular")) return "popular";
        if (url.contains("top_rated")) return "top_rated";
        if (url.contains("upcoming")) return "upcoming";
        if (url.contains("trending")) return "trending";
        return "unknown";
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public void clearAllMovies() {
        movieRepository.deleteAll();
        if (progressFile.exists()) progressFile.delete();
    }



    /*───────────────────────────────*
     *      ID SCAN (135M Film tarama)
     *───────────────────────────────*/
    public int scanById() {

        int saved = 0;

        File scanFile = new File("tmdb_id_scan.json");
        long startId = 1;

        if (scanFile.exists()) {
            try {
                Map<String, Long> data = mapper.readValue(scanFile, new TypeReference<>() {});
                startId = data.getOrDefault("last_scanned_id", 1L);
            } catch (Exception ignored) {}
        }

        long currentId = startId;
        long limit = startId + 5000;

        while (currentId <= limit) {

            long id = currentId++;

            try {
                Map<String, Object> movieData = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/movie/{id}")
                                .queryParam("api_key", apiKey)
                                .queryParam("language", "en-US")
                                .queryParam("include_adult", "false")
                                .build(id))
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .onErrorResume(e -> null)
                        .block();

                if (movieData == null || movieData.isEmpty()) continue;

                if (isErotic(movieData)) {
                    log.warn("⛔ Erotik ID-Scan atlandı {}", movieData.get("title"));
                    continue;
                }

                Long tmdbId = id;

                if (movieRepository.findByTmdbId(tmdbId).isPresent()) continue;

                Movie movie = new Movie();
                movie.setTmdbId(tmdbId);
                movie.setTitle((String) movieData.get("title"));
                movie.setOverview((String) movieData.getOrDefault("overview", ""));
                movie.setPosterUrl(movieData.get("poster_path") != null
                        ? "https://image.tmdb.org/t/p/w500" + movieData.get("poster_path")
                        : null);
                movie.setRating(((Number) movieData.getOrDefault("vote_average", 0)).doubleValue());
                movie.setReleaseDate((String) movieData.get("release_date"));
                movie.setCategory("id-scan");

                movieRepository.save(movie);
                saved++;

                log.info("🌟 Yeni film bulundu (ID-Scan): {} - {}", tmdbId, movie.getTitle());

                Thread.sleep(150);

                mapper.writerWithDefaultPrettyPrinter()
                        .writeValue(scanFile, Map.of("last_scanned_id", currentId));

            } catch (Exception e) {
                log.error("❌ ID Scan hatası ({}): {}", id, e.getMessage());
            }
        }

        log.info("🏁 ID Scan tamamlandı → {} yeni film", saved);
        return saved;
    }

    public boolean isEroticForCleaning(Long tmdbId) {

        try {
            Map<String, Object> movieData = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/movie/{id}")
                            .queryParam("api_key", apiKey)
                            .queryParam("language", "en-US")
                            .queryParam("append_to_response", "keywords")
                            .build(tmdbId))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .onErrorResume(e -> null)
                    .block();

            if (movieData == null || movieData.isEmpty()) {
                return false;
            }

            return isErotic(movieData);  // 🔥 mevcut filtreyi kullanıyoruz

        } catch (Exception e) {
            log.error("Temizlik kontrol hatası: {}", e.getMessage());
            return false;
        }
    }
}