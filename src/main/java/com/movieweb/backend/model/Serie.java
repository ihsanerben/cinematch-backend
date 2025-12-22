package com.movieweb.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "series")
@Getter
@Setter
public class Serie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long tmdbId;

    private String name;

    @Column(length = 2000)
    private String overview;

    private String posterUrl;
    private String backdropUrl;

    private Double rating;
    private String firstAirDate;
    private String category;
    private String originCountry;
}