// src/main/java/com/example/jobfinder/dto/statistic_admin/PaymentComparisonResponse.java
package com.example.jobfinder.dto.statistic_admin;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentComparisonResponse {
    String monthYear; // Ví dụ: "2025-07"

    // Tổng tiền thu được trong tháng này
    Double currentMonthTotalRevenue;
    Double revenueChangePercentage;
    String revenueStatus;

    // Tổng số payment có status là PAID trong tháng này
    Long currentMonthTotalPaidPayments;
    Double paidPaymentsChangePercentage;
    String paidPaymentsStatus;

    Long currentMonthTotalPendingPayments;
    Double pendingPaymentsChangePercentage;
    String pendingPaymentsStatus;

    // Có thể thêm tổng số lượng payment (PAID + PENDING + ...)
    Long currentMonthTotalPayments;
    Double totalPaymentsChangePercentage;
    String totalPaymentsStatus;
}