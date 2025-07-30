// src/main/java/com/example/jobfinder/mapper/PaymentMapper.java
package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.payment.PaymentResponse;
import com.example.jobfinder.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring") // componentModel = "spring" để Spring tự động inject mapper
public interface PaymentMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "subscriptionId", source = "subscription.id")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    @Mapping(target = "paidAt", source = "paidAt")
    @Mapping(target = "intendedPlanId", source = "intendedPlan.id")
    @Mapping(target = "intendedPlanName", source = "intendedPlan.subscriptionPlanName")
    @Mapping(target = "payosOrderCode", source = "payosOrderCode")
    @Mapping(target = "payosPaymentLinkId", source = "payosPaymentLinkId")
    @Mapping(target = "payosTransactionRef", source = "payosTransactionRef")
    @Mapping(target = "payosStatus", source = "payosStatus")
    PaymentResponse toPaymentResponse(Payment payment);
}