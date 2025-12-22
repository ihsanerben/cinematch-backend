package com.movieweb.backend.service;

import com.movieweb.backend.model.Serie;
import com.movieweb.backend.repository.SerieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmdbShowService {

    private final SerieRepository serieRepository;

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.themoviedb.org/3")
            .build();

    public int syncAllSeries() {
        int totalSaved = 0;
        int totalPages = 100;

        List<String> endpoints = List.of(
                "/tv/popular",
                "/tv/top_rated",
                "/tv/on_the_air",
                "/tv/airing_today",
                "/trending/tv/week"
        );

        Set<Long> existingIds = serieRepository.findAll().stream()
                .map(Serie::getTmdbId)
                .collect(Collectors.toSet());

        for (String endpoint : endpoints) {
            String category = detectCategory(endpoint);
            log.info("🚀 Başlatılıyor: {} kategorisi", category);

            for (int page = 1; page <= totalPages; page++) {
                final int currentPage = page;

                try {
                    Map<String, Object> response = webClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path(endpoint)
                                    .queryParam("api_key", apiKey)
                                    .queryParam("language", "en-US")
                                    .queryParam("page", String.valueOf(currentPage))
                                    .build())
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                            .block();

                    if (response == null || !response.containsKey("results"))
                        break;

                    List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                    if (results == null || results.isEmpty()) break;

                    List<Serie> newSeries = new ArrayList<>();

                    for (Map<String, Object> showData : results) {
                        Long tmdbId = ((Number) showData.get("id")).longValue();
                        if (existingIds.contains(tmdbId)) continue;

                        Serie serie = new Serie();
                        serie.setTmdbId(tmdbId);
                        serie.setName((String) showData.getOrDefault("name", "Untitled"));
                        serie.setOverview((String) showData.getOrDefault("overview", "No description."));
                        serie.setPosterUrl(showData.get("poster_path") != null
                                ? "https://image.tmdb.org/t/p/w500" + showData.get("poster_path")
                                : null);
                        serie.setBackdropUrl(showData.get("backdrop_path") != null
                                ? "https://image.tmdb.org/t/p/w780" + showData.get("backdrop_path")
                                : null);
                        serie.setRating(showData.get("vote_average") != null
                                ? ((Number) showData.get("vote_average")).doubleValue()
                                : 0.0);
                        serie.setFirstAirDate((String) showData.get("first_air_date"));
                        serie.setCategory(category);

                        if (showData.get("origin_country") instanceof List<?> countries && !countries.isEmpty()) {
                            serie.setOriginCountry(countries.get(0).toString());
                        }

                        newSeries.add(serie);
                        existingIds.add(tmdbId);
                    }

                    if (!newSeries.isEmpty()) {
                        serieRepository.saveAll(newSeries);
                        totalSaved += newSeries.size();
                        log.info("✅ {}: {}. sayfa tamamlandı ({} yeni dizi eklendi, toplam: {})",
                                category, currentPage, newSeries.size(), totalSaved);
                    }

                    Thread.sleep(300);

                } catch (Exception e) {
                    log.error("❌ Hata ({} - page={}): {}", category, page, e.getMessage());
                    break;
                }
            }
        }

        log.info("🏁 Tüm diziler senkronize edildi. Toplam {} yeni dizi kaydedildi.", totalSaved);
        return totalSaved;
    }

    private String detectCategory(String url) {
        if (url.contains("popular")) return "popular";
        if (url.contains("top_rated")) return "top_rated";
        if (url.contains("on_the_air")) return "on_the_air";
        if (url.contains("airing_today")) return "airing_today";
        if (url.contains("trending")) return "trending";
        return "unknown";
    }

    public List<Serie> getAllSeries() {
        return serieRepository.findAll();
    }

    public void clearAllSeries() {
        serieRepository.deleteAll();
        log.info("🗑️ Tüm diziler silindi.");
    }
}