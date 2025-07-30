// Trong SubscriptionSchedulerService.java
package com.example.jobfinder.service;

import com.example.jobfinder.model.Subscription;
import com.example.jobfinder.model.SubscriptionPlan;
import com.example.jobfinder.repository.SubscriptionRepository;
import com.example.jobfinder.repository.SubscriptionPlanRepository;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
@ConditionalOnProperty(
        value = "app.scheduler.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class SubscriptionSchedulerService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    private SubscriptionPlan basicSeekerPlan;
    private SubscriptionPlan basicEmployerPlan;

    @jakarta.annotation.PostConstruct
    public void init() {
        // Tìm gói Basic cho Job Seeker (role_id = 1)
        basicSeekerPlan = subscriptionPlanRepository.findBySubscriptionPlanNameAndRoleId("Basic Plan", 1L)
                .orElseThrow(() -> {
                    log.error("CRITICAL: Default 'Basic Plan' for Job Seeker (role_id=1) not found in database!");
                    return new AppException(ErrorCode.PLAN_NOT_FOUND);
                });
        if (basicSeekerPlan.getPrice() != 0) {
            log.error("CRITICAL: 'Basic Plan' for Job Seeker has price {}. It must be 0.", basicSeekerPlan.getPrice());
            throw new AppException(ErrorCode.INVALID_PLAN_CONFIGURATION);
        }
        log.info("Successfully loaded 'Basic Plan' for Job Seeker (ID: {}) for scheduler.", basicSeekerPlan.getId());

        // Tìm gói Basic cho Employer (role_id = 2)
        basicEmployerPlan = subscriptionPlanRepository.findBySubscriptionPlanNameAndRoleId("Basic Plan", 2L)
                .orElseThrow(() -> {
                    log.error("CRITICAL: Default 'Basic Plan' for Employer (role_id=2) not found in database!");
                    return new AppException(ErrorCode.PLAN_NOT_FOUND);
                });
        if (basicEmployerPlan.getPrice() != 0) {
            log.error("CRITICAL: 'Basic Plan' for Employer has price {}. It must be 0.", basicEmployerPlan.getPrice());
            throw new AppException(ErrorCode.INVALID_PLAN_CONFIGURATION);
        }
        log.info("Successfully loaded 'Basic Plan' for Employer (ID: {}) for scheduler.", basicEmployerPlan.getId());
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void downgradeExpiredPremiumSubscriptions() {
        List<SubscriptionPlan> allBasicPlans = subscriptionPlanRepository.findBySubscriptionPlanName("Basic Plan");

        if (allBasicPlans.isEmpty()) {
            return;
        }

        List<Subscription> expiredPremiumSubscriptions = subscriptionRepository
                .findByEndDateBeforeAndIsActiveTrueAndPlanNotIn(LocalDateTime.now(), allBasicPlans);

        if (expiredPremiumSubscriptions.isEmpty()) {
            return;
        }

        for (Subscription subscription : expiredPremiumSubscriptions) {
            try {
                SubscriptionPlan targetBasicPlan = null;
                if (subscription.getUser().getRole().getId().equals(1L)) { // Job Seeker
                    targetBasicPlan = basicSeekerPlan;
                } else if (subscription.getUser().getRole().getId().equals(2L)) { // Employer
                    targetBasicPlan = basicEmployerPlan;
                }

                if (targetBasicPlan == null) {
                    log.warn("Could not find a target Basic Plan for user {} with role ID {}. Skipping downgrade.",
                            subscription.getUser().getEmail(), subscription.getUser().getRole().getId());
                    continue;
                }

                if (!subscription.getPlan().equals(targetBasicPlan)) {
                    log.info("Downgrading user {} (current plan: {}) to BASIC plan for role {}.",
                            subscription.getUser().getEmail(), subscription.getPlan().getSubscriptionPlanName(),
                            subscription.getUser().getRole().getName());
                    subscription.setPlan(targetBasicPlan);
                    subscription.setStartDate(LocalDateTime.now());
                    subscription.setEndDate(LocalDateTime.now().plusYears(100));
                    subscription.setIsActive(true);
                    subscription.getUser().setIsPremium(false);

                    subscriptionRepository.save(subscription);
                    log.info("Successfully downgraded user {} to BASIC plan for role {}.",
                            subscription.getUser().getEmail(), subscription.getUser().getRole().getName());
                } else {
                    log.info("User {} is already on their respective Basic Plan. Skipping.", subscription.getUser().getEmail());
                }
            } catch (Exception e) {
                log.error("Error downgrading subscription for user {}: {}",
                        subscription.getUser() != null ? subscription.getUser().getEmail() : "N/A", e.getMessage(), e);
            }
        }
        log.info("Finished scheduled task: Downgrade of expired premium subscriptions completed.");
    }
}