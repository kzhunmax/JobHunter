package com.github.kzhunmax.jobsearch.shared;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.frontend.reset-url}")
    private String resetUrl;

    @Value("${app.backend.url}")
    private String backendUrl;

    public void sendVerificationEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Verify Your Email");
        String verifyLink = backendUrl + "/api/auth/verify-email?token=" + token;
        message.setText("Click here to verify: " + verifyLink);
        mailSender.send(message);
        log.info("Verification email sent to {}", email);
    }

    public void sendPasswordResetEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Reset Your Password");

        String resetLink = resetUrl + "?token=" + token;
        message.setText("To reset your password, click the link below:\n" + resetLink);
        mailSender.send(message);
        log.info("Password reset email sent to {}", email);
    }
}
