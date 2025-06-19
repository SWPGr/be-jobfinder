// src/main/java/com/example/jobfinder/controller/UserController.java
package com.example.jobfinder.controller;

import com.example.jobfinder.dto.ApiResponse; // Đảm bảo import đúng đường dẫn ApiResponse của bạn
import com.example.jobfinder.dto.user.UserCreationRequest;
import com.example.jobfinder.dto.user.UserResponse;
import com.example.jobfinder.dto.user.UserSearchRequest; // Hoặc UserSearchParams nếu bạn dùng tên đó
import com.example.jobfinder.dto.user.UserUpdateRequest;
import com.example.jobfinder.service.UserService;
import jakarta.validation.Valid; // Để kích hoạt Bean Validation
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize; // Để phân quyền
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Đánh dấu đây là một REST Controller
@RequestMapping("api/users") // Định nghĩa base path cho tất cả các API trong controller này
@RequiredArgsConstructor // Tự động tạo constructor với các trường final, giúp inject dependency
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) // Đặt các trường là private final
@Slf4j // Để sử dụng logging
public class UserController {

    UserService userService; // Inject UserService vào Controller
    @PostMapping
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        log.info("API: Tạo người dùng mới với email: {}", request.getEmail());
        UserResponse user = userService.createUser(request);
        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.OK.value()) // HTTP 200 OK
                .message("User created successfully")
                .result(user)
                .build();
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('READ_USER_PROFILE') or #userId == authentication.principal.id")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long userId) {
        log.info("API: Lấy thông tin người dùng với ID: {}", userId);
        UserResponse user = userService.getUserById(userId);
        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.OK.value())
                .message("User fetched successfully")
                .result(user)
                .build();
    }

    @GetMapping("/jobseekers")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN mới có thể xem danh sách JobSeekers
    public ApiResponse<List<UserResponse>> getAllJobSeekers() {
        log.info("API: Lấy danh sách tất cả JobSeekers");
        UserSearchRequest searchRequest = UserSearchRequest.builder()
                .roleName("JOB_SEEKER") // Chỉ định rõ role là JOB_SEEKER
                .build();
        List<UserResponse> jobSeekers = userService.searchUsers(searchRequest);
        return ApiResponse.<List<UserResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Job Seekers fetched successfully")
                .result(jobSeekers)
                .build();
    }

    @GetMapping("/employers")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN mới có thể xem danh sách Employers
    public ApiResponse<List<UserResponse>> getAllEmployers() {
        log.info("API: Lấy danh sách tất cả Employers");
        UserSearchRequest searchRequest = UserSearchRequest.builder()
                .roleName("EMPLOYER") // Chỉ định rõ role là EMPLOYER
                .build();
        List<UserResponse> employers = userService.searchUsers(searchRequest);
        return ApiResponse.<List<UserResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Employers fetched successfully")
                .result(employers)
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN mới có thể xem danh sách người dùng
    public ApiResponse<List<UserResponse>> searchUsers(@ModelAttribute UserSearchRequest request) {
        log.info("API: Tìm kiếm người dùng với tiêu chí: {}", request);
        List<UserResponse> users = userService.searchUsers(request);
        return ApiResponse.<List<UserResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Users fetched successfully")
                .result(users)
                .build();
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long userId, @RequestBody @Valid UserUpdateRequest request) {
        log.info("API: Cập nhật người dùng với ID: {}", userId);
        UserResponse updatedUser = userService.updateUser(userId, request);
        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.OK.value())
                .message("User updated successfully")
                .result(updatedUser)
                .build();
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN mới có thể xóa người dùng
    public ApiResponse<Void> deleteUser(@PathVariable Long userId) {
        log.info("API: Xóa người dùng với ID: {}", userId);
        userService.deleteUser(userId);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("User deleted successfully")
                .build();
    }
}