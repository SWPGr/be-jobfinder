package com.example.jobfinder.service;

import com.example.jobfinder.config.JwtUtil;
import com.example.jobfinder.dto.auth.*;
import com.example.jobfinder.model.Role;
import com.example.jobfinder.model.User;
import com.example.jobfinder.model.UserDetails;
import com.example.jobfinder.repository.RoleRepository;
import com.example.jobfinder.repository.UserDetailsRepository;
import com.example.jobfinder.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, UserDetailsRepository userDetailsRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public void register(RegisterRequest request) throws Exception {
        if(userRepository.findByEmail(request.getEmail()) != null) {
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
        user.setVerified(false);

        userRepository.save(user);

        UserDetails userDetail = new UserDetails();
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

        User user = userRepository.findByEmail(request.getEmail());
        if(!user.isVerified()) {
            throw new RuntimeException("Please verify your email first");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getName());
        return new LoginResponse(token, user.getRole().getName());
    }

    public void verifyEmail(String token) throws Exception {
        User user = userRepository.findByVerificationToken(token);
        if(user == null) {
            throw new Exception("Invalid verification token");
        }
        user.setVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    public void resendVerificationEmail(String email) throws Exception {
        User user = userRepository.findByEmail(email);
        if(user == null) {
            throw new Exception("User not found");
        }
        if(user.isVerified()) {
            throw new Exception("User already verified");
        }

        user.setVerificationToken(UUID.randomUUID().toString());
        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());
    }

    public void forgotPassowrd(ForgotPasswordRequest request) throws Exception {
        User user = userRepository.findByEmail(request.getEmail());
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
        User user = userRepository.findByResetPasswordToken(request.getToken());
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
}
