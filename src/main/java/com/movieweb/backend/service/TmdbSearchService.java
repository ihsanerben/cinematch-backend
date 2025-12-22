package com.movieweb.backend.service;

import com.movieweb.backend.model.Movie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TmdbSearchService {

    @Value("${tmdb.api.key}")
    private String tmdbKey;

    public Map<String, Object> searchOne(String title) {

        try {
            RestTemplate rt = new RestTemplate();

            String url =
                    "https://api.themoviedb.org/3/search/multi?api_key=" + tmdbKey +
                            "&query=" + title.replace(" ", "%20");

            Map response = rt.getForObject(url, Map.class);
            List<Map<String, Object>> results =
                    (List<Map<String, Object>>) response.get("results");

            if (results == null || results.isEmpty()) return null;

            return results.get(0);

        } catch (Exception e) {
            return null;
        }
    }

    public Movie convertToMovie(Map<String, Object> data) {
        Movie m = new Movie();
        m.setTmdbId(Long.valueOf((Integer) data.get("id")));
        m.setTitle((String) data.get("title"));
        m.setOverview((String) data.get("overview"));
        m.setPosterUrl("https://image.tmdb.org/t/p/w500" + data.get("poster_path"));
        m.setRating(Double.valueOf(data.get("vote_average").toString()));
        return m;
    }
}