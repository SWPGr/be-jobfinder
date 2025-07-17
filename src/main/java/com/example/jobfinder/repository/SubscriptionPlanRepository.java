package com.example.jobfinder.repository;

import com.example.jobfinder.model.Role;
import com.example.jobfinder.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    Optional<SubscriptionPlan> findBySubscriptionPlanName(String subscriptionPlanName);
    Optional<SubscriptionPlan> findByPrice(Float price);

    // Phương thức mới để tìm kiếm theo Role entity
    List<SubscriptionPlan> findByRole(Role role);

    // Phương thức tiện lợi để tìm kiếm theo Role ID
    List<SubscriptionPlan> findByRoleId(Long roleId);
}