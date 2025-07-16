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
// [cite: 78, 41]
        PaymentData paymentData = builder
                .build();


        return payOS.createPaymentLink(paymentData);
    }

    /**
     * Lấy thông tin thanh toán của đơn hàng đã tạo link. [cite: 27, 44]
     * @param orderCode Mã đơn hàng. [cite: 45]
     * @return PaymentLinkData chứa thông tin chi tiết về link thanh toán. [cite: 27, 45, 100]
     * @throws Exception nếu có lỗi.
     */
    public PaymentLinkData getPaymentLinkInformation(long orderCode) throws Exception {
        return payOS.getPaymentLinkInformation(orderCode);
    }

    /**
     * Hủy link thanh toán của đơn hàng. [cite: 30, 52]
     * @param orderCode Mã đơn hàng. [cite: 53]
     * @param cancellationReason Lý do hủy (tùy chọn). [cite: 53]
     * @return PaymentLinkData của link bị hủy. [cite: 30, 54]
     * @throws Exception nếu có lỗi.
     */
    public PaymentLinkData cancelPaymentLink(long orderCode, String cancellationReason) throws Exception {
        return payOS.cancelPaymentLink(orderCode, cancellationReason);
    }

    /**
     * Xác thực và thêm/cập nhật URL Webhook cho kênh thanh toán. [cite: 30, 63, 64]
     * @param webhookUrl URL Webhook của bạn. [cite: 65]
     * @return String chứa URL Webhook đã xác thực. [cite: 30, 65]
     * @throws Exception nếu có lỗi.
     */
    public String confirmWebhook(String webhookUrl) throws Exception {
        return payOS.confirmWebhook(webhookUrl);
    }


    public WebhookData verifyPaymentWebhookData(Webhook webhookBody) throws Exception {
        return payOS.verifyPaymentWebhookData(webhookBody); // [cite: 30] // Lưu ý: tài liệu chỉ ghi 'Webhook' không phải 'WebhookData'
    }
}