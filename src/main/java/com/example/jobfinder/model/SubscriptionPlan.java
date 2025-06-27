package com.example.jobfinder.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "subscription_plans")
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

    @Column(name = "max_applications_view")
    private Integer maxApplicationsView;

    @Column(name = "highlight_jobs")
    private Boolean highlightJobs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Một SubscriptionPlan có thể có nhiều Subscription
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonBackReference("plan-subscriptions")
    private Set<Subscription> subscriptions = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubscriptionPlanName() {
        return subscriptionPlanName;
    }

    public void setSubscriptionPlanName(String subscriptionPlanName) {
        this.subscriptionPlanName = subscriptionPlanName;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public Integer getMaxJobsPost() {
        return maxJobsPost;
    }

    public void setMaxJobsPost(Integer maxJobsPost) {
        this.maxJobsPost = maxJobsPost;
    }

    public Integer getMaxApplicationsView() {
        return maxApplicationsView;
    }

    public void setMaxApplicationsView(Integer maxApplicationsView) {
        this.maxApplicationsView = maxApplicationsView;
    }

    public Boolean getHighlightJobs() {
        return highlightJobs;
    }

    public void setHighlightJobs(Boolean highlightJobs) {
        this.highlightJobs = highlightJobs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}