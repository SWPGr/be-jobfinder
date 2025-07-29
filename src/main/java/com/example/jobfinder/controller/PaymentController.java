// src/main/java/com/example/jobfinder/controller/PaymentController.java
package com.example.jobfinder.controller;

import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.dto.PageResponse;
import com.example.jobfinder.dto.payment.PaymentResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.UserRepository;
import com.example.jobfinder.service.SubscriptionPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final SubscriptionPaymentService subscriptionPaymentService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getAllPaymentHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "paidAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String paymentStatus
    ) {
        try {
            Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            LocalDateTime parsedFromDate = parseDate(fromDate, false); // Parse fromDate
            LocalDateTime parsedToDate = parseDate(toDate, true);     // Parse toDate

            PageResponse<PaymentResponse> paymentsPageResponse = subscriptionPaymentService.getAllPaymentHistory(pageable, parsedFromDate, parsedToDate, paymentStatus);

            ApiResponse<PageResponse<PaymentResponse>> apiResponse = ApiResponse.<PageResponse<PaymentResponse>>builder()
                    .code(HttpStatus.OK.value())
                    .message("All payment history retrieved successfully")
                    .result(paymentsPageResponse)
                    .build();
            return ResponseEntity.ok(apiResponse);
        } catch (DateTimeParseException e) {
            System.err.println("Date parsing error: " + e.getMessage());
            ApiResponse<PageResponse<PaymentResponse>> apiResponse = ApiResponse.<PageResponse<PaymentResponse>>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message("Invalid date format. Please use ISO 8601 format (e.g., YYYY-MM-DDTHH:MM:SS) or YYYY-MM-DD.")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        } catch (Exception e) {
            System.err.println("Error retrieving all payment history: " + e.getMessage());
            e.printStackTrace();
            ApiResponse<PageResponse<PaymentResponse>> apiResponse = ApiResponse.<PageResponse<PaymentResponse>>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve all payment history: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/my-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getMyPaymentHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "paidAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = authentication.getName();

            User currentUser = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            LocalDateTime parsedFromDate = parseDate(fromDate, false);
            LocalDateTime parsedToDate = parseDate(toDate, true);

            PageResponse<PaymentResponse> paymentsPageResponse = subscriptionPaymentService.getMyPaymentHistory(currentUser.getId(), pageable, parsedFromDate, parsedToDate);

            ApiResponse<PageResponse<PaymentResponse>> apiResponse = ApiResponse.<PageResponse<PaymentResponse>>builder()
                    .code(200)
                    .message("My payment history retrieved successfully")
                    .result(paymentsPageResponse)
                    .build();
            return ResponseEntity.ok(apiResponse);
        } catch (DateTimeParseException e) {
            System.err.println("Date parsing error: " + e.getMessage());
            ApiResponse<PageResponse<PaymentResponse>> apiResponse = ApiResponse.<PageResponse<PaymentResponse>>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message("Invalid date format. Please use ISO 8601 format (e.g., YYYY-MM-DDTHH:MM:SS) or YYYY-MM-DD.")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        } catch (AppException e) {
            System.err.println("Application Error retrieving user payment history: " + e.getMessage());
            ApiResponse<PageResponse<PaymentResponse>> apiResponse = ApiResponse.<PageResponse<PaymentResponse>>builder()
                    .code(e.getErrorCode().getErrorCode())
                    .message(e.getErrorCode().getErrorMessage())
                    .build();
            return ResponseEntity.status(e.getErrorCode().getErrorCode()).body(apiResponse);
        } catch (Exception e) {
            System.err.println("Internal Server Error retrieving user payment history: " + e.getMessage());
            e.printStackTrace();
            ApiResponse<PageResponse<PaymentResponse>> apiResponse = ApiResponse.<PageResponse<PaymentResponse>>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve my payment history: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }



    private LocalDateTime parseDate(String dateString, boolean isEndDate) throws DateTimeParseException {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            // Thử parse với định dạng đầy đủ (YYYY-MM-DDTHH:MM:SS)
            return LocalDateTime.parse(dateString);
        } catch (DateTimeParseException e) {

            if (isEndDate) {
                return LocalDateTime.parse(dateString + "T23:59:59");
            } else {
                return LocalDateTime.parse(dateString + "T00:00:00");
            }
        }
    }
}