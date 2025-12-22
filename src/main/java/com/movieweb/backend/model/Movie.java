package com.movieweb.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "movies")
@Getter
@Setter
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tmdb_id", unique = true)
    private Long tmdbId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String overview;

    private String posterUrl;

    private Double rating;

    private String releaseDate;

    private String category;
}