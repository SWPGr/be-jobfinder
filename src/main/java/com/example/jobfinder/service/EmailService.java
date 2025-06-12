package com.example.jobfinder.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("verify your email");
        String verificationLink = "http://localhost:3030/api/auth/verify?token=" + token;
        helper.setText(
                "<h1>Please Verify Your Email</h1>" +
                        "<p>Click the link below to verify your email:</p>" +
                        "<a href=\"" + verificationLink + "\">Verify Email</a>",
                true
        );
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String to, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Reset your password");
        String resetLink = "/api/auth/reset-password?token=" + token;
        helper.setText(
                "<h1>Reset Your Password</h1>" +
                        "<p>Click the link below to reset your password:</p>" +
                        "<a href=\"" + resetLink + "\">Reset Password</a>" +
                        "<p>This link will expire in 1 hour.</p>",
                true
        );

        mailSender.send(message);
    }
}
