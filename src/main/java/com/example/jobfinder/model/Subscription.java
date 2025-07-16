package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // --- Mối quan hệ ---

    // Một Subscription thuộc về một User
    // user_id UNIQUE trong DB, nên có thể cân nhắc là OneToOne, nhưng ở đây vẫn dùng ManyToOne
    // vì User có thể có nhiều subscriptions trong lịch sử (isActive = FALSE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true) // Cần đảm bảo rằng chỉ có một subscription active cho mỗi user.
    @JsonManagedReference("user-subscriptions")
    private User user;

    // Một Subscription thuộc về một SubscriptionPlan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    @JsonManagedReference("plan-subscriptions")
    private SubscriptionPlan plan;

    // Một Subscription có một Payment
    // Mối quan hệ OneToOne với Payment, Payment sở hữu khóa ngoại subscription_id
    @OneToOne(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference("subscription-payment")
    private Payment payment;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}