package com.example.jobfinder.controller;

import com.example.jobfinder.dto.auth.*;
import com.example.jobfinder.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) throws Exception {
        authService.register(request);
        return ResponseEntity.ok("Registration successful. Please check your email for verification.");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token)throws Exception {
        authService.verifyEmail(token);
        return ResponseEntity.ok("Email verification successfully.");
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationEmail(@RequestBody Map<String, String> request)throws Exception {
        String email = request.get("email");
        authService.resendVerificationEmail(email);
        return ResponseEntity.ok("Email resend successfully.");
    }

    @PostMapping("/forgot-passowrd")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request)throws Exception {
        authService.forgotPassowrd(request);
        return ResponseEntity.ok("Password reset email sent. Please check your email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) throws Exception {
        authService.resetPassword(request);
        return ResponseEntity.ok("Password reset successfully.");
    }
}
