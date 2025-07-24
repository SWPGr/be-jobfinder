package com.example.jobfinder.dto.SubscriptionPlan;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class SubscriptionPlanCreationRequest {
    @NonNull
    private String subscriptionPlanName;
    @NonNull
    private Float price;
    @NonNull
    private Integer durationDays;
    @NonNull
    private Integer maxJobsPost;
    @NonNull
    private Integer maxApplicationsView;
    @NonNull
    private Boolean highlightJobs;
}