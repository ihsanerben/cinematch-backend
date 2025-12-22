package com.movieweb.backend.service;

import com.movieweb.backend.dto.FavoriteResponseMovie;
import com.movieweb.backend.dto.FavoriteResponseSerie;
import com.movieweb.backend.model.Favorite;
import com.movieweb.backend.model.Movie;
import com.movieweb.backend.model.Serie;
import com.movieweb.backend.model.User;
import com.movieweb.backend.repository.*;
import com.movieweb.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final SerieRepository serieRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private User getUserFromToken(String token) {
        String email = jwtTokenProvider.getEmailFromToken(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // 🎬 MOVIE FAVORITES
    public Map<String, Object> addMovieToFavorites(String token, Long movieId) {
        return addToFavorites(token, movieId, "MOVIE");
    }

    public Map<String, Object> removeMovieFromFavorites(String token, Long movieId) {
        return removeFromFavorites(token, movieId, "MOVIE");
    }

    public List<FavoriteResponseMovie> getFavoriteMovies(String token) {
        User user = getUserFromToken(token);
        List<Favorite> favorites = favoriteRepository.findByUserAndType(user, "MOVIE");
        List<FavoriteResponseMovie> result = new ArrayList<>();

        for (Favorite fav : favorites) {
            movieRepository.findById(fav.getContentId()).ifPresent(movie -> {
                result.add(FavoriteResponseMovie.builder()
                        .id(fav.getId())
                        .contentId(movie.getId())
                        .type("MOVIE")
                        .title(movie.getTitle())
                        .overview(movie.getOverview())
                        .posterUrl(movie.getPosterUrl())
                        .rating(movie.getRating())
                        .releaseDate(movie.getReleaseDate())
                        .category(movie.getCategory())
                        .build());
            });
        }

        return result;
    }

    // 📺 SERIE FAVORITES
    public Map<String, Object> addSerieToFavorites(String token, Long serieId) {
        return addToFavorites(token, serieId, "SERIE");
    }

    public Map<String, Object> removeSerieFromFavorites(String token, Long serieId) {
        return removeFromFavorites(token, serieId, "SERIE");
    }

    public List<FavoriteResponseSerie> getFavoriteSeries(String token) {
        User user = getUserFromToken(token);
        List<Favorite> favorites = favoriteRepository.findByUserAndType(user, "SERIE");
        List<FavoriteResponseSerie> result = new ArrayList<>();

        for (Favorite fav : favorites) {
            serieRepository.findById(fav.getContentId()).ifPresent(serie -> {
                result.add(FavoriteResponseSerie.builder()
                        .id(fav.getId())
                        .contentId(serie.getId())
                        .type("SERIE")
                        .name(serie.getName())
                        .overview(serie.getOverview())
                        .posterUrl(serie.getPosterUrl())
                        .rating(serie.getRating())
                        .firstAirDate(serie.getFirstAirDate())
                        .category(serie.getCategory())
                        .originCountry(serie.getOriginCountry())
                        .build());
            });
        }

        return result;
    }

    // Ortak ekleme/silme metotları
    private Map<String, Object> addToFavorites(String token, Long contentId, String type) {
        User user = getUserFromToken(token);
        Map<String, Object> response = new HashMap<>();

        boolean exists = favoriteRepository
                .findByUserAndContentIdAndType(user, contentId, type)
                .isPresent();

        if (exists) {
            response.put("message", type + " already in favorites");
            return response;
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .contentId(contentId)
                .type(type)
                .build();

        favoriteRepository.save(favorite);
        response.put("message", type + " added to favorites");
        return response;
    }

    private Map<String, Object> removeFromFavorites(String token, Long contentId, String type) {
        User user = getUserFromToken(token);
        Map<String, Object> response = new HashMap<>();

        Optional<Favorite> favOpt = favoriteRepository.findByUserAndContentIdAndType(user, contentId, type);
        if (favOpt.isEmpty()) {
            response.put("error", type + " not found in favorites");
            return response;
        }

        favoriteRepository.delete(favOpt.get());
        response.put("message", type + " removed from favorites");
        return response;
    }
}