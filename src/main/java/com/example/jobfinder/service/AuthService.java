package com.example.jobfinder.service;
import com.example.jobfinder.config.JwtUtil;
import com.example.jobfinder.dto.auth.*;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.UserMapper;
import com.example.jobfinder.model.*;
import com.example.jobfinder.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    GoogleTokenVerifierService googleTokenVerifierService;
    SubscriptionRepository subscriptionRepository;
    SubscriptionPlanRepository subscriptionPlanRepository;
    UserMapper userMapper;


    public void register(RegisterRequest request) throws Exception {
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        Role role = roleRepository.findByName(request.getRoleName()).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerified(0);
        user.setIsActive(true);

        userRepository.save(user);

        UserDetail userDetail = new UserDetail();
        userDetail.setUser(user);

        userDetailsRepository.save(userDetail);
        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());
        assignDefaultBasicSubscription(user);
        log.info("Assigned default BASIC plan to new user: {}", user.getEmail());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user;
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()
                    )
            );
            user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found after authentication: " + request.getEmail()));

        } catch (BadCredentialsException e) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }

        if (user.getVerified() == 0) {
            throw new AppException(ErrorCode.USER_NOT_VERIFIED); // Ném lỗi
        }

        if (user.getIsActive() == false) {
            throw new AppException(ErrorCode.ACCOUNT_BLOCKED); // Ném lỗi
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getName());

        return LoginResponse.builder()
                .token(token)
                .role(user.getRole().getName())
                .build();
    }

    @Transactional
    public LoginResponse loginWithGoogleToken(String idToken) {
        GoogleUserInfo userInfo = googleTokenVerifierService.verify(idToken);

        if (userInfo == null || userInfo.getEmail() == null) {
            throw new AppException(ErrorCode.INVALID_EMAIL); // Ném lỗi
        }

        User user = userRepository.findByEmail(userInfo.getEmail())
                .orElseGet(() -> createNewGoogleUser(userInfo.getEmail(), userInfo.getName()));


        if (user.getVerified() == 0) {
            return LoginResponse.builder()
                    .build();
        }

        // 4. Kiểm tra trạng thái tài khoản: bị block (isActive)
        if (user.getIsActive() == false) {
            return LoginResponse.builder()
                    .build();
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getName());

        return LoginResponse.builder()
                .token(token)
                .role(user.getRole().getName())
                .build();
    }

    private User createNewGoogleUser(String email, String name) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setIsPremium(false);
        newUser.setVerified(1); // Google email được coi là đã xác minh
        newUser.setIsActive(true);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        newUser.setRole(defaultRole);
        return userRepository.save(newUser);
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

    public void changePassword(ChangePasswordRequest request) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
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

    @Transactional
    public void assignDefaultBasicSubscription(User user) {
        // 1. Lấy role ID của người dùng
        Long userRoleId = user.getRole().getId(); // Giả định User có trường Role và Role có ID

        // 2. Tìm gói Basic mặc định dựa trên tên gói VÀ role ID
        SubscriptionPlan basicPlan = subscriptionPlanRepository
                .findBySubscriptionPlanNameAndRoleId("Basic Plan", userRoleId)
                .orElseThrow(() -> {
                    log.error("CRITICAL: Default BASIC subscription plan not found for role ID {} in database!", userRoleId);
                    return new AppException(ErrorCode.PLAN_NOT_FOUND);
                });

        // 3. Đảm bảo giá gói basic là 0
        if (basicPlan.getPrice() != 0) {
            log.error("CRITICAL: BASIC plan for role ID {} has price {}. It must be 0. Please correct the SubscriptionPlan data.", userRoleId, basicPlan.getPrice());
            throw new AppException(ErrorCode.INVALID_PLAN_CONFIGURATION);
        }

        // 4. Tạo Subscription mới cho người dùng
        Subscription newSubscription = Subscription.builder()
                .user(user)
                .plan(basicPlan) // Đổi tên trường từ .plan thành .subscriptionPlan nếu Entity của bạn là subscriptionPlan
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusYears(100))
                .isActive(true)
                .build();

        subscriptionRepository.save(newSubscription);
        log.info("Assigned default BASIC plan (ID: {}) to new user: {} with role ID: {}", basicPlan.getId(), user.getEmail(), userRoleId);
    }
}
