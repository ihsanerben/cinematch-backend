package com.movieweb.backend.service;

import com.movieweb.backend.model.User;
import com.movieweb.backend.repository.UserRepository;
import com.movieweb.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // ✅ Register
    public Map<String, Object> register(User user) {

        user.setCreatedAt(LocalDate.now()); // 🟩 createdAt burada set edilmeli!

        // şifre hash:
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getEmail());

        return Map.of(
                "token", token,
                "user", user
        );
    }

    // ✅ Login
    public Map<String, Object> login(String email, String password) {
        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            response.put("error", "User not found!");
            return response;
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.put("error", "Invalid password!");
            return response;
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());
        response.put("token", token);
        response.put("userEmail", email);
        response.put("message", "Login successful!");
        return response;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}