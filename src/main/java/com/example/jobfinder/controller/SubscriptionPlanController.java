// src/main/java/com/example/jobfinder/controller/SubscriptionPlanController.java
package com.example.jobfinder.controller;

import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.dto.subscriptionPlan.SubscriptionPlanResponse;
import com.example.jobfinder.exception.AppException;
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
            List<SubscriptionPlanResponse> plans = subscriptionPlanService.getSubscriptionPlansByCurrentUserRole();
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
            // Xử lý các lỗi không mong muốn khác
            System.err.println("Lỗi nội bộ server khi lấy gói đăng ký theo vai trò của người dùng: " + e.getMessage());
            e.printStackTrace(); // In stack trace ra console để debug
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<SubscriptionPlanResponse>>builder()
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Lỗi nội bộ server khi lấy gói đăng ký theo vai trò của người dùng.")
                            .build());
        }
    }
}