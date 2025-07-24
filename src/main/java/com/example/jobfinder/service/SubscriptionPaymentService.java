package com.example.jobfinder.service;

import com.example.jobfinder.dto.payment.PaymentResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.*; // Import tất cả các model
import com.example.jobfinder.repository.*; // Import tất cả các repository
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.type.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionPaymentService {

    private final PayOSService payOSService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

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

        String description = "Thanh toan " + plan.getSubscriptionPlanName();
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

        // Gọi PayOSService để tạo link
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
        // 1. Lấy thông tin User hiện tại (đã được xác thực từ Controller)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. TÌM BẢN GHI PAYMENT BAN ĐẦU CỦA CHÚNG TA (TRONG DB CỦA BẠN)
        Optional<Payment> initialPaymentOptional = paymentRepository.findByPayosOrderCode(orderCode);
        if (initialPaymentOptional.isEmpty()) {
            System.err.println("Frontend Callback: Không tìm thấy bản ghi Payment ban đầu cho orderCode: " + orderCode + ". Có thể là lỗi đồng bộ.");
            throw new AppException(ErrorCode.PAYMENT_PROCESSING_ERROR);
        }
        Payment paymentRecord = initialPaymentOptional.get();

        // 3. (BẢO MẬT): Đảm bảo giao dịch này thuộc về người dùng đang đăng nhập
        if (!paymentRecord.getUser().getId().equals(userId)) {
            System.err.println("Frontend Callback: Người dùng " + user.getEmail() + " đang cố gắng xử lý giao dịch không phải của mình. orderCode: " + orderCode);
            throw new AppException(ErrorCode.PAYMENT_PROCESSING_ERROR);
        }

        // 4. GỌI LẠI PAYOS API ĐỂ XÁC MINH TRẠNG THÁI GIAO DỊCH THỰC SỰ
        PaymentLinkData paymentLinkInfo = payOSService.getPaymentLinkInformation(orderCode);

        if (paymentLinkInfo == null || !paymentLinkInfo.getOrderCode().equals(orderCode)) {
            System.err.println("Frontend Callback: Không thể lấy thông tin giao dịch từ PayOS hoặc orderCode không khớp. orderCode: " + orderCode);
            throw new AppException(ErrorCode.PAYMENT_INFO_NOT_FOUND_PAYOS);
        }

        // 5. Kiểm tra trạng thái giao dịch từ PayOS API Response
        if (!paymentLinkInfo.getStatus().equals("PAID")) {
            System.out.println("Frontend Callback: Giao dịch không thành công/chưa hoàn tất cho order " + orderCode + ". Trạng thái: " + paymentLinkInfo.getStatus() + ".");
            paymentRecord.setPayosStatus(paymentLinkInfo.getStatus());
            paymentRecord.setPayosStatus(paymentLinkInfo.getStatus());
            paymentRepository.save(paymentRecord);
            throw new AppException(ErrorCode.PAYMENT_FAILED_OR_PENDING);
        }

        // 6. Kiểm tra trùng lặp (Idempotency)
        if (paymentRecord.getPayosStatus() != null && paymentRecord.getPayosStatus().equals("PAID")) {
            System.out.println("Frontend Callback: Order " + orderCode + " đã được xử lý thành công trước đó (qua Frontend Callback). Bỏ qua trùng lặp.");
            return;
        }
        // 7. Lấy Plan từ bản ghi Payment đã lưu
        SubscriptionPlan plan = paymentRecord.getIntendedPlan();

        // 8. Cập nhật hoặc tạo Subscription mới cho người dùng
        Optional<Subscription> existingActiveSubscription = subscriptionRepository.findByUserId(user.getId());
        Subscription subscription;
        LocalDateTime now = LocalDateTime.now();

        if (existingActiveSubscription.isPresent()) {
            subscription = existingActiveSubscription.get();
            subscription.setPlan(plan);
            subscription.setStartDate(now);
            subscription.setEndDate(now.plusDays(plan.getDurationDays()));
            subscription.setIsActive(true);
            System.out.println("Frontend Callback: Cập nhật gói đăng ký cho user " + user.getEmail() + " thành " + plan.getSubscriptionPlanName());
        } else {
            subscription = Subscription.builder()
                    .user(user)
                    .plan(plan)
                    .startDate(now)
                    .endDate(now.plusDays(plan.getDurationDays()))
                    .isActive(true)
                    .build();
            System.out.println("Frontend Callback: Tạo gói đăng ký mới cho user " + user.getEmail() + ": " + plan.getSubscriptionPlanName());
        }
        subscriptionRepository.save(subscription);

        // 9. Cập nhật trạng thái isPremium của User
        user.setIsPremium(true);
        userRepository.save(user);

        // 10. Cập nhật bản ghi Payment với thông tin thành công và liên kết với Subscription
        paymentRecord.setSubscription(subscription);
        paymentRecord.setAmount((float) paymentLinkInfo.getAmountPaid());
        paymentRecord.setPaidAt(now);
        paymentRecord.setPayosStatus("PAID"); // Cập nhật trạng thái thành "PAID"
        paymentRepository.save(paymentRecord);

        System.out.println("Frontend Callback: Xử lý thành công giao dịch cho order " + orderCode + ". User " + user.getEmail() + " giờ là Premium.");
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        // Truy cập các mối quan hệ lazy-loaded trước khi ánh xạ
        // Điều này đảm bảo chúng được khởi tạo nếu session còn mở
        String userEmail = null;
        Long userId = null;
        if (payment.getUser() != null) {
            userId = payment.getUser().getId();
            userEmail = payment.getUser().getEmail();
        }

        String intendedPlanName = null;
        Long intendedPlanId = null;
        if (payment.getIntendedPlan() != null) {
            intendedPlanId = payment.getIntendedPlan().getId();
            intendedPlanName = payment.getIntendedPlan().getSubscriptionPlanName();
        }

        Long subscriptionId = null;
        if (payment.getSubscription() != null) {
            subscriptionId = payment.getSubscription().getId();
        }

        return PaymentResponse.builder()
                .id(payment.getId())
                .userId(userId)
                .userEmail(userEmail)
                .subscriptionId(subscriptionId)
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paidAt(payment.getPaidAt())
                .intendedPlanId(intendedPlanId)
                .intendedPlanName(intendedPlanName)
                .payosOrderCode(payment.getPayosOrderCode())
                .payosPaymentLinkId(payment.getPayosPaymentLinkId())
                .payosTransactionRef(payment.getPayosTransactionRef())
                .payosStatus(payment.getPayosStatus())
                .build();
    }

    /**
     * Lấy toàn bộ lịch sử thanh toán (dành cho Admin).
     * @return Danh sách tất cả các bản ghi Payment được ánh xạ sang PaymentResponse DTO.
     */
    public List<PaymentResponse> getAllPaymentHistory() {
        return paymentRepository.findAll().stream()
                .map(this::mapToPaymentResponse) // Ánh xạ từng Payment sang PaymentResponse
                .collect(Collectors.toList());
    }

    /**
     * Lấy lịch sử thanh toán của một người dùng cụ thể.
     * @param userId ID của người dùng.
     * @return Danh sách các bản ghi Payment được ánh xạ sang PaymentResponse DTO của người dùng đó.
     * @throws AppException nếu không tìm thấy người dùng.
     */
    public List<PaymentResponse> getUserPaymentHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return paymentRepository.findByUser(user).stream()
                .map(this::mapToPaymentResponse) // Ánh xạ từng Payment sang PaymentResponse
                .collect(Collectors.toList());
    }

    //Triển khai sau khi deploy dự án:
//    @Transactional
//    public void handlePayOSWebhook(Webhook webhookBody) throws Exception {
//        // 1. Xác minh dữ liệu webhook
//        WebhookData verifiedData = payOSService.verifyPaymentWebhookData(webhookBody);
//
//        Long orderCode = verifiedData.getOrderCode();
//        String paymentLinkId = verifiedData.getPaymentLinkId();
//        Float amountPaid = (float) verifiedData.getAmount();
//
//        // 2. Tìm bản ghi Payment ban đầu dựa trên orderCode của PayOS
//        // Đây là điểm mấu chốt để lấy được User và Plan
//        Optional<Payment> initialPaymentOptional = paymentRepository.findByPayosOrderCode(orderCode);
//
//        if (initialPaymentOptional.isEmpty()) {
//            System.err.println("PayOS Webhook: Không tìm thấy bản ghi Payment ban đầu cho orderCode: " + orderCode + ". Có thể là lỗi đồng bộ hoặc webhook trùng lặp không đúng.");
//            throw new AppException(ErrorCode.PAYMENT_PROCESSING_ERROR); // Hoặc một lỗi cụ thể hơn
//        }
//
//        Payment paymentRecord = initialPaymentOptional.get();
//        User user = paymentRecord.getUser(); // Lấy User từ bản ghi Payment đã lưu
//        SubscriptionPlan plan = paymentRecord.getIntendedPlan(); // Lấy Plan từ bản ghi Payment đã lưu
//
//        // 3. Kiểm tra trạng thái giao dịch từ PayOS
//        if (!verifiedData.getCode().equals("00")) {
//            System.out.println("PayOS Webhook: Giao dịch không thành công/chưa hoàn tất cho order " + orderCode + ". Mã: " + verifiedData.getCode() + ", Mô tả: " + verifiedData.getDesc());
//            // Cập nhật trạng thái Payment thành FAILED/CANCELLED
//            paymentRecord.setPayosStatus(verifiedData.getCode() + " - " + verifiedData.getDesc());
//            paymentRepository.save(paymentRecord);
//            return; // Không xử lý tiếp nếu không thành công
//        }
//
//        // 4. Xử lý khi giao dịch thành công (code "00")
//        // Tránh xử lý trùng lặp webhook cho cùng một giao dịch thành công
//        if (paymentRecord.getPayosStatus() != null && paymentRecord.getPayosStatus().equals("00 - Giao dịch thành công")) {
//            System.out.println("PayOS Webhook: Order " + orderCode + " đã được xử lý thành công trước đó. Bỏ qua trùng lặp.");
//            return;
//        }
//
//        // 5. Cập nhật hoặc tạo Subscription mới cho người dùng
//        Optional<Subscription> existingActiveSubscription = subscriptionRepository.findByUserId(user.getId());
//        Subscription subscription;
//        LocalDateTime now = LocalDateTime.now();
//
//        if (existingActiveSubscription.isPresent()) {
//            // Nếu đã có subscription cho user này, cập nhật nó
//            subscription = existingActiveSubscription.get();
//            // Cập nhật gói hiện tại (có thể là nâng cấp/hạ cấp)
//            subscription.setPlan(plan);
//            subscription.setStartDate(now);
//            subscription.setEndDate(now.plusDays(plan.getDurationDays()));
//            subscription.setIsActive(true);
//            System.out.println("PayOS Webhook: Cập nhật gói đăng ký cho user " + user.getEmail() + " thành " + plan.getSubscriptionPlanName());
//        } else {
//            // Nếu chưa có subscription, tạo mới
//            subscription = Subscription.builder()
//                    .user(user)
//                    .plan(plan)
//                    .startDate(now)
//                    .endDate(now.plusDays(plan.getDurationDays()))
//                    .isActive(true)
//                    .build();
//            System.out.println("PayOS Webhook: Tạo gói đăng ký mới cho user " + user.getEmail() + ": " + plan.getSubscriptionPlanName());
//        }
//        subscriptionRepository.save(subscription); // LƯU SUBSCRIPTION VÀO DB
//
//        // 6. Cập nhật trạng thái isPremium của User
//        user.setIsPremium(true); // Đảm bảo User entity có trường isPremium
//        userRepository.save(user); // LƯU USER VÀO DB
//
//        // 7. Cập nhật bản ghi Payment với thông tin thành công và liên kết với Subscription
//        paymentRecord.setSubscription(subscription); // LIÊN KẾT PAYMENT VỚI SUBSCRIPTION
//        paymentRecord.setAmount(amountPaid); // Số tiền đã thanh toán từ webhook
//        paymentRecord.setPaidAt(now); // Thời điểm thanh toán thành công
//        paymentRecord.setPayosStatus("00 - Giao dịch thành công");
//        paymentRecord.setPayosTransactionRef(verifiedData.getReference()); // Lưu mã tham chiếu giao dịch
//        paymentRepository.save(paymentRecord); // LƯU BẢN GHI PAYMENT VÀO DB
//
//        System.out.println("PayOS Webhook: Xử lý thành công giao dịch cho order " + orderCode + ". User " + user.getEmail() + " giờ là Premium.");
//    }

    // Cần thêm phương thức này vào SubscriptionPlanRepository
    // Optional<SubscriptionPlan> findByPrice(Float price);
}