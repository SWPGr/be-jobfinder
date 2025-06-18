package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Float amount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "paid_at", nullable = false, updatable = false)
    private LocalDateTime paidAt;

    // --- Mối quan hệ ---

    // Một Payment thuộc về một User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // user_id có thể NULL trong DB schema của bạn nếu free plan
    @JsonManagedReference("user-payments") // Đặt reference name khác nếu User có nhiều loại payments
    private User user;

    // Một Payment liên quan đến một Subscription (OneToOne)
    // Payment sở hữu khóa ngoại subscription_id
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id") // subscription_id có thể NULL trong DB
    @JsonManagedReference("subscription-payment")
    private Subscription subscription;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.paidAt = LocalDateTime.now();
    }
}