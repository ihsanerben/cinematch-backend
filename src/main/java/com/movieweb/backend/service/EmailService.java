package com.movieweb.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetMail(String toEmail, String token) {
        String resetLink = "http://localhost:3000/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("🔐 Password Reset Request");
        message.setText(
                "Hello,\n\n" +
                        "We received a request to reset your password.\n" +
                        "Click the link below to reset it:\n\n" + resetLink + "\n\n" +
                        "⚠ This link will expire in 15 minutes.\n\n" +
                        "If you did not request this, just ignore this email.\n\n" +
                        "Best regards,\nCineMatch Team");

        mailSender.send(message);
    }
}