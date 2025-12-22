package com.movieweb.backend.repository;

import com.movieweb.backend.model.OldSerieRecommendation;
import com.movieweb.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OldSerieRecommendationRepository
        extends JpaRepository<OldSerieRecommendation, Long> {

    List<OldSerieRecommendation>
    findByUserOrderByCreatedAtDesc(User user);
}
