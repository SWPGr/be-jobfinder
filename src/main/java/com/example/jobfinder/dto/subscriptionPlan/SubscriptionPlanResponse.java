package com.example.jobfinder.dto.subscriptionPlan;

import com.example.jobfinder.dto.simple.SimpleNameResponse; // THÊM IMPORT NÀY
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlanResponse {
    private Long id;
    private String subscriptionPlanName;
    private Float price;
    private Integer durationDays;
    private Integer maxJobsPost;
    private Integer maxApplicationsView;
    private Boolean highlightJobs;
    private LocalDateTime createdAt;
    private SimpleNameResponse role;

    private Boolean isActive;
}