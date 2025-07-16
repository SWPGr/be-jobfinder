package com.example.jobfinder.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long userId;        // ID của người dùng
    private String userEmail;   // Email của người dùng (từ đối tượng User)
    private Long subscriptionId; // ID của Subscription liên quan (nếu có)
    private Float amount;
    private String paymentMethod;
    private LocalDateTime paidAt;
    private Long intendedPlanId;    // ID của gói đăng ký dự định
    private String intendedPlanName; // Tên của gói đăng ký dự định (từ đối tượng SubscriptionPlan)
    private Long payosOrderCode;
    private String payosPaymentLinkId;
    private String payosTransactionRef;
    private String payosStatus;
}