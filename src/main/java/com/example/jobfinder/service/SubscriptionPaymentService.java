package com.example.jobfinder.service;

import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.*; // Import tất cả các model
import com.example.jobfinder.repository.*; // Import tất cả các repository
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionPaymentService {

    private final PayOSService payOSService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository; // Để cập nhật isPremium của User

    /**
     * Tạo link thanh toán PayOS cho việc mua gói Premium.
     * @param userId ID của người dùng mua gói.
     * @param planId ID của gói đăng ký muốn mua.
     * @param returnUrl URL chuyển hướng khi thành công.
     * @param cancelUrl URL chuyển hướng khi hủy.
     * @return CheckoutResponseData từ PayOS.
     * @throws Exception nếu có lỗi hoặc không tìm thấy gói.
     */
    @Transactional
    public CheckoutResponseData createPremiumSubscriptionPaymentLink(
            Long userId,
            Long planId,
            String returnUrl,
            String cancelUrl) throws Exception {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND)); // Cần định nghĩa ErrorCode này

        // Kiểm tra nếu gói là Basic (miễn phí) thì không cần thanh toán
        if (plan.getPrice() <= 0) {
            // Xử lý logic cho gói miễn phí nếu cần (ví dụ: kích hoạt ngay)
            throw new AppException(ErrorCode.INVALID_PAYMENT_FOR_FREE_PLAN); // Cần định nghĩa ErrorCode này
        }

        // Tạo orderCode duy nhất. Sử dụng orderCode từ DB hoặc một cơ chế sinh ID mạnh mẽ hơn.
        // Ví dụ: kết hợp userId và timestamp để đảm bảo tính duy nhất.
        long orderCode = Long.parseLong(String.valueOf(userId) + String.valueOf(System.currentTimeMillis()).substring(6));
        // Đảm bảo orderCode không quá dài và là kiểu long.
        // Hoặc dùng một sequence từ DB.
        // Để đơn giản, tôi dùng một cách kết hợp userId và timestamp.
        // Bạn có thể dùng một cơ chế sinh orderCode an toàn hơn.
        // Ví dụ: orderCode = generateUniqueOrderCode();

        String description = "Thanh toan " + plan.getSubscriptionPlanName();
        // Giới hạn mô tả không quá 25 ký tự cho PayOS
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }

        List<ItemData> items = Collections.singletonList(
                ItemData.builder()
                        .name(plan.getSubscriptionPlanName())
                        .quantity(1)
                        .price(plan.getPrice().intValue()) // PayOS amount là int
                        .build()
        );

        // Gọi PayOSService để tạo link
        CheckoutResponseData checkoutData = payOSService.createPaymentLink(
                orderCode,
                plan.getPrice().intValue(), // Amount phải là int
                description,
                items,
                returnUrl,
                cancelUrl
        );

        // Tạm thời lưu một bản ghi Payment với trạng thái PENDING
        // Bản ghi này sẽ được cập nhật khi webhook báo thành công
        Payment newPayment = Payment.builder()
                .user(user)
                .subscription(null) // Sẽ được gán sau khi subscription được tạo/cập nhật
                .amount(plan.getPrice())
                .paymentMethod("PayOS")
                .payosOrderCode(orderCode)
                .payosPaymentLinkId(checkoutData.getPaymentLinkId())
                .payosStatus("PENDING") // Trạng thái ban đầu
                .paidAt(null) // Sẽ được cập nhật khi webhook báo thành công
                .build();
        paymentRepository.save(newPayment);

        return checkoutData;
    }

    /**
     * Xử lý webhook từ PayOS để cập nhật trạng thái thanh toán và gói đăng ký.
     * @param webhookBody Dữ liệu webhook từ PayOS.
     * @throws Exception nếu có lỗi trong quá trình xử lý.
     */
    @Transactional
    public void handlePayOSWebhook(Webhook webhookBody) throws Exception {
        // 1. Xác minh dữ liệu webhook
        WebhookData verifiedData = payOSService.verifyPaymentWebhookData(webhookBody);

        // 2. Kiểm tra trạng thái giao dịch từ PayOS
        if (!verifiedData.getCode().equals("00")) {
            System.out.println("PayOS Webhook: Giao dịch không thành công/chưa hoàn tất cho order " + verifiedData.getOrderCode() + ". Mã: " + verifiedData.getCode() + ", Mô tả: " + verifiedData.getDesc());
            // Cập nhật trạng thái Payment thành FAILED/CANCELLED nếu cần
            Optional<Payment> existingPayment = paymentRepository.findByPayosOrderCode(verifiedData.getOrderCode());
            existingPayment.ifPresent(payment -> {
                payment.setPayosStatus(verifiedData.getCode() + " - " + verifiedData.getDesc());
                paymentRepository.save(payment);
            });
            return; // Không xử lý tiếp nếu không thành công
        }

        // 3. Xử lý khi giao dịch thành công (code "00")
        Long orderCode = verifiedData.getOrderCode();
        String paymentLinkId = verifiedData.getPaymentLinkId();
        Float amountPaid = (float) verifiedData.getAmount(); // Số tiền đã thanh toán

        // Tránh xử lý trùng lặp webhook
        Optional<Payment> existingPayment = paymentRepository.findByPayosOrderCode(orderCode);
        if (existingPayment.isPresent() && existingPayment.get().getPayosStatus().equals("00 - Giao dịch thành công")) {
            System.out.println("PayOS Webhook: Order " + orderCode + " đã được xử lý thành công trước đó. Bỏ qua trùng lặp.");
            return;
        }

        // Lấy thông tin PaymentIntent để lấy userId và planId (nếu bạn lưu trong metadata)
        // Hoặc lấy từ orderCode nếu bạn đã thiết kế orderCode chứa userId
        // Giả định orderCode được tạo từ userId + timestamp
        Long userId = Long.parseLong(String.valueOf(orderCode).substring(0, String.valueOf(orderCode).length() - 7)); // Cần điều chỉnh tùy cách bạn sinh orderCode
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Lấy thông tin PaymentLinkData từ PayOS để lấy planId (nếu bạn lưu trong description hoặc metadata)
        // Hoặc bạn cần tìm cách liên kết orderCode với planId trong DB của bạn.
        // Để đơn giản, ta sẽ tìm gói dựa trên số tiền thanh toán (không khuyến khích trong thực tế)
        // Hoặc bạn có thể lưu planId vào metadata khi tạo PaymentLink
        Optional<SubscriptionPlan> purchasedPlan = subscriptionPlanRepository.findByPrice(amountPaid); // Cần thêm findByPrice vào SubscriptionPlanRepository
        if (purchasedPlan.isEmpty()) {
            System.err.println("PayOS Webhook: Không tìm thấy gói đăng ký phù hợp với số tiền " + amountPaid + " cho order " + orderCode);
            throw new AppException(ErrorCode.PLAN_NOT_FOUND);
        }
        SubscriptionPlan plan = purchasedPlan.get();

        // 4. Cập nhật hoặc tạo Subscription mới cho người dùng
        Optional<Subscription> existingSubscription = subscriptionRepository.findByUserId(user.getId());
        Subscription subscription;
        LocalDateTime now = LocalDateTime.now();

        if (existingSubscription.isPresent()) {
            subscription = existingSubscription.get();
            // Cập nhật gói hiện tại
            subscription.setPlan(plan);
            subscription.setStartDate(now);
            subscription.setEndDate(now.plusDays(plan.getDurationDays()));
            subscription.setIsActive(true);
            System.out.println("PayOS Webhook: Cập nhật gói đăng ký cho user " + user.getEmail() + " thành " + plan.getSubscriptionPlanName());
        } else {
            // Tạo gói đăng ký mới
            subscription = Subscription.builder()
                    .user(user)
                    .plan(plan)
                    .startDate(now)
                    .endDate(now.plusDays(plan.getDurationDays()))
                    .isActive(true)
                    .build();
            System.out.println("PayOS Webhook: Tạo gói đăng ký mới cho user " + user.getEmail() + ": " + plan.getSubscriptionPlanName());
        }
        subscriptionRepository.save(subscription);

        // 5. Cập nhật trạng thái isPremium của User
        user.setIsPremium(true); // Đảm bảo User entity có trường isPremium
        userRepository.save(user);

        // 6. Lưu hoặc cập nhật bản ghi Payment
        Payment paymentRecord;
        if (existingPayment.isPresent()) {
            paymentRecord = existingPayment.get();
            paymentRecord.setSubscription(subscription);
            paymentRecord.setAmount(amountPaid);
            paymentRecord.setPaidAt(now);
            paymentRecord.setPayosStatus("00 - Giao dịch thành công");
            paymentRecord.setPayosTransactionRef(verifiedData.getReference()); // Lưu mã tham chiếu giao dịch
            System.out.println("PayOS Webhook: Cập nhật bản ghi thanh toán cho order " + orderCode);
        } else {
            // Trường hợp webhook đến trước khi Payment ban đầu được lưu (ít xảy ra nếu bạn lưu PENDING)
            // Hoặc nếu bạn không lưu PENDING ban đầu
            paymentRecord = Payment.builder()
                    .user(user)
                    .subscription(subscription)
                    .amount(amountPaid)
                    .paymentMethod("PayOS")
                    .paidAt(now)
                    .payosOrderCode(orderCode)
                    .payosPaymentLinkId(paymentLinkId)
                    .payosStatus("00 - Giao dịch thành công")
                    .payosTransactionRef(verifiedData.getReference())
                    .build();
            System.out.println("PayOS Webhook: Tạo bản ghi thanh toán mới cho order " + orderCode);
        }
        paymentRepository.save(paymentRecord);

        System.out.println("PayOS Webhook: Xử lý thành công giao dịch cho order " + orderCode);
    }

    // Cần thêm phương thức này vào SubscriptionPlanRepository
    // Optional<SubscriptionPlan> findByPrice(Float price);
}