package com.example.jobfinder.dto.payment;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePremiumPaymentRequest {
    public Long planId; // ID của gói Premium muốn mua
    public String returnUrl;
    public String cancelUrl;
}
