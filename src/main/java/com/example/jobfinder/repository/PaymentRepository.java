package com.example.jobfinder.repository;

import com.example.jobfinder.model.Payment;
import com.example.jobfinder.model.User;
import com.example.jobfinder.model.Subscription;
import com.example.jobfinder.util.QueryConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Tìm kiếm các khoản thanh toán của một user
    List<Payment> findByUser(User user);

    // Tìm kiếm khoản thanh toán cho một subscription cụ thể
    Optional<Payment> findBySubscription(Subscription subscription);

    @Query(QueryConstants.FIND_PAYMENTS_BY_CRITERIA)
    List<Payment> findPaymentsByCriteria(@Param("userEmail") String userEmail,
                                         @Param("paymentMethod") String paymentMethod,
                                         @Param("minAmount") Float minAmount,
                                         @Param("maxAmount") Float maxAmount);
}