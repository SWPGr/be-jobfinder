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

    private PayOS payOS; // [cite: 21, 25, 38, 49, 59, 68]

    @PostConstruct
    public void init() {
        payOS = new PayOS(clientId, apiKey, checksumKey); // [cite: 25, 38, 49, 59, 68]
        System.out.println("PayOS initialized successfully.");
    }

    /**
     * Tạo link thanh toán cho đơn hàng.
     * @param orderCode Mã đơn hàng duy nhất của bạn (kiểu long). [cite: 78]
     * @param amount Tổng số tiền thanh toán (kiểu int). [cite: 78]
     * @param description Mô tả cho thanh toán (nội dung chuyển khoản). [cite: 78]
     * @param items Danh sách các sản phẩm trong đơn hàng. [cite: 78]
     * @param returnUrl Đường dẫn chuyển tiếp khi thanh toán thành công. [cite: 78]
     * @param cancelUrl Đường dẫn chuyển tiếp khi người dùng hủy đơn hàng. [cite: 78]
     * @return CheckoutResponseData chứa thông tin thanh toán, bao gồm checkoutUrl. [cite: 27, 32, 95]
     * @throws Exception nếu có lỗi trong quá trình tạo link.
     */
    public CheckoutResponseData createPaymentLink(
            long orderCode,
            int amount,
            String description,
            java.util.List<ItemData> items,
            String returnUrl,
            String cancelUrl) throws Exception {

        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode) // [cite: 78]
                .amount(amount) // [cite: 78]
                .description(description) // [cite: 78]
                .items(items) // [cite: 78]
                .returnUrl(returnUrl) // [cite: 78, 41]
                .cancelUrl(cancelUrl) // [cite: 78, 40]
                .build();

        // Sử dụng phương thức createPaymentLink từ đối tượng payOS. [cite: 27, 43]
        return payOS.createPaymentLink(paymentData);
    }

    /**
     * Lấy thông tin thanh toán của đơn hàng đã tạo link. [cite: 27, 44]
     * @param orderCode Mã đơn hàng. [cite: 45]
     * @return PaymentLinkData chứa thông tin chi tiết về link thanh toán. [cite: 27, 45, 100]
     * @throws Exception nếu có lỗi.
     */
    public PaymentLinkData getPaymentLinkInformation(long orderCode) throws Exception {
        return payOS.getPaymentLinkInformation(orderCode); // [cite: 27, 51]
    }

    /**
     * Hủy link thanh toán của đơn hàng. [cite: 30, 52]
     * @param orderCode Mã đơn hàng. [cite: 53]
     * @param cancellationReason Lý do hủy (tùy chọn). [cite: 53]
     * @return PaymentLinkData của link bị hủy. [cite: 30, 54]
     * @throws Exception nếu có lỗi.
     */
    public PaymentLinkData cancelPaymentLink(long orderCode, String cancellationReason) throws Exception {
        return payOS.cancelPaymentLink(orderCode, cancellationReason); // [cite: 61]
    }

    /**
     * Xác thực và thêm/cập nhật URL Webhook cho kênh thanh toán. [cite: 30, 63, 64]
     * @param webhookUrl URL Webhook của bạn. [cite: 65]
     * @return String chứa URL Webhook đã xác thực. [cite: 30, 65]
     * @throws Exception nếu có lỗi.
     */
    public String confirmWebhook(String webhookUrl) throws Exception {
        return payOS.confirmWebhook(webhookUrl); // [cite: 70]
    }


    public WebhookData verifyPaymentWebhookData(Webhook webhookBody) throws Exception {
        return payOS.verifyPaymentWebhookData(webhookBody); // [cite: 30] // Lưu ý: tài liệu chỉ ghi 'Webhook' không phải 'WebhookData'
    }
}