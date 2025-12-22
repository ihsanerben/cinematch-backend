package com.movieweb.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponseMovie {
    private Long id;
    private Long contentId;
    private String type; // "MOVIE"
    private String title;
    private String overview;
    private String posterUrl;
    private Double rating;
    private String releaseDate;
    private String category;
}