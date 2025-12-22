package com.movieweb.backend.repository;

import com.movieweb.backend.model.Favorite;
import com.movieweb.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // Belirli kullanıcı ve içerik için favori var mı kontrolü
    Optional<Favorite> findByUserAndContentIdAndType(User user, Long contentId, String type);

    // Kullanıcının tüm favorileri (film veya dizi türüne göre)
    List<Favorite> findByUserAndType(User user, String type);

    // 🔥 OpenAI Recommendation için EKLENDİ (filmler + diziler birlikte lazım)
    List<Favorite> findByUser(User user);
}