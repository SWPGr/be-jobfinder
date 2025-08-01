package com.example.jobfinder.controller;

import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.dto.payment.CreatePremiumPaymentRequest;
import com.example.jobfinder.dto.payment.FrontendPaymentSuccessRequest;
import com.example.jobfinder.service.SubscriptionPaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.payos.type.CheckoutResponseData;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/payos")
public class PayOSPaymentController {

    private final SubscriptionPaymentService subscriptionPaymentService;
    private final com.example.jobfinder.repository.UserRepository userRepository; // Cần để lấy User object

    public PayOSPaymentController(SubscriptionPaymentService subscriptionPaymentService, com.example.jobfinder.repository.UserRepository userRepository) {
        this.subscriptionPaymentService = subscriptionPaymentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/create-premium-payment-link")
    public ResponseEntity<ApiResponse<Map<String, String>>> createPremiumPaymentLink(@RequestBody CreatePremiumPaymentRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = authentication.getName();
            com.example.jobfinder.model.User currentUser = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new com.example.jobfinder.exception.AppException(com.example.jobfinder.exception.ErrorCode.USER_NOT_FOUND));

            CheckoutResponseData checkoutData = subscriptionPaymentService.createPremiumSubscriptionPaymentLink(
                    currentUser.getId(),
                    request.planId,
                    request.returnUrl,
                    request.cancelUrl
            );

            Map<String, String> responseResult = new HashMap<>();
            responseResult.put("checkoutUrl", checkoutData.getCheckoutUrl());
            responseResult.put("orderCode", String.valueOf(checkoutData.getOrderCode()));
            responseResult.put("paymentLinkId", checkoutData.getPaymentLinkId());

            ApiResponse<Map<String, String>> apiResponse = ApiResponse.<Map<String, String>>builder()
                    .code(200)
                    .message("Premium payment link created successfully")
                    .result(responseResult)
                    .build();

            return ResponseEntity.ok(apiResponse);

        } catch (com.example.jobfinder.exception.AppException e) {
            System.err.println("Application Error creating PayOS payment link: " + e.getMessage());
            ApiResponse<Map<String, String>> apiResponse = ApiResponse.<Map<String, String>>builder()
                    .code(e.getErrorCode().getErrorCode())
                    .message(e.getErrorCode().getErrorMessage())
                    .result(Collections.emptyMap())
                    .build();
            return ResponseEntity.status(e.getErrorCode().getErrorCode()).body(apiResponse);
        } catch (Exception e) {
            System.err.println("Internal Server Error creating PayOS payment link: " + e.getMessage());
            ApiResponse<Map<String, String>> apiResponse = ApiResponse.<Map<String, String>>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to create premium payment link: " + e.getMessage())
                    .result(Collections.emptyMap())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    //Dùng tạm để thay đổi trạng thái trong database nhưng sau này deploy xong thì dùng hàm dưới (webhook)
    @PostMapping("/process-frontend-success")
    public ResponseEntity<ApiResponse<String>> processFrontendPaymentSuccess(@RequestBody FrontendPaymentSuccessRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = authentication.getName();
            com.example.jobfinder.model.User currentUser = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new com.example.jobfinder.exception.AppException(com.example.jobfinder.exception.ErrorCode.USER_NOT_FOUND));

            subscriptionPaymentService.processPaymentFromFrontendCallback(
                    currentUser.getId(),
                    request.getOrderCode(),
                    request.getPaymentLinkId()
            );

            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .code(200)
                    .message("Payment successfully processed and premium status updated.")
                    .result("OK")
                    .build();

            return ResponseEntity.ok(apiResponse);

        } catch (com.example.jobfinder.exception.AppException e) {
            System.err.println("Application Error processing frontend payment success: " + e.getMessage());
            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .code(e.getErrorCode().getErrorCode())
                    .message(e.getErrorCode().getErrorMessage())
                    .result("ERROR")
                    .build();
            return ResponseEntity.status(e.getErrorCode().getErrorCode()).body(apiResponse);
        } catch (Exception e) {
            System.err.println("Internal Server Error processing frontend payment success: " + e.getMessage());
            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to process payment from frontend: " + e.getMessage())
                    .result("ERROR")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }


    //Sẽ triển khai sau này chi chúng ta đã deploy lên server chính thức\
//
//    @PostMapping("/webhook")
//    public ResponseEntity<String> handlePayOSWebhook(@RequestBody Webhook webhookBody) {
//        try {
//            subscriptionPaymentService.handlePayOSWebhook(webhookBody);
//            return ResponseEntity.ok("Webhook received and processed successfully");
//        } catch (Exception e) {
//            System.err.println("Error processing PayOS webhook: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook: " + e.getMessage());
//        }
//    }
}