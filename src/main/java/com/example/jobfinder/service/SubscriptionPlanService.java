
package com.example.jobfinder.service;
import com.example.jobfinder.dto.subscriptionPlan.SubscriptionPlanResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.SubscriptionPlanMapper;
import com.example.jobfinder.model.Role;
import com.example.jobfinder.model.SubscriptionPlan;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.SubscriptionPlanRepository;
import com.example.jobfinder.repository.SubscriptionRepository;
import com.example.jobfinder.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubscriptionPlanService {

    SubscriptionPlanRepository subscriptionPlanRepository;
    SubscriptionPlanMapper subscriptionPlanMapper;
    UserRepository userRepository;
    SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getAllSubscriptionPlans() {
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAll();
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = null;
        Set<Long> activePlanIds = null;

        if (currentUserEmail != null && !currentUserEmail.equals("anonymousUser")) {
            currentUser = userRepository.findByEmail(currentUserEmail).orElse(null);
        }

        if (currentUser != null) {
            LocalDateTime now = LocalDateTime.now();
            activePlanIds = subscriptionRepository
                    .findByUserAndStartDateBeforeAndEndDateAfter(currentUser, now, now)
                    .stream()
                    .map(sub -> sub.getPlan().getId())
                    .collect(Collectors.toSet());
        } else {
            activePlanIds = Set.of();
        }
        Set<Long> finalActivePlanIds = activePlanIds;
        return plans.stream()
                .map(plan -> {
                    SubscriptionPlanResponse response = subscriptionPlanMapper.toSubscriptionPlanResponse(plan);
                    response.setIsActive(finalActivePlanIds.contains(plan.getId()));
                    return response;
                })
                .collect(Collectors.toList());
    }



    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getSubscriptionPlansByCurrentUserRole() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Role currentUserRole = currentUser.getRole();
        Long currentRoleId = currentUserRole.getId();

        List<SubscriptionPlan> plans = subscriptionPlanRepository.findByRole(currentUserRole);

        SubscriptionPlan activeSubscriptionPlan = null;
        if (Boolean.TRUE.equals(currentUser.getIsPremium())) {
            activeSubscriptionPlan = plans.stream()
                    .filter(plan -> plan.getPrice() != null && plan.getPrice() > 0)
                    .findFirst()
                    .orElse(null);
        } else {
            activeSubscriptionPlan = plans.stream()
                    .filter(plan -> plan.getPrice() != null && plan.getPrice() == 0)
                    .findFirst()
                    .orElse(null);
        }

        Long activePlanId = (activeSubscriptionPlan != null) ? activeSubscriptionPlan.getId() : null;

        return plans.stream()
                .map(plan -> {
                    SubscriptionPlanResponse response = subscriptionPlanMapper.toSubscriptionPlanResponse(plan);
                    // Gán isActive dựa trên ID của gói đang hoạt động
                    response.setIsActive(activePlanId != null && activePlanId.equals(plan.getId()));
                    return response;
                })
                .collect(Collectors.toList());
    }
}