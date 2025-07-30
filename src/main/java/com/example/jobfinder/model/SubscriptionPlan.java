package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscription_plan_name", unique = true, length = 100)
    private String subscriptionPlanName;

    private Float price;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "max_jobs_post")
    private Integer maxJobsPost;

    @Column(name = "max_applications")
    private Integer maxApplicationsView;

    @Column(name = "highlight_jobs")
    private Boolean highlightJobs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // Một SubscriptionPlan có thể có nhiều Subscription
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("plan-subscriptions")
    private Set<Subscription> subscriptions = new HashSet<>();

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}