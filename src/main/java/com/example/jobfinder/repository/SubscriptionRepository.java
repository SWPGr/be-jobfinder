package com.example.jobfinder.repository;

import com.example.jobfinder.model.Subscription;
import com.example.jobfinder.model.SubscriptionPlan;
import com.example.jobfinder.model.User;
import com.example.jobfinder.util.QueryConstants;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUser_Email(String userEmail);
    Optional<Subscription> findByUserId(Long userId);

    @EntityGraph(attributePaths = "subscriptionPlan")
    List<Subscription> findByUserAndStartDateBeforeAndEndDateAfter(User user, LocalDateTime now1, LocalDateTime now2);
    List<Subscription> findByEndDateBeforeAndIsActiveTrueAndPlanNotIn(LocalDateTime endDate, Collection<SubscriptionPlan> plan);
    @Query(QueryConstants.FIND_SUBSCRIPTIONS_BY_CRITERIA) // Sử dụng hằng số
    List<Subscription> findSubscriptionsByCriteria(@Param("userEmail") String userEmail,
                                                   @Param("planName") String planName,
                                                   @Param("isActive") Boolean isActive);
}