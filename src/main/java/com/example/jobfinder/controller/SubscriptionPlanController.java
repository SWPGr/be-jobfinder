// src/main/java/com/example/jobfinder/controller/SubscriptionPlanController.java
package com.example.jobfinder.controller;

import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.dto.SubscriptionPlan.SubscriptionPlanCreationRequest;
import com.example.jobfinder.dto.SubscriptionPlan.SubscriptionPlanResponse;
import com.example.jobfinder.dto.SubscriptionPlan.SubscriptionPlanUpdateRequest;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.SubscriptionPlan;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.UserRepository;
import com.example.jobfinder.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Để quản lý quyền truy cập
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscription-plans") // Base URL cho các API này
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("permitAll()") // Cho phép mọi truy cập (kể cả anonymous)
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getAllSubscriptionPlans() { // Sửa kiểu trả về
        try {
            List<SubscriptionPlanResponse> plans = subscriptionPlanService.getAllSubscriptionPlans();
            ApiResponse<List<SubscriptionPlanResponse>> apiResponse = ApiResponse.<List<SubscriptionPlanResponse>>builder() // Sửa kiểu
                    .code(200)
                    .message("Subscription plans retrieved successfully")
                    .result(plans)
                    .build();
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            ApiResponse<List<SubscriptionPlanResponse>> apiResponse = ApiResponse.<List<SubscriptionPlanResponse>>builder() // Sửa kiểu
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve subscription plans: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/by-role")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getSubscriptionPlansByCurrentUserRole(
            Authentication authentication) {
        try {
            String currentUserEmail = authentication.getName();
            User currentUser = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            Long currentRoleId = currentUser.getRole().getId();

            List<SubscriptionPlanResponse> plans = subscriptionPlanService.getSubscriptionPlansByRoleId(currentRoleId);
            return ResponseEntity.ok(ApiResponse.<List<SubscriptionPlanResponse>>builder()
                    .code(HttpStatus.OK.value())
                    .message("Subscription plans fetched successfully for current user's role.")
                    .result(plans)
                    .build());
        } catch (AppException e) {

            return ResponseEntity.status(e.getErrorCode().getErrorCode())
                    .body(ApiResponse.<List<SubscriptionPlanResponse>>builder()
                            .code(e.getErrorCode().getErrorCode())
                            .message(e.getErrorCode().getErrorMessage())
                            .build());
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<SubscriptionPlanResponse>>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Lỗi nội bộ server khi lấy gói đăng ký theo vai trò của người dùng.")
                            .build());
        }
    }

    // CREATE NEW PLAN
    // Endpoint: POST /api/subscription-plans
    // Chỉ người dùng có vai trò 'ADMIN' mới có thể tạo gói.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Giả định bạn đã cấu hình roles trong Spring Security
    public ResponseEntity<ApiResponse<SubscriptionPlan>> createSubscriptionPlan(@RequestBody SubscriptionPlanCreationRequest request) {
        try {
            SubscriptionPlan newPlan = subscriptionPlanService.createSubscriptionPlan(request);
            ApiResponse<SubscriptionPlan> apiResponse = ApiResponse.<SubscriptionPlan>builder()
                    .code(200)
                    .message("Subscription plan created successfully")
                    .result(newPlan)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse); // Trả về 201 Created
        } catch (AppException e) {
            System.err.println("Application Error creating subscription plan: " + e.getMessage());
            ApiResponse<SubscriptionPlan> apiResponse = ApiResponse.<SubscriptionPlan>builder()
                    .code(e.getErrorCode().getErrorCode())
                    .message(e.getErrorCode().getErrorMessage())
                    .build();
            return ResponseEntity.status(e.getErrorCode().getErrorCode()).body(apiResponse);
        } catch (Exception e) {
            System.err.println("Internal Server Error creating subscription plan: " + e.getMessage());
            ApiResponse<SubscriptionPlan> apiResponse = ApiResponse.<SubscriptionPlan>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to create subscription plan: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    // UPDATE EXISTING PLAN
    // Endpoint: PUT /api/subscription-plans/{id}
    // Chỉ người dùng có vai trò 'ADMIN' mới có thể cập nhật gói.
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionPlan>> updateSubscriptionPlan(@PathVariable Long id, @RequestBody SubscriptionPlanUpdateRequest request) {
        try {
            SubscriptionPlan updatedPlan = subscriptionPlanService.updateSubscriptionPlan(id, request);
            ApiResponse<SubscriptionPlan> apiResponse = ApiResponse.<SubscriptionPlan>builder()
                    .code(200)
                    .message("Subscription plan updated successfully")
                    .result(updatedPlan)
                    .build();
            return ResponseEntity.ok(apiResponse);
        } catch (AppException e) {
            System.err.println("Application Error updating subscription plan: " + e.getMessage());
            ApiResponse<SubscriptionPlan> apiResponse = ApiResponse.<SubscriptionPlan>builder()
                    .code(e.getErrorCode().getErrorCode())
                    .message(e.getErrorCode().getErrorMessage())
                    .build();
            return ResponseEntity.status(e.getErrorCode().getErrorCode()).body(apiResponse);
        } catch (Exception e) {
            System.err.println("Internal Server Error updating subscription plan: " + e.getMessage());
            ApiResponse<SubscriptionPlan> apiResponse = ApiResponse.<SubscriptionPlan>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to update subscription plan: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    // DELETE PLAN
    // Endpoint: DELETE /api/subscription-plans/{id}
    // Chỉ người dùng có vai trò 'ADMIN' mới có thể xóa gói.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteSubscriptionPlan(@PathVariable Long id) {
        try {
            subscriptionPlanService.deleteSubscriptionPlan(id);
            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .code(200)
                    .message("Subscription plan deleted successfully")
                    .result("Plan with ID " + id + " deleted.")
                    .build();
            return ResponseEntity.ok(apiResponse);
        } catch (AppException e) {
            System.err.println("Application Error deleting subscription plan: " + e.getMessage());
            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .code(e.getErrorCode().getErrorCode())
                    .message(e.getErrorCode().getErrorMessage())
                    .build();
            return ResponseEntity.status(e.getErrorCode().getErrorCode()).body(apiResponse);
        } catch (Exception e) {
            System.err.println("Internal Server Error deleting subscription plan: " + e.getMessage());
            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to delete subscription plan: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
}