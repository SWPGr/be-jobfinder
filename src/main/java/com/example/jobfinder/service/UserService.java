// src/main/java/com/example/jobfinder/service/UserService.java
package com.example.jobfinder.service;

import com.example.jobfinder.dto.user.*;
import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.UserMapper;
import com.example.jobfinder.mapper.JobSeekerMapper;
import com.example.jobfinder.mapper.EmployerMapper;
import com.example.jobfinder.model.*; // Import tất cả các model cần thiết
import com.example.jobfinder.repository.*; // Import tất cả các repository cần thiết
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// Annotation @Service để Spring tự động nhận diện đây là một Service component.
// @RequiredArgsConstructor sẽ tự động tạo constructor cho các final fields (dependency injection).
// @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) giúp các trường được khai báo là private final.
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    UserDetailsRepository userDetailRepository;
    EducationRepository educationRepository;
    JobRepository jobRepository;
    ExperienceRepository experienceRepository;
    ApplicationRepository applicationRepository;
    EmailService emailService;

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toUserResponseList(users);
    }

    public UserResponse getUserById(Long userId) {
        // Tìm người dùng theo ID. Nếu không tìm thấy, ném ngoại lệ USER_NOT_FOUND.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // Chuyển đổi User entity sang UserResponse DTO.
        return userMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse createUser(UserCreationRequest request) {
        // 1. Kiểm tra xem email đã tồn tại chưa để tránh trùng lặp.
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXIST);
        }

        // 2. Tìm đối tượng Role dựa trên roleName trong request.
        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        // 3. Tạo User entity từ request và mã hóa mật khẩu.
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Mã hóa mật khẩu trước khi lưu.
        user.setRole(role); // Gán vai trò cho người dùng.
        user.setVerified(0); // Mặc định chưa được xác minh khi tạo bởi Admin (có thể cần email verification sau).
        User savedUser = userRepository.save(user); // Lưu User vào database.

        // 4. Tạo UserDetail entity liên kết với User vừa tạo.
        UserDetail userDetail = UserDetail.builder()
                .user(savedUser) // Liên kết UserDetail với User.
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .location(request.getLocation())
                // Các trường khác (yearsExperience, resumeUrl, companyName, description, website, education)
                // sẽ được set dựa trên vai trò hoặc để null nếu không liên quan.
                .build();

        // 5. Gán các thuộc tính chuyên biệt trong UserDetail dựa trên vai trò của người dùng.
        if (role.getName().equals("JOB_SEEKER")) {
            if (request.getUserExperience() != null) {
                Experience experience = experienceRepository.findById(request.getUserExperience())
                        .orElseThrow(() -> new AppException(ErrorCode.EXPERIENCE_NOT_FOUND));
                userDetail.setExperience(experience);
            }
            userDetail.setResumeUrl(request.getResumeUrl());
            if (request.getEducationId() != null) {
                // Nếu có educationId trong request, tìm và gán Education entity.
                Education education = educationRepository.findById(request.getEducationId())
                        .orElseThrow(() -> new AppException(ErrorCode.EDUCATION_NOT_FOUND)); // Cần định nghĩa ErrorCode này.
                userDetail.setEducation(education);
            }
            // Đảm bảo các trường của Employer là null khi tạo JobSeeker.
            userDetail.setCompanyName(null);
            userDetail.setDescription(null);
            userDetail.setWebsite(null);
        } else if (role.getName().equals("EMPLOYER")) {
            userDetail.setCompanyName(request.getCompanyName());
            userDetail.setDescription(request.getDescription());
            userDetail.setWebsite(request.getWebsite());
            // Đảm bảo các trường của JobSeeker là null khi tạo Employer.
            userDetail.setExperience(null);
            userDetail.setResumeUrl(null);
            userDetail.setEducation(null);
        } else {
            // Đối với các vai trò khác (như ADMIN), tất cả các trường chuyên biệt sẽ là null.
            userDetail.setExperience(null);
            userDetail.setResumeUrl(null);
            userDetail.setCompanyName(null);
            userDetail.setDescription(null);
            userDetail.setWebsite(null);
            userDetail.setEducation(null);
        }

        userDetailRepository.save(userDetail); // Lưu UserDetail vào database.

        // 6. Trả về UserResponse của người dùng đã tạo.
        return userMapper.toUserResponse(savedUser);
    }


    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(UserSearchRequest request) {
        log.info("Searching users with role: {}", request.getRoleName());
        List<Object[]> results = userRepository.findUsersWithDetailsAndRole(request.getRoleName());

        return results.stream()
                .map(row -> {
                    User user = (User) row[0];
                    UserDetail userDetail = (UserDetail) row[1];
                    Role role = (Role) row[2];

                    UserResponse userResponse = UserResponse.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .isPremium(user.getIsPremium())
                            .createdAt(user.getCreatedAt()) // Chuyển đổi LocalDateTime sang String
                            .updatedAt(user.getUpdatedAt()) // Chuyển đổi LocalDateTime sang String
                            .role(role != null ? new SimpleNameResponse(role.getId(), role.getName()) : null)
                            .verified(user.getVerified())
                            .totalApplications(null) // Khởi tạo là null
                            .totalJobsPosted(null)   // Khởi tạo là null
                            .build();

                    // Map UserDetail nếu có
                    if (userDetail != null) {
                        userResponse.setFullName(userDetail.getFullName());
                        userResponse.setPhone(userDetail.getPhone());
                        userResponse.setLocation(userDetail.getLocation());
                        userResponse.setCompanyName(userDetail.getCompanyName());
                        userResponse.setWebsite(userDetail.getWebsite());
                    }

                    // Điền totalJobsPosted cho EMPLOYER (chỉ đếm job active)
                    if (role != null && "EMPLOYER".equals(role.getName())) {
                        long totalActiveJobs = jobRepository.countByEmployerIdAndActiveTrue(user.getId());
                        userResponse.setTotalJobsPosted(totalActiveJobs);
                    }
                    // Điền totalApplications cho JOB_SEEKER
                    else if (role != null && "JOB_SEEKER".equals(role.getName())) {
                        long totalApplicationsCount = applicationRepository.countByApplicantId(user.getId());
                        userResponse.setTotalApplications(totalApplicationsCount);
                    }

                    return userResponse;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getTotalUsers() {
        log.info("Service: Đếm tổng số người dùng.");
        return userRepository.countAllUsers();
    }

    @Transactional(readOnly = true)
    public long getTotalJobSeekers() {
        log.info("Service: Đếm tổng số JobSeekers.");
        return userRepository.countUsersByRoleName("JOB_SEEKER");
    }

    @Transactional(readOnly = true)
    public long getTotalEmployers() {
        log.info("Service: Đếm tổng số Employers.");
        return userRepository.countUsersByRoleName("EMPLOYER");
    }

    @Transactional
    public void updateUserStatus(UserStatusUpdateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        boolean oldStatus = user.getIsActive(); // Lưu trạng thái cũ
        user.setIsActive(request.getIsActive()); // Cập nhật trạng thái active
        userRepository.save(user);
        log.info("User with ID {} active status updated to {}", request.getUserId(), request.getIsActive());
        if (oldStatus && request.getIsActive() == false) {
            try {
                emailService.sendAccountBlockedEmail(user.getEmail(), "Vi phạm quy tắc cộng đồng về nội dung đăng tải."); // Ví dụ về lý do
                log.info("Sent account blocked email to user: {}", user.getEmail());
            } catch (MessagingException e) {
                log.error("Failed to send account blocked email to {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }
}