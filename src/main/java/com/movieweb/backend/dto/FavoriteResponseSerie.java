package com.movieweb.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponseSerie {
    private Long id;
    private Long contentId;
    private String type; // "SERIE"
    private String name;
    private String overview;
    private String posterUrl;
    private Double rating;
    private String firstAirDate;
    private String category;
    private String originCountry;
}