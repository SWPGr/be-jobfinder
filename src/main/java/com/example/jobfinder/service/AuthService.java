package com.example.jobfinder.service;

import com.example.jobfinder.config.JwtUtil;
import com.example.jobfinder.dto.auth.*;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.Role;
import com.example.jobfinder.model.User;
import com.example.jobfinder.model.UserDetail;
import com.example.jobfinder.repository.RoleRepository;
import com.example.jobfinder.repository.UserDetailsRepository;
import com.example.jobfinder.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    UserRepository userRepository;
    RoleRepository roleRepository;
    UserDetailsRepository userDetailsRepository;
    PasswordEncoder passwordEncoder;
    AuthenticationManager authenticationManager;
    JwtUtil jwtUtil;
    EmailService emailService;


    public void register(RegisterRequest request) throws Exception {
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new Exception("Email already exists");
        }

        Role role = roleRepository.findByName(request.getRoleName()).orElseThrow(() -> new Exception("Role not found"));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerified(0);

        userRepository.save(user);

        UserDetail userDetail = new UserDetail();
        userDetail.setUser(user);

        userDetailsRepository.save(userDetail);
        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(request.getEmail()));
        if(user.getVerified() == 0) {
            throw new RuntimeException("Please verify your email first");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getName());
        return new LoginResponse(token, user.getRole().getName());
    }
    @Transactional
    public LoginResponse handleGoogleLogin(OidcUser oidcUser) {
        String email = oidcUser.getEmail();
        log.debug("Processing Google login for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.debug("Creating new user for email: {}", email);
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setPassword("");
                    Role userRole = roleRepository.findByName("JOB_SEEKER")
                            .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
                    newUser.setRole(userRole);
                    return userRepository.save(newUser);
                });
        String jwt = jwtUtil.generateToken(user.getEmail(), user.getRole().getName());
        log.debug("Generated JWT for user: {}", email);
        return new LoginResponse(jwt, user.getRole().getName());
    }

    public void verifyEmail(String token) throws Exception {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new UsernameNotFoundException(token));
        if(user == null) {
            throw new Exception("Invalid verification token");
        }
        user.setVerified(1);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    public void resendVerificationEmail(String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        if(user == null) {
            throw new Exception("User not found");
        }
        if(user.getVerified() == 1) {
            throw new Exception("User already verified");
        }

        user.setVerificationToken(UUID.randomUUID().toString());
        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());
    }

    public void forgotPassword(ForgotPasswordRequest request) throws Exception {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(request.getEmail()));
        if(user == null) {
            throw new Exception("User not found");
        }

        String resetToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }

    public void resetPassword(ResetPasswordRequest request) throws Exception {
        User user = userRepository.findByResetPasswordToken(request.getToken())
                .orElseThrow(() -> new UsernameNotFoundException(request.getToken()));
        if(user == null) {
            throw new Exception("Invalid or expired reset token");
        }

        if(user.getResetPasswordExpiry().isBefore(LocalDateTime.now())) {
            throw new Exception("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpiry(null);
        userRepository.save(user);
    }

    // Helper method to get authenticated user ID
    Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)); // Hoặc UNAUTHENTICATED
        return currentUser.getId();
    }

    // Helper method to get authenticated user entity
    User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
