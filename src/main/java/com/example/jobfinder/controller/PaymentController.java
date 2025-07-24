// src/main/java/com/example/jobfinder/controller/PaymentController.java
package com.example.jobfinder.controller;

import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.dto.payment.PaymentResponse; // Import PaymentResponse DTO
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.UserRepository;
import com.example.jobfinder.service.SubscriptionPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final SubscriptionPaymentService subscriptionPaymentService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPaymentHistory() { // Thay đổi kiểu trả về
        try {
            List<PaymentResponse> payments = subscriptionPaymentService.getAllPaymentHistory(); // Gọi phương thức Service đã cập nhật
            ApiResponse<List<PaymentResponse>> apiResponse = ApiResponse.<List<PaymentResponse>>builder() // Thay đổi kiểu trả về
                    .code(200)
                    .message("All payment history retrieved successfully")
                    .result(payments)
                    .build();
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            System.err.println("Error retrieving all payment history: " + e.getMessage());
            ApiResponse<List<PaymentResponse>> apiResponse = ApiResponse.<List<PaymentResponse>>builder() // Thay đổi kiểu trả về
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve all payment history: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/my-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPaymentHistory() { // Thay đổi kiểu trả về
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = authentication.getName();

            User currentUser = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            List<PaymentResponse> payments = subscriptionPaymentService.getUserPaymentHistory(currentUser.getId()); // Gọi phương thức Service đã cập nhật
            ApiResponse<List<PaymentResponse>> apiResponse = ApiResponse.<List<PaymentResponse>>builder() // Thay đổi kiểu trả về
                    .code(200)
                    .message("My payment history retrieved successfully")
                    .result(payments)
                    .build();
            return ResponseEntity.ok(apiResponse);
        } catch (AppException e) {
            System.err.println("Application Error retrieving user payment history: " + e.getMessage());
            ApiResponse<List<PaymentResponse>> apiResponse = ApiResponse.<List<PaymentResponse>>builder() // Thay đổi kiểu trả về
                    .code(e.getErrorCode().getErrorCode())
                    .message(e.getErrorCode().getErrorMessage())
                    .build();
            return ResponseEntity.status(e.getErrorCode().getErrorCode()).body(apiResponse);
        } catch (Exception e) {
            System.err.println("Internal Server Error retrieving user payment history: " + e.getMessage());
            ApiResponse<List<PaymentResponse>> apiResponse = ApiResponse.<List<PaymentResponse>>builder() // Thay đổi kiểu trả về
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve my payment history: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
}