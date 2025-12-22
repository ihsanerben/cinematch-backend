package com.movieweb.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.movieweb.backend.service.CustomUserDetailsService;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtFilter(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService customUserDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        System.out.println("---- 🔍 JWT FILTER ÇALIŞTI ----");
        System.out.println("➡ Request: " + request.getMethod() + " " + request.getRequestURI());

        String header = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (header != null) {
            System.out.println("📌 Authorization Header: " + header);
        } else {
            System.out.println("⚠ Authorization header YOK");
        }

        // Header "Bearer ..." şeklindeyse tokenı al
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
            System.out.println("🔑 Token alındı: " + token);

            try {
                username = jwtTokenProvider.getEmailFromToken(token);
                System.out.println("📩 Token'dan çıkarılan email: " + username);
            } catch (Exception e) {
                System.out.println("❌ JWT parse error: " + e.getMessage());
            }
        }

        System.out.println("🔐 SecurityContext mevcut auth: "
                + SecurityContextHolder.getContext().getAuthentication());

        // Kullanıcı adı var ve SecurityContext boşsa kimliği doğrula
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            System.out.println("➡ Kullanıcı detayları yükleniyor: " + username);

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            if (jwtTokenProvider.validateToken(token)) {
                System.out.println("✅ Token GEÇERLİ");

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);

                System.out.println("🎉 Kullanıcı authenticate edildi -> "
                        + userDetails.getUsername());
            } else {
                System.out.println("❌ Token GEÇERSİZ!");
            }
        }

        System.out.println("🔐 SecurityContext SON auth: "
                + SecurityContextHolder.getContext().getAuthentication());

        filterChain.doFilter(request, response);
    }
}