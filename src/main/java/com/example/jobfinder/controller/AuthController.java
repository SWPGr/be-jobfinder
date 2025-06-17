package com.example.jobfinder.controller;

import com.example.jobfinder.dto.auth.*;
import com.example.jobfinder.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    /****
     * Constructs an AuthController with the specified authentication service.
     *
     * @param authService the authentication service used to handle authentication logic
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Handles user registration by accepting registration details and initiating the registration process.
     *
     * @param request the registration details provided by the user
     * @return a response indicating successful registration and prompting email verification
     * @throws Exception if registration fails
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) throws Exception {
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
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("http://localhost:3030/api/auth/verify"))
                .build();
    }

    /****
     * Handles a request to resend the email verification link to a user.
     *
     * @param request a map containing the user's email address under the key "email"
     * @return a response indicating that the verification email was resent successfully
     * @throws Exception if an error occurs during the resend process
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationEmail(@RequestBody Map<String, String> request)throws Exception {
        String email = request.get("email");
        authService.resendVerificationEmail(email);
        return ResponseEntity.ok("Email resend successfully.");
    }

    /**
     * Initiates the password reset process by sending a reset email to the user.
     *
     * @param request the password reset request containing the user's email address
     * @return a response indicating that the password reset email was sent
     * @throws Exception if an error occurs during the password reset process
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request)throws Exception {
        authService.forgotPassword(request);
        return ResponseEntity.ok("Password reset email sent. Please check your email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) throws Exception {
        authService.resetPassword(request);
        return ResponseEntity.ok("Password reset successfully.");
    }
}
