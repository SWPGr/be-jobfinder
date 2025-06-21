package com.example.jobfinder.controller;

import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.dto.auth.*;
import com.example.jobfinder.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) throws Exception {
        authService.register(request);
        return ResponseEntity.ok("Registration successful. Please check your email for verification.");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("google/success")
    public ApiResponse<LoginResponse> googleLoginSuccess(@AuthenticationPrincipal OidcUser oidcUser) {
        log.debug("Google login callback received for user: {}", oidcUser.getEmail());
        LoginResponse loginResponse = authService.handleGoogleLogin(oidcUser);
        return new ApiResponse<>(200, "Google login successful", loginResponse);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token)throws Exception {
        authService.verifyEmail(token);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("http://localhost:3030/api/auth/verify"))
                .build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationEmail(@RequestBody Map<String, String> request)throws Exception {
        String email = request.get("email");
        authService.resendVerificationEmail(email);
        return ResponseEntity.ok("Email resend successfully.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request)throws Exception {
        authService.forgotPassword(request);
        return ResponseEntity.ok("Password reset email sent. Please check your email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request) throws Exception {
        authService.resetPassword(request);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(200)
                .message("Password reset successfully")
                .result("OK")
                .build();

        return ResponseEntity.ok(response);
    }
}
