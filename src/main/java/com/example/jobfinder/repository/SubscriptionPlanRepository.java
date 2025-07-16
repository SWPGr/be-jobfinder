package com.example.jobfinder.repository;

import com.example.jobfinder.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    Optional<SubscriptionPlan> findBySubscriptionPlanName(String subscriptionPlanName);
    Optional<SubscriptionPlan> findByPrice(Float price);
}