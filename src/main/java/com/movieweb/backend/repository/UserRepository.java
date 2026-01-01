package com.movieweb.backend.repository;

import com.movieweb.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);  // ✅ Login için gerekli

    boolean existsByEmail(String email);       // ✅ Register için gerekli

    boolean existsByPhoneNumber(String phoneNumber);
}
