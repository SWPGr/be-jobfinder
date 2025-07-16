package com.example.jobfinder.dto.SubscriptionPlan;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionPlanUpdateRequest {
    private String subscriptionPlanName;
    private Float price;
    private Integer durationDays;
    private Integer maxJobsPost;
    private Integer maxApplicationsView;
    private Boolean highlightJobs;
}