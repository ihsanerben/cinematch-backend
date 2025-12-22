package com.movieweb.backend.controller;

import com.movieweb.backend.model.User;
import com.movieweb.backend.service.AuthService;
import com.movieweb.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider; // ✅ EKLENDİ

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        Map<String, Object> result = authService.register(user);
        user.setCreatedAt(LocalDate.now());
        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        Map<String, Object> result = authService.login(email, password);
        if (result.containsKey("error")) {
            return ResponseEntity.status(401).body(result);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(HttpServletRequest request) {


        String auth = request.getHeader("Authorization");

        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Token missing");
        }

        String token = auth.substring(7);
        String email = jwtTokenProvider.getEmailFromToken(token);

        return ResponseEntity.ok(authService.getUserByEmail(email));
    }
}