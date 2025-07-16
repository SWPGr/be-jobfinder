// src/main/java/com/example/jobfinder/dto/payment/FrontendPaymentSuccessRequest.java
package com.example.jobfinder.dto.payment;

import lombok.Data;

@Data
public class FrontendPaymentSuccessRequest {
    private Long orderCode;
    private String paymentLinkId; // PayOS trả về cả orderCode và paymentLinkId trên URL success
}