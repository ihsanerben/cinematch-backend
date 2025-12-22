package com.movieweb.backend.repository;

import com.movieweb.backend.model.OldMovieRecommendation;
import com.movieweb.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OldMovieRecommendationRepository
        extends JpaRepository<OldMovieRecommendation, Long> {

    List<OldMovieRecommendation>
    findByUserOrderByCreatedAtDesc(User user);
}
