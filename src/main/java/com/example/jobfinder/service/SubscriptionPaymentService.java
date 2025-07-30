package com.example.jobfinder.service;

import com.example.jobfinder.dto.PageResponse;
import com.example.jobfinder.dto.payment.PaymentResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.PaymentMapper;
import com.example.jobfinder.model.*; // Import tất cả các model
import com.example.jobfinder.repository.*; // Import tất cả các repository
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.type.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.criteria.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SubscriptionPaymentService {

    PayOSService payOSService;
    SubscriptionPlanRepository subscriptionPlanRepository;
    SubscriptionRepository subscriptionRepository;
    PaymentRepository paymentRepository;
    UserRepository userRepository;
    PaymentMapper paymentMapper;

    @Transactional
    public CheckoutResponseData createPremiumSubscriptionPaymentLink(
            Long userId,
            Long planId,
            String returnUrl,
            String cancelUrl) throws Exception {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));

        if (plan.getPrice() <= 0) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_FOR_FREE_PLAN);
        }

        long orderCode = Long.parseLong(String.valueOf(userId) + String.valueOf(System.currentTimeMillis()).substring(6));

        String description = "Pay " + plan.getSubscriptionPlanName();
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }

        List<ItemData> items = Collections.singletonList(
                ItemData.builder()
                        .name(plan.getSubscriptionPlanName())
                        .quantity(1)
                        .price(plan.getPrice().intValue())
                        .build()
        );

        CheckoutResponseData checkoutData = payOSService.createPaymentLink(
                orderCode,
                plan.getPrice().intValue(),
                description,
                items,
                returnUrl,
                cancelUrl
        );

        Payment newPayment = Payment.builder()
                .user(user)
                .intendedPlan(plan)
                .subscription(null)
                .amount(plan.getPrice())
                .paymentMethod("PayOS")
                .payosOrderCode(orderCode)
                .payosPaymentLinkId(checkoutData.getPaymentLinkId())
                .payosStatus("PENDING")
                .paidAt(null)
                .build();
        paymentRepository.save(newPayment);

        return checkoutData;
    }

    @Transactional
    public void processPaymentFromFrontendCallback(Long userId, Long orderCode, String paymentLinkId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));


        Optional<Payment> initialPaymentOptional = paymentRepository.findByPayosOrderCode(orderCode);
        if (initialPaymentOptional.isEmpty()) {
            throw new AppException(ErrorCode.PAYMENT_PROCESSING_ERROR);
        }
        Payment paymentRecord = initialPaymentOptional.get();

        if (!paymentRecord.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.PAYMENT_PROCESSING_ERROR);
        }

        PaymentLinkData paymentLinkInfo = payOSService.getPaymentLinkInformation(orderCode);

        if (paymentLinkInfo == null || !paymentLinkInfo.getOrderCode().equals(orderCode)) {
            throw new AppException(ErrorCode.PAYMENT_INFO_NOT_FOUND_PAYOS);
        }

        if (!paymentLinkInfo.getStatus().equals("PAID")) {
            paymentRecord.setPayosStatus(paymentLinkInfo.getStatus());
            paymentRecord.setPayosStatus(paymentLinkInfo.getStatus());
            paymentRepository.save(paymentRecord);
            throw new AppException(ErrorCode.PAYMENT_FAILED_OR_PENDING);
        }

        if (paymentRecord.getPayosStatus() != null && paymentRecord.getPayosStatus().equals("PAID")) {
            return;
        }
        SubscriptionPlan plan = paymentRecord.getIntendedPlan();

        Optional<Subscription> existingActiveSubscription = subscriptionRepository.findByUserId(user.getId());
        Subscription subscription;
        LocalDateTime now = LocalDateTime.now();

        if (existingActiveSubscription.isPresent()) {
            subscription = existingActiveSubscription.get();
            subscription.setPlan(plan);
            subscription.setStartDate(now);
            subscription.setEndDate(now.plusDays(plan.getDurationDays()));
            subscription.setIsActive(true);
        } else {
            subscription = Subscription.builder()
                    .user(user)
                    .plan(plan)
                    .startDate(now)
                    .endDate(now.plusDays(plan.getDurationDays()))
                    .isActive(true)
                    .build();
        }
        subscriptionRepository.save(subscription);
        user.setIsPremium(true);
        userRepository.save(user);
        paymentRecord.setSubscription(subscription);
        paymentRecord.setAmount((float) paymentLinkInfo.getAmountPaid());
        paymentRecord.setPaidAt(now);
        paymentRecord.setPayosStatus("PAID");
        paymentRepository.save(paymentRecord);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getAllPaymentHistory(
            Pageable pageable,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String paymentStatus,
            String userEmail
    ) {
        Page<Payment> paymentsPage;

        // Xây dựng Specification động
        Specification<Payment> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("paidAt"), fromDate));
            }
            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("paidAt"), toDate));
            }

            if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("payosStatus"), paymentStatus));
            }

            if (userEmail != null && !userEmail.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("email")), "%" + userEmail.toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        paymentsPage = paymentRepository.findAll(spec, pageable);
        return buildPageResponse(paymentsPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getMyPaymentHistory(
            Long userId,
            Pageable pageable,
            LocalDateTime fromDate,
            LocalDateTime toDate) {
        Page<Payment> paymentsPage;
        if (fromDate != null && toDate != null) {
            paymentsPage = paymentRepository.findByUserIdAndPaidAtBetween(userId, fromDate, toDate, pageable);
        } else if (fromDate != null) {
            paymentsPage = paymentRepository.findByUserIdAndPaidAtAfter(userId, fromDate, pageable);
        } else if (toDate != null) {
            paymentsPage = paymentRepository.findByUserIdAndPaidAtBefore(userId, toDate, pageable);
        } else {
            paymentsPage = paymentRepository.findByUserId(userId, pageable);
        }
        return buildPageResponse(paymentsPage);
    }

    private PageResponse<PaymentResponse> buildPageResponse(Page<Payment> paymentsPage) {
        List<PaymentResponse> content = paymentsPage.getContent().stream()
                .map(paymentMapper::toPaymentResponse)
                .collect(Collectors.toList());

        return PageResponse.<PaymentResponse>builder()
                .content(content) // Nếu paymentsPage.getContent() rỗng, thì content cũng rỗng
                .pageNumber(paymentsPage.getNumber())
                .pageSize(paymentsPage.getSize())
                .totalElements(paymentsPage.getTotalElements())
                .totalPages(paymentsPage.getTotalPages())
                .isLast(paymentsPage.isLast())
                .isFirst(paymentsPage.isFirst())
                .build();
    }

}