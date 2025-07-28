package com.example.jobfinder.service; // Thay đổi package cho phù hợp với dự án của bạn

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import vn.payos.PayOS; // [cite: 24]
import vn.payos.type.CheckoutResponseData; // [cite: 35]
import vn.payos.type.PaymentData; // [cite: 36, 75]
import vn.payos.type.ItemData; // [cite: 37, 80]
import vn.payos.type.PaymentLinkData; // [cite: 48, 99]
import vn.payos.type.Webhook; // [cite: 108]
import vn.payos.type.WebhookData; // [cite: 118]


@Service
public class PayOSService {

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    private PayOS payOS;

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

    public PaymentLinkData cancelPaymentLink(long orderCode, String cancellationReason) throws Exception {
        return payOS.cancelPaymentLink(orderCode, cancellationReason);
    }

    public String confirmWebhook(String webhookUrl) throws Exception {
        return payOS.confirmWebhook(webhookUrl);
    }


    public WebhookData verifyPaymentWebhookData(Webhook webhookBody) throws Exception {
        return payOS.verifyPaymentWebhookData(webhookBody);
    }
}