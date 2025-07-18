// src/main/java/com/example/jobfinder/service/SubscriptionPlanService.java
package com.example.jobfinder.service;

import com.example.jobfinder.dto.SubscriptionPlan.SubscriptionPlanCreationRequest;
import com.example.jobfinder.dto.SubscriptionPlan.SubscriptionPlanResponse;
import com.example.jobfinder.dto.SubscriptionPlan.SubscriptionPlanUpdateRequest;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.SubscriptionPlanMapper;
import com.example.jobfinder.model.Role;
import com.example.jobfinder.model.SubscriptionPlan;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.RoleRepository;
import com.example.jobfinder.repository.SubscriptionPlanRepository;
import com.example.jobfinder.repository.SubscriptionRepository;
import com.example.jobfinder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final RoleRepository roleRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public SubscriptionPlan createSubscriptionPlan(SubscriptionPlanCreationRequest request) {
        // Kiểm tra trùng lặp tên gói
        if (subscriptionPlanRepository.findBySubscriptionPlanName(request.getSubscriptionPlanName()).isPresent()) {
            throw new AppException(ErrorCode.PLAN_NAME_DUPLICATED);
        }

        SubscriptionPlan newPlan = SubscriptionPlan.builder()
                .subscriptionPlanName(request.getSubscriptionPlanName())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .maxJobsPost(request.getMaxJobsPost())
                .maxApplicationsView(request.getMaxApplicationsView())
                .highlightJobs(request.getHighlightJobs())
                .build();
        return subscriptionPlanRepository.save(newPlan);
    }

    public Optional<SubscriptionPlan> getSubscriptionPlanById(Long id) {
        return subscriptionPlanRepository.findById(id);
    }

    @Transactional
    public SubscriptionPlan updateSubscriptionPlan(Long id, SubscriptionPlanUpdateRequest request) {
        SubscriptionPlan existingPlan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));

        // Cập nhật các trường chỉ khi chúng được cung cấp trong request (không phải null)
        Optional.ofNullable(request.getSubscriptionPlanName()).ifPresent(existingPlan::setSubscriptionPlanName);
        Optional.ofNullable(request.getPrice()).ifPresent(existingPlan::setPrice);
        Optional.ofNullable(request.getDurationDays()).ifPresent(existingPlan::setDurationDays);
        Optional.ofNullable(request.getMaxJobsPost()).ifPresent(existingPlan::setMaxJobsPost);
        Optional.ofNullable(request.getMaxApplicationsView()).ifPresent(existingPlan::setMaxApplicationsView);
        Optional.ofNullable(request.getHighlightJobs()).ifPresent(existingPlan::setHighlightJobs);

        return subscriptionPlanRepository.save(existingPlan);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getAllSubscriptionPlans() {
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAll();

        // Lấy thông tin người dùng hiện tại (nếu người dùng đã đăng nhập)
        // Nếu người dùng chưa đăng nhập, activePlanIds sẽ rỗng, isActive sẽ là false cho tất cả
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
            activePlanIds = Set.of(); // Nếu không có người dùng, không có gói nào active
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

    @Transactional
    public void deleteSubscriptionPlan(Long id) {
        if (!subscriptionPlanRepository.existsById(id)) {
            throw new AppException(ErrorCode.PLAN_NOT_FOUND);
        }
        subscriptionPlanRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getSubscriptionPlansByRoleId(Long roleId) {
        // Kiểm tra xem Role có tồn tại không
        Role targetRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        List<SubscriptionPlan> plans = subscriptionPlanRepository.findByRole(targetRole);

        // Lấy thông tin người dùng hiện tại để xác định gói nào đang hoạt động
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)); // User không tồn tại thì là lỗi

        // Lấy tất cả các Subscription đang hoạt động của người dùng
        // Một Subscription được coi là hoạt động nếu thời gian hiện tại nằm giữa startDate và endDate
        LocalDateTime now = LocalDateTime.now();
        Set<Long> activePlanIds = subscriptionRepository
                .findByUserAndStartDateBeforeAndEndDateAfter(currentUser, now, now)
                .stream()
                .map(sub -> sub.getPlan().getId()) // Lấy ID của SubscriptionPlan từ Subscription
                .collect(Collectors.toSet());

        // Chuyển đổi entity sang DTO và gán giá trị isActive
        return plans.stream()
                .map(plan -> {
                    SubscriptionPlanResponse response = subscriptionPlanMapper.toSubscriptionPlanResponse(plan);
                    response.setIsActive(activePlanIds.contains(plan.getId())); // Kiểm tra nếu gói này nằm trong số các gói đang hoạt động của user
                    return response;
                })
                .collect(Collectors.toList());
    }
}