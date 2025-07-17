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
import com.example.jobfinder.repository.RoleRepository;
import com.example.jobfinder.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final RoleRepository roleRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;
    /**
     * Tạo một gói đăng ký mới.
     * @param request DTO chứa thông tin gói cần tạo.
     * @return SubscriptionPlan đã được tạo.
     * @throws AppException nếu tên gói đã tồn tại.
     */
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

    public List<SubscriptionPlan> getAllSubscriptionPlans() {
        return subscriptionPlanRepository.findAll();
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

    /**
     * Xóa một gói đăng ký theo ID.
     * @param id ID của gói cần xóa.
     * @throws AppException nếu không tìm thấy gói.
     */
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

        List<SubscriptionPlan> plans = subscriptionPlanRepository.findByRole(targetRole); // Sử dụng findByRole với entity Role

        if (plans.isEmpty()) {

            // Bạn có thể chọn ném ngoại lệ hoặc trả về danh sách rỗng tùy theo logic nghiệp vụ
            // throw new AppException(ErrorCode.NO_SUBSCRIPTION_PLANS_FOUND_FOR_ROLE);
        }

        return subscriptionPlanMapper.toSubscriptionPlanResponseList(plans);
    }
}