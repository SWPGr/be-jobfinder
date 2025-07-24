package com.example.jobfinder.controller;

import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.dto.user.*;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.service.UserService;
import jakarta.validation.Valid; // Để kích hoạt Bean Validation
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {

    UserService userService;
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

    @GetMapping("/total-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Long> getTotalUsers() {
        log.info("API: Lấy tổng số lượng người dùng.");
        long totalUsers = userService.getTotalUsers();
        return ApiResponse.<Long>builder()
                .code(HttpStatus.OK.value())
                .message("Total users fetched successfully")
                .result(totalUsers)
                .build();
    }

    @GetMapping("/total-jobseekers")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Long> getTotalJobSeekers() {
        log.info("API: Lấy tổng số lượng JobSeekers.");
        long totalJobSeekers = userService.getTotalJobSeekers();
        return ApiResponse.<Long>builder()
                .code(HttpStatus.OK.value())
                .message("Total Job Seekers fetched successfully")
                .result(totalJobSeekers)
                .build();
    }

    @GetMapping("/total-employers")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Long> getTotalEmployers() {
        log.info("API: Lấy tổng số lượng Employers.");
        long totalEmployers = userService.getTotalEmployers();
        return ApiResponse.<Long>builder()
                .code(HttpStatus.OK.value())
                .message("Total Employers fetched successfully")
                .result(totalEmployers)
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
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserResponse>> getAllJobSeekers() {
        log.info("API: Lấy danh sách tất cả JobSeekers");
        UserSearchRequest searchRequest = UserSearchRequest.builder()
                .roleName("JOB_SEEKER")
                .build();
        List<UserResponse> jobSeekers = userService.searchUsers(searchRequest);
        return ApiResponse.<List<UserResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Job Seekers fetched successfully")
                .result(jobSeekers)
                .build();
    }

    @GetMapping("/employers")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserResponse>> getAllEmployers() {
        log.info("API: Lấy danh sách tất cả Employers");
        UserSearchRequest searchRequest = UserSearchRequest.builder()
                .roleName("EMPLOYER")
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

    @PutMapping("/status") // Dùng PUT vì đây là cập nhật trạng thái của tài nguyên đã tồn tại
    @PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN mới có quyền này
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(@RequestBody @Valid UserStatusUpdateRequest request) {
        try {
            userService.updateUserStatus(request);
            String message = request.getIsActive() ? "User activated successfully." : "User deactivated successfully.";
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .code(HttpStatus.OK.value())
                    .message(message)
                    .build());
        } catch (AppException e) {
            return ResponseEntity.status(e.getErrorCode().getErrorCode())
                    .body(ApiResponse.<Void>builder()
                            .code(e.getErrorCode().getErrorCode())
                            .message(e.getErrorCode().getErrorMessage())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Failed to update user status: " + e.getMessage())
                            .build());
        }
    }
}