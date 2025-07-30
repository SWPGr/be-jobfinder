package com.example.jobfinder.service; // Thay đổi package cho phù hợp với dự án của bạn

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import vn.payos.PayOS; // [cite: 24]
import vn.payos.type.CheckoutResponseData; // [cite: 35]
import vn.payos.type.PaymentData; // [cite: 36, 75]
import vn.payos.type.ItemData; // [cite: 37, 80]
import vn.payos.type.PaymentLinkData; // [cite: 48, 99]



@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayOSService {

    @Value("${payos.client-id}")
    String clientId;

    @Value("${payos.api-key}")
    String apiKey;

    @Value("${payos.checksum-key}")
    String checksumKey;

    PayOS payOS;

    @PostConstruct
    public void init() {
        payOS = new PayOS(clientId, apiKey, checksumKey);
        System.out.println("PayOS initialized successfully.");
    }

    public CheckoutResponseData createPaymentLink(
            long orderCode,
            int amount,
            String description,
            java.util.List<ItemData> items,
            String returnUrl,
            String cancelUrl) throws Exception {

        PaymentData.PaymentDataBuilder builder = PaymentData.builder();
        builder.orderCode(orderCode);
        builder.amount(amount);
        builder.description(description);
        builder.items(items);
        builder.returnUrl(returnUrl);
        builder.cancelUrl(cancelUrl);
        PaymentData paymentData = builder
                .build();


        return payOS.createPaymentLink(paymentData);
    }

    public PaymentLinkData getPaymentLinkInformation(long orderCode) throws Exception {
        return payOS.getPaymentLinkInformation(orderCode);
    }
}