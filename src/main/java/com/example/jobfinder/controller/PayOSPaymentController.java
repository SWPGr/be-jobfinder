package com.example.jobfinder.controller; // Thay đổi package cho phù hợp

import com.example.jobfinder.dto.ApiResponse; // Giả sử bạn có ApiResponse DTO
import com.example.jobfinder.service.PayOSService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.type.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.Instant; // Dùng để tạo orderCode duy nhất

@RestController
@RequestMapping("/api/payos")
public class PayOSPaymentController {

    private final PayOSService payOSService;

    public PayOSPaymentController(PayOSService payOSService) {
        this.payOSService = payOSService;
    }

    // DTO cho request từ frontend để tạo Payment Link
    public static class CreatePaymentLinkRequest {
        public String productName;
        public int amount; // Số tiền tính bằng đơn vị nhỏ nhất (VND)
        public String description;
        public String returnUrl; // URL để PayOS chuyển hướng về khi thanh toán thành công
        public String cancelUrl; // URL để PayOS chuyển hướng về khi người dùng hủy
        public List<ItemData> items; // Có thể bỏ qua nếu sản phẩm là cố định và chỉ dùng productName
    }

    /**
     * Endpoint để tạo link thanh toán PayOS.
     * Người dùng sẽ được chuyển hướng đến checkoutUrl trả về.
     */
    @PostMapping("/create-payment-link")
    public ResponseEntity<ApiResponse<Map<String, String>>> createPaymentLink(@RequestBody CreatePaymentLinkRequest request) {
        try {
            // Tạo một orderCode duy nhất. Bạn nên sử dụng một ID đơn hàng từ DB của bạn.
            // Ví dụ này sử dụng timestamp làm orderCode đơn giản.
            long orderCode = Instant.now().getEpochSecond(); // Hoặc Long.parseLong(UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10), 16);
            // Quan trọng: orderCode phải là duy nhất và có kiểu long.

            // Nếu request không có items, tạo một ItemData mặc định
            List<ItemData> itemsToUse = request.items;
            if (itemsToUse == null || itemsToUse.isEmpty()) {
                // Tạo một item mặc định nếu không có danh sách items được gửi lên
                itemsToUse = Collections.singletonList(
                        ItemData.builder()
                                .name(request.productName != null ? request.productName : "Dịch vụ")
                                .quantity(1)
                                .price(request.amount)
                                .build()
                );
            }

            // Gọi service để tạo link thanh toán
            CheckoutResponseData checkoutData = payOSService.createPaymentLink(
                    orderCode,
                    request.amount,
                    request.description,
                    itemsToUse,
                    request.returnUrl,
                    request.cancelUrl
            );

            Map<String, String> responseResult = new HashMap<>();
            responseResult.put("checkoutUrl", checkoutData.getCheckoutUrl()); // [cite: 97]
            responseResult.put("orderCode", String.valueOf(checkoutData.getOrderCode())); // [cite: 97]
            responseResult.put("paymentLinkId", checkoutData.getPaymentLinkId()); // [cite: 97]

            ApiResponse<Map<String, String>> apiResponse = ApiResponse.<Map<String, String>>builder()
                    .code(200)
                    .message("Payment link created successfully")
                    .result(responseResult)
                    .build();

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            System.err.println("Error creating PayOS payment link: " + e.getMessage());
            ApiResponse<Map<String, String>> apiResponse = ApiResponse.<Map<String, String>>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to create payment link: " + e.getMessage())
                    .result(Collections.emptyMap())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Endpoint để nhận thông báo Webhook từ PayOS.
     * PayOS sẽ gửi dữ liệu Webhook đến endpoint này khi có sự kiện thanh toán.
     * Quan trọng: Luôn xác minh chữ ký của webhook để đảm bảo tính xác thực.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handlePayOSWebhook(@RequestBody Webhook webhookBody) { // [cite: 108]
        try {
            // Xác minh dữ liệu webhook
            WebhookData verifiedData = payOSService.verifyPaymentWebhookData(webhookBody); // [cite: 30, 71]

            // Xử lý logic dựa trên trạng thái thanh toán
            // verifiedData.getCode() và verifiedData.getDesc() sẽ chứa thông tin trạng thái. [cite: 120]
            // verifiedData.getOrderCode() là mã đơn hàng của bạn. [cite: 120]

            if (verifiedData.getCode().equals("00")) { // Giả sử "00" là mã thành công từ PayOS
                System.out.println("Webhook received: Payment for order " + verifiedData.getOrderCode() + " succeeded.");
                // Cập nhật trạng thái đơn hàng trong DB của bạn (ví dụ: đánh dấu là đã thanh toán)
                // Kích hoạt dịch vụ/gói cho người dùng
            } else {
                System.out.println("Webhook received: Payment for order " + verifiedData.getOrderCode() + " failed/pending with code: " + verifiedData.getCode() + " - " + verifiedData.getDesc());
                // Xử lý các trạng thái khác (thất bại, chờ xử lý,...)
            }

            // Trả về HTTP 200 OK để PayOS biết bạn đã nhận được webhook thành công
            return ResponseEntity.ok("Webhook received and processed successfully");

        } catch (Exception e) {
            System.err.println("Error processing PayOS webhook: " + e.getMessage());
            // Trả về HTTP 500 Internal Server Error nếu có lỗi xử lý
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook: " + e.getMessage());
        }
    }

    // Endpoint ví dụ để lấy thông tin link thanh toán (có thể không cần dùng nếu dùng webhook)
    @GetMapping("/payment-info/{orderCode}")
    public ResponseEntity<ApiResponse<PaymentLinkData>> getPaymentInfo(@PathVariable long orderCode) {
        try {
            PaymentLinkData paymentLinkData = payOSService.getPaymentLinkInformation(orderCode); // [cite: 27, 44]
            ApiResponse<PaymentLinkData> apiResponse = ApiResponse.<PaymentLinkData>builder()
                    .code(200)
                    .message("Payment link information retrieved successfully")
                    .result(paymentLinkData)
                    .build();
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            System.err.println("Error retrieving payment info: " + e.getMessage());
            ApiResponse<PaymentLinkData> apiResponse = ApiResponse.<PaymentLinkData>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Failed to retrieve payment info: " + e.getMessage())
                    .result(null)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
}