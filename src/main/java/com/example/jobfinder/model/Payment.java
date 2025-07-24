// src/main/java/com/example/jobfinder/model/Payment.java
package com.example.jobfinder.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY) // OneToOne vì mỗi payment thường cho một subscription cụ thể
    @JoinColumn(name = "subscription_id", unique = true) // Có thể unique hoặc không tùy logic của bạn
    private Subscription subscription; // Liên kết với Subscription mà payment này kích hoạt

    private Float amount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // Ví dụ: "PayOS", "Stripe", "PayPal"

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intended_plan_id", nullable = false) // Gói mà user muốn mua
    private SubscriptionPlan intendedPlan;

    @Column(name = "payos_order_code", unique = true) // Mã đơn hàng từ PayOS
    private Long payosOrderCode;

    @Column(name = "payos_payment_link_id", unique = true) // ID link thanh toán từ PayOS
    private String payosPaymentLinkId;

    @Column(name = "payos_transaction_ref") // Mã tham chiếu giao dịch từ PayOS webhook
    private String payosTransactionRef;

    @Column(name = "payos_status") // Trạng thái cuối cùng từ PayOS webhook
    private String payosStatus;

    @PrePersist
    protected void onCreate() {
        if (paidAt == null) {
            paidAt = LocalDateTime.now();
        }
    }
}