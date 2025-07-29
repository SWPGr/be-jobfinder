package com.example.jobfinder.repository;

import com.example.jobfinder.model.Payment;
import com.example.jobfinder.model.User;
import com.example.jobfinder.model.Subscription;
import com.example.jobfinder.util.QueryConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    Optional<Payment> findByPayosOrderCode(Long payosOrderCode);
    List<Payment> findByPaidAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    @EntityGraph(attributePaths = {"user", "intendedPlan"})
    Page<Payment> findByUserIdAndPaidAtBetween(Long userId, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "intendedPlan"})
    Page<Payment> findByUserIdAndPaidAtAfter(Long userId, LocalDateTime fromDate, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "intendedPlan"})
    Page<Payment> findByUserIdAndPaidAtBefore(Long userId, LocalDateTime toDate, Pageable pageable);

    Page<Payment> findByUserId(Long userId, Pageable pageable);

    @Query(QueryConstants.FIND_PAYMENTS_BY_CRITERIA)
    List<Payment> findPaymentsByCriteria(@Param("userEmail") String userEmail,
                                         @Param("paymentMethod") String paymentMethod,
                                         @Param("minAmount") Float minAmount,
                                         @Param("maxAmount") Float maxAmount);
}