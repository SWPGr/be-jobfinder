// src/main/java/com/example/jobfinder/service/UserService.java
package com.example.jobfinder.service;

import com.example.jobfinder.dto.user.UserCreationRequest;
import com.example.jobfinder.dto.user.UserResponse;
import com.example.jobfinder.dto.user.UserUpdateRequest;
import com.example.jobfinder.dto.user.JobSeekerResponse;
import com.example.jobfinder.dto.user.EmployerResponse;
import com.example.jobfinder.dto.user.UserSearchRequest; // Thêm DTO tìm kiếm người dùng
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.UserMapper;
import com.example.jobfinder.mapper.JobSeekerMapper;
import com.example.jobfinder.mapper.EmployerMapper;
import com.example.jobfinder.model.*; // Import tất cả các model cần thiết
import com.example.jobfinder.repository.*; // Import tất cả các repository cần thiết
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

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

    // Dependencies (các Repository và Mapper cần thiết)
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder; // Để mã hóa mật khẩu
    UserMapper userMapper; // Để chuyển đổi giữa User entity và User DTO
    UserDetailsRepository userDetailRepository; // Để thao tác với UserDetail
    JobSeekerMapper jobSeekerMapper; // Để chuyển đổi UserDetail sang JobSeekerResponse
    EmployerMapper employerMapper; // Để chuyển đổi UserDetail sang EmployerResponse
    EducationRepository educationRepository; // Cần nếu UserDetail có Education và bạn cần lấy/lưu Education
    JobRepository jobRepository;

    // --- Phương thức CRUD cho User (Chủ yếu dành cho Admin) ---

    public List<UserResponse> getAllUsers() {
        // Gọi findAll() từ UserRepository để lấy tất cả User entities
        List<User> users = userRepository.findAll();
        // Chuyển đổi danh sách User entities sang danh sách UserResponse DTOs bằng UserMapper
        return userMapper.toUserResponseList(users);
    }

    public UserResponse getUserById(Long userId) {
        // Tìm người dùng theo ID. Nếu không tìm thấy, ném ngoại lệ USER_NOT_FOUND.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // Chuyển đổi User entity sang UserResponse DTO.
        return userMapper.toUserResponse(user);
    }

    @Transactional // Đảm bảo các thao tác với User và UserDetail được thực hiện trong cùng một transaction.
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
            userDetail.setYearsExperience(request.getYearsExperience());
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
            userDetail.setYearsExperience(null);
            userDetail.setResumeUrl(null);
            userDetail.setEducation(null);
        } else {
            // Đối với các vai trò khác (như ADMIN), tất cả các trường chuyên biệt sẽ là null.
            userDetail.setYearsExperience(null);
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

    @Transactional // Đảm bảo các thao tác được thực hiện trong cùng một transaction.
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        // 1. Tìm User hiện có.
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Kiểm tra nếu email thay đổi và email mới đã tồn tại.
        if (!existingUser.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXIST);
        }

        // 3. Cập nhật các trường chung của User (email, enabled).
        userMapper.updateUser(existingUser, request); // MapStruct sẽ giúp cập nhật các trường được định nghĩa.

        // 4. Cập nhật Role nếu roleName được cung cấp và khác với role hiện tại.
        // Đây là logic phức tạp, cần suy nghĩ kỹ nếu bạn cho phép chuyển đổi vai trò.
        if (request.getRoleName() != null && !request.getRoleName().equals(existingUser.getRole().getName())) {
            Role newRole = roleRepository.findByName(request.getRoleName())
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
            existingUser.setRole(newRole);
            // Quan trọng: Nếu vai trò thay đổi, các trường chuyên biệt trong UserDetail cần được reset/chỉnh sửa
            // Logic này sẽ được xử lý dưới đây trong phần UserDetail.
        }

        User updatedUser = userRepository.save(existingUser); // Lưu User đã cập nhật.

        // 5. Cập nhật UserDetail liên kết.
        UserDetail userDetail = userDetailRepository.findByUser(existingUser)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND)); // UserDetail phải tồn tại.

        userDetail.setFullName(request.getFullName());
        userDetail.setPhone(request.getPhone());
        userDetail.setLocation(request.getLocation());

        // 6. Cập nhật các trường đặc thù trong UserDetail dựa trên VAI TRÒ MỚI của người dùng.
        // Điều này đảm bảo rằng khi một user chuyển vai trò, các trường cũ không liên quan sẽ bị null.
        String currentRoleName = updatedUser.getRole().getName(); // Lấy vai trò sau khi update

        if (currentRoleName.equals("JOB_SEEKER")) {
            userDetail.setYearsExperience(request.getYearsExperience());
            userDetail.setResumeUrl(request.getResumeUrl());
            if (request.getEducationId() != null) {
                Education education = educationRepository.findById(request.getEducationId())
                        .orElseThrow(() -> new AppException(ErrorCode.EDUCATION_NOT_FOUND));
                userDetail.setEducation(education);
            } else {
                userDetail.setEducation(null); // Nếu request không có ID, xóa liên kết education
            }
            // Đảm bảo các trường Employer là null khi vai trò là JobSeeker.
            userDetail.setCompanyName(null);
            userDetail.setDescription(null);
            userDetail.setWebsite(null);
        } else if (currentRoleName.equals("EMPLOYER")) {
            userDetail.setCompanyName(request.getCompanyName());
            userDetail.setDescription(request.getDescription());
            userDetail.setWebsite(request.getWebsite());
            // Đảm bảo các trường JobSeeker là null khi vai trò là Employer.
            userDetail.setYearsExperience(null);
            userDetail.setResumeUrl(null);
            userDetail.setEducation(null);
        } else {
            // Nếu là ADMIN hoặc vai trò khác, đảm bảo tất cả các trường chuyên biệt là null.
            userDetail.setYearsExperience(null);
            userDetail.setResumeUrl(null);
            userDetail.setCompanyName(null);
            userDetail.setDescription(null);
            userDetail.setWebsite(null);
            userDetail.setEducation(null);
        }
        userDetailRepository.save(userDetail); // Lưu UserDetail đã cập nhật.

        return userMapper.toUserResponse(updatedUser); // Trả về UserResponse của người dùng đã cập nhật.
    }

    @Transactional // Đảm bảo thao tác xóa được thực hiện trong một transaction.
    public void deleteUser(Long userId) {
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(userToDelete); // Xóa User.
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(UserSearchRequest request) {
        log.info("Searching users with role: {}", request.getRoleName());

        // Lấy dữ liệu thô từ Repository (User, UserDetail, Role)
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
                            .createdAt(String.valueOf(user.getCreatedAt()))
                            .updatedAt(String.valueOf(user.getUpdatedAt()))
                            .roleName(role != null ? role.getName() : null)
                            .verified(user.getVerified())
                            .build();

                    // Map UserDetail nếu có
                    if (userDetail != null) {
                        userResponse.setFullName(userDetail.getFullName());
                        userResponse.setPhone(userDetail.getPhone());
                        userResponse.setLocation(userDetail.getLocation());
                        userResponse.setCompanyName(userDetail.getCompanyName());
                        userResponse.setWebsite(userDetail.getWebsite());
                    }

                    // Nếu là EMPLOYER, điền thêm totalJobsPosted
                    if (role != null && "EMPLOYER".equals(role.getName())) {
                        long totalJobs = jobRepository.countByEmployerId(user.getId());
                        userResponse.setTotalJobsPosted(totalJobs);
                    } else {
                        // Đảm bảo trường này là null nếu không phải employer
                        userResponse.setTotalJobsPosted(null);
                    }

                    return userResponse;
                })
                .collect(Collectors.toList());
    }

    public List<UserResponse> getUsersByRole(String roleName) {
        // Tùy chọn: Kiểm tra xem roleName có tồn tại trong hệ thống không
        // Điều này giúp tránh lỗi nếu roleName được truyền vào không hợp lệ.
        if (!roleRepository.findByName(roleName).isPresent()) {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }

        // Gọi phương thức findByRoleName từ UserRepository để lấy danh sách User.
        List<User> users = userRepository.findByRoleName(roleName);
        // Chuyển đổi danh sách User entities sang danh sách UserResponse DTOs.
        return userMapper.toUserResponseList(users);
    }

    public JobSeekerResponse getJobSeekerInfo(Long userId) {
        // 1. Tìm User entity theo ID.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Kiểm tra xem vai trò của User có phải là JOB_SEEKER hay không.
        if (!user.getRole().getName().equals("JOB_SEEKER")) {
            throw new AppException(ErrorCode.USER_IS_NOT_JOB_SEEKER); // Cần định nghĩa ErrorCode này.
        }

        // 3. Lấy UserDetail liên quan đến User. Phương thức findByUserId trong UserDetailsRepository
        //    được thiết kế để tải eager User và Education để tránh LazyInitializationException.
        UserDetail userDetail = userDetailRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND)); // UserDetail phải tồn tại.;

        // 4. Chuyển đổi UserDetail entity sang JobSeekerResponse DTO.
        return jobSeekerMapper.toJobSeekerResponse(userDetail);
    }

    /**
     * Lấy thông tin chi tiết hồ sơ của một nhà tuyển dụng (Employer) theo User ID.
     *
     * @param userId ID của người dùng (phải có vai trò EMPLOYER).
     * @return {@link EmployerResponse} chứa thông tin hồ sơ nhà tuyển dụng.
     * @throws AppException Nếu người dùng không tìm thấy (ErrorCode.USER_NOT_FOUND),
     * người dùng không phải là Employer (ErrorCode.USER_IS_NOT_EMPLOYER),
     * hoặc profile UserDetail không tìm thấy (ErrorCode.PROFILE_NOT_FOUND).
     */
    public EmployerResponse getEmployerInfo(Long userId) {
        // 1. Tìm User entity theo ID.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Kiểm tra xem vai trò của User có phải là EMPLOYER hay không.
        if (!user.getRole().getName().equals("EMPLOYER")) {
            throw new AppException(ErrorCode.USER_IS_NOT_EMPLOYER); // Cần định nghĩa ErrorCode này.
        }

        // 3. Lấy UserDetail liên quan đến User. findByUserId sẽ tải eager User.
        UserDetail userDetail = userDetailRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND)); // UserDetail phải tồn tại.;

        // 4. Chuyển đổi UserDetail entity sang EmployerResponse DTO.
        return employerMapper.toEmployerResponse(userDetail);
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
}