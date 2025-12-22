package com.movieweb.backend.service;

import com.movieweb.backend.model.User;
import com.movieweb.backend.repository.UserRepository;
import com.movieweb.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register: returns JWT token on success.
     * Throws IllegalArgumentException / IllegalStateException with clear messages on validation errors.
     */
    public String register(User user) {
        // basic null checks (koruma)
        if (user == null) throw new IllegalArgumentException("User payload is missing.");

        validateEmail(user.getEmail());
        validatePassword(user.getPassword());

        if (userRepository.existsByEmail(user.getEmail())) {
            // eskisi gibi açık bir uyarı
            throw new IllegalStateException("Email already in use.");
        }

        // optional fields cleanup
        if (user.getPhoneNumber() != null && user.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(null);
        }
        if (user.getGender() != null && user.getGender().isBlank()) {
            user.setGender(null);
        }

        // hash the password before save
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        // Return token (frontend önceden bekliyorsa burada tokenu alır)
        return jwtTokenProvider.generateToken(user.getEmail());
    }

    /**
     * Login: receives email and password, returns JWT token.
     * Throws IllegalArgumentException on missing/invalid credentials.
     */
    public String login(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        // find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No user found with this email."));

        // compare hashed password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect password.");
        }

        // success -> return token
        return jwtTokenProvider.generateToken(email);
    }

    /* ---------- Validations (exact messages preserved / clear) ---------- */

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        if (email.contains(" ")) {
            throw new IllegalArgumentException("Email cannot contain spaces.");
        }
        // simple regex — aynen eskisi gibi
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        if (password.contains(" ")) {
            throw new IllegalArgumentException("Password cannot contain spaces.");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter.");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one number.");
        }
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character.");
        }
    }
}