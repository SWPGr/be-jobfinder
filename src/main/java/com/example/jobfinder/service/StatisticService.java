// src/main/java/com/example/jobfinder/service/StatisticService.java
package com.example.jobfinder.service;

import com.example.jobfinder.dto.statistic_admin.*;
import com.example.jobfinder.dto.statistic_job_seeker.JobSeekerDashboardResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.Application;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.Payment;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StatisticService {
    UserRepository userRepository;
    JobRepository jobRepository;
    ApplicationRepository applicationRepository;
    SavedJobRepository savedJobRepository;
    JobRecommendationRepository jobRecommendationRepository;
    PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public List<MonthlyTrendResponse> getMonthlyTrendsCalculatedOnTheFly() {
        log.info("Service: Tính toán sự biến động các chỉ số qua từng tháng từ dữ liệu hiện có.");

        List<MonthlyTrendResponse> trends = new ArrayList<>();

        LocalDate startDate = userRepository.findFirstByOrderByCreatedAtAsc()
                .map(User::getCreatedAt)
                .map(LocalDateTime::toLocalDate)
                .orElse(LocalDate.of(2023, 1, 1));

        YearMonth startMonth = YearMonth.from(startDate);
        YearMonth currentMonth = YearMonth.now();

        for (YearMonth ym = startMonth; !ym.isAfter(currentMonth); ym = ym.plusMonths(1)) {
            LocalDateTime endDateOfMonth = ym.atEndOfMonth().atTime(23, 59, 59); // Cuối ngày cuối cùng của tháng

            long totalJobSeekers = userRepository.countUsersByRoleNameAndCreatedAtBeforeOrEquals("JOB_SEEKER", endDateOfMonth);
            long totalEmployers = userRepository.countUsersByRoleNameAndCreatedAtBeforeOrEquals("EMPLOYER", endDateOfMonth);
            long totalJobs = jobRepository.countTotalJobsPostedBeforeOrEquals(endDateOfMonth);
            long totalAppliedJobs = applicationRepository.countUniqueAppliedJobsBeforeOrEquals(endDateOfMonth); // Sử dụng đếm unique applied jobs

            long totalActiveJobs = totalJobs - totalAppliedJobs;
            if (totalActiveJobs < 0) {
                totalActiveJobs = 0;
            }
            trends.add(MonthlyTrendResponse.builder()
                    .monthYear(ym.toString()) // Format "YYYY-MM"
                    .totalJobSeekers(totalJobSeekers)
                    .totalEmployers(totalEmployers)
                    .totalJobs(totalJobs)
                    .totalAppliedJobs(totalAppliedJobs)
                    .totalActiveJobs(totalActiveJobs)
                    .build());
        }

        return trends;
    }

    @Transactional(readOnly = true)
    public List<DailyTrendResponse> getDailyTrendsCalculatedOnTheFly() {
        log.info("Service: Tính toán sự biến động các chỉ số qua từng ngày từ dữ liệu hiện có.");

        List<DailyTrendResponse> trends = new ArrayList<>();

        Optional<User> firstUserOptional = userRepository.findFirstByOrderByCreatedAtAsc();
        LocalDate startDate = firstUserOptional
                .map(User::getCreatedAt)
                .map(LocalDateTime::toLocalDate)
                .orElse(LocalDate.of(2023, 1, 1)); // Mốc mặc định nếu không có user nào

        LocalDate currentDate = LocalDate.now();

        if (startDate.isAfter(currentDate)) {
            log.warn("Ngày bắt đầu ({}) muộn hơn ngày hiện tại ({}). Không có dữ liệu để thống kê theo ngày.", startDate, currentDate);
            return trends;
        }
        for (LocalDate date = startDate; !date.isAfter(currentDate); date = date.plusDays(1)) {
            LocalDateTime endOfDay = date.atTime(23, 59, 59); // Cuối ngày
            long totalJobSeekers = userRepository.countUsersByRoleNameAndCreatedAtBeforeOrEquals("JOB_SEEKER", endOfDay);
            long totalEmployers = userRepository.countUsersByRoleNameAndCreatedAtBeforeOrEquals("EMPLOYER", endOfDay);
            long totalJobs = jobRepository.countTotalJobsPostedBeforeOrEquals(endOfDay);
            long totalAppliedJobs = applicationRepository.countUniqueAppliedJobsBeforeOrEquals(endOfDay);
            long totalActiveJobs = totalJobs - totalAppliedJobs;
            if (totalActiveJobs < 0) {
                totalActiveJobs = 0;
            }
            trends.add(DailyTrendResponse.builder()
                    .date(date.toString())
                    .totalJobSeekers(totalJobSeekers)
                    .totalEmployers(totalEmployers)
                    .totalJobs(totalJobs)
                    .totalAppliedJobs(totalAppliedJobs)
                    .totalActiveJobs(totalActiveJobs)
                    .build());
        }

        return trends;
    }

    @Transactional(readOnly = true)
    public MonthlyComparisonResponse getMonthOverMonthComparison() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        LocalDateTime endOfCurrentMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        long currentMonthTotalJobs = jobRepository.countTotalJobsPostedBeforeOrEquals(endOfCurrentMonth);
        long currentMonthTotalAppliedJobs = applicationRepository.countUniqueAppliedJobsBeforeOrEquals(endOfCurrentMonth);
        long currentMonthTotalJobSeekers = userRepository.countUsersByRoleNameAndCreatedAtBeforeOrEquals("JOB_SEEKER", endOfCurrentMonth);
        long currentMonthTotalEmployers = userRepository.countUsersByRoleNameAndCreatedAtBeforeOrEquals("EMPLOYER", endOfCurrentMonth);

        LocalDateTime endOfPreviousMonth = previousMonth.atEndOfMonth().atTime(23, 59, 59);
        long previousMonthTotalJobs = jobRepository.countTotalJobsPostedBeforeOrEquals(endOfPreviousMonth);
        long previousMonthTotalAppliedJobs = applicationRepository.countUniqueAppliedJobsBeforeOrEquals(endOfPreviousMonth);
        long previousMonthTotalJobSeekers = userRepository.countUsersByRoleNameAndCreatedAtBeforeOrEquals("JOB_SEEKER", endOfPreviousMonth);
        long previousMonthTotalEmployers = userRepository.countUsersByRoleNameAndCreatedAtBeforeOrEquals("EMPLOYER", endOfPreviousMonth);


        MonthlyComparisonResponse.MonthlyComparisonResponseBuilder builder = MonthlyComparisonResponse.builder()
                .monthYear(currentMonth.toString())
                .currentMonthTotalJobs(currentMonthTotalJobs)
                .currentMonthTotalAppliedJobs(currentMonthTotalAppliedJobs)
                .currentMonthTotalJobSeekers(currentMonthTotalJobSeekers)
                .currentMonthTotalEmployers(currentMonthTotalEmployers);

        // Hàm tiện ích để tính toán và trả về kết quả
        calculateAndSetComparison(builder,
                currentMonthTotalJobs, previousMonthTotalJobs,
                "jobsChangePercentage", "jobsStatus");
        calculateAndSetComparison(builder,
                currentMonthTotalAppliedJobs, previousMonthTotalAppliedJobs,
                "appliedJobsChangePercentage", "appliedJobsStatus");
        calculateAndSetComparison(builder,
                currentMonthTotalJobSeekers, previousMonthTotalJobSeekers,
                "jobSeekersChangePercentage", "jobSeekersStatus");
        calculateAndSetComparison(builder,
                currentMonthTotalEmployers, previousMonthTotalEmployers,
                "employersChangePercentage", "employersStatus");

        log.info("Service: Hoàn tất tính toán so sánh tháng này với tháng trước.");
        return builder.build();
    }

    private void calculateAndSetComparison(MonthlyComparisonResponse.MonthlyComparisonResponseBuilder builder,
                                           long currentValue, long previousValue,
                                           String percentageFieldName, String statusFieldName) {
        double changePercentage = 0.0;
        String status = "no_change";

        if (previousValue != 0) {
            changePercentage = ((double) (currentValue - previousValue) / previousValue) * 100;
        } else if (currentValue > 0) {
            changePercentage = 100.0;
        }

        if (changePercentage > 0) {
            status = "increase";
        } else if (changePercentage < 0) {
            status = "decrease";
        }

        try {
            java.lang.reflect.Method setPercentageMethod = builder.getClass().getMethod(percentageFieldName, double.class);
            setPercentageMethod.invoke(builder, changePercentage);

            java.lang.reflect.Method setStatusMethod = builder.getClass().getMethod(statusFieldName, String.class);
            setStatusMethod.invoke(builder, status);

        } catch (Exception e) {
            log.error("Lỗi khi setting giá trị bằng Reflection: {}", e.getMessage());
        }
    }

    public List<HourlyActivityResponse> getTodayHourlyActivities() {
        log.info("Calculating hourly activities for today on the fly.");

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        List<User> usersToday = userRepository.findByCreatedAtBetweenAndIsActive(startOfDay, endOfDay, false);
        List<Job> jobsToday = jobRepository.findByCreatedAtBetween(startOfDay, endOfDay);
        List<Application> applicationsToday = applicationRepository.findByAppliedAtBetween(startOfDay, endOfDay);
        Map<Integer, Long> usersByHour = usersToday.stream()
                .collect(Collectors.groupingBy(user -> user.getCreatedAt().getHour(), Collectors.counting()));
        Map<Integer, Long> jobsByHour = jobsToday.stream()
                .collect(Collectors.groupingBy(job -> job.getCreatedAt().getHour(), Collectors.counting()));
        Map<Integer, Long> applicationsByHour = applicationsToday.stream()
                .collect(Collectors.groupingBy(application -> application.getAppliedAt().getHour(), Collectors.counting()));

        List<HourlyActivityResponse> hourlyActivities = new ArrayList<>();

        for (int hour = 0; hour < 24; hour++) {
            hourlyActivities.add(HourlyActivityResponse.builder()
                    .hourOfDay(hour)
                    .newUsers(usersByHour.getOrDefault(hour, 0L))
                    .newJobs(jobsByHour.getOrDefault(hour, 0L))
                    .newApplications(applicationsByHour.getOrDefault(hour, 0L))
                    .build());
        }
        log.debug("Generated hourly activities: {}", hourlyActivities);
        return hourlyActivities;
    }

    @Transactional(readOnly = true)
    public List<JobPostCountByCategoryAndDateResponse> getTotalJobPostCountByCategory() {
        log.info("Calculating total job post count by category.");

        List<Object[]> rawResults = jobRepository.countTotalJobsByCategory();
        return rawResults.stream()
                .map(result -> JobPostCountByCategoryAndDateResponse.builder()
                        .categoryName((String) result[0])
                        .jobCount((Long) result[1])
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JobSeekerDashboardResponse getDashboardSummaryForCurrentUser() {
        String authenticatedEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Đảm bảo người dùng là JOB_SEEKER
        if (!currentUser.getRole().getName().equals("JOB_SEEKER")) {
            log.warn("User {} is not a JOB_SEEKER. Cannot retrieve job seeker dashboard.", authenticatedEmail);
            throw new AppException(ErrorCode.ROLE_NOT_FOUND); // Hoặc một ErrorCode phù hợp hơn nếu bạn có
        }

        // 3. Sử dụng các Repository để đếm số lượng
        long totalAppliedJobs = applicationRepository.countByJobSeekerId(currentUser.getId());
        long totalSavedJobs = savedJobRepository.countByJobSeeker_Id(currentUser.getId()); // Đảm bảo có phương thức count này trong SavedJobRepository
        long totalJobRecommendations = jobRecommendationRepository.countByJobSeekerId(currentUser.getId()); // Đảm bảo có phương thức count này trong JobRecommendationRepository

        log.info("Dashboard summary for JobSeeker {}: Applied={}, Saved={}, Recommended={}",
                currentUser.getEmail(), totalAppliedJobs, totalSavedJobs, totalJobRecommendations);

        // 4. Trả về DTO tổng hợp
        return JobSeekerDashboardResponse.builder()
                .totalAppliedJobs(totalAppliedJobs)
                .totalSavedJobs(totalSavedJobs)
                .totalJobRecommendations(totalJobRecommendations)
                .build();
    }
    @Transactional(readOnly = true)
    public PaymentComparisonResponse getPaymentMonthOverMonthComparison() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        LocalDateTime currentMonthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime currentMonthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59, 999999999);

        LocalDateTime previousMonthStart = previousMonth.atDay(1).atStartOfDay();
        LocalDateTime previousMonthEnd = previousMonth.atEndOfMonth().atTime(23, 59, 59, 999999999);

        // --- Lấy dữ liệu cho tháng hiện tại ---
        List<Payment> currentMonthPayments = paymentRepository.findByPaidAtBetween(currentMonthStart, currentMonthEnd);
        double currentMonthTotalRevenue = currentMonthPayments.stream()
                .mapToDouble(Payment::getAmount)
                .sum();
        long currentMonthTotalPaidPayments = currentMonthPayments.stream()
                .filter(p -> "PAID".equalsIgnoreCase(p.getPayosStatus())) // Giả định trường status là payosStatus
                .count();
        long currentMonthTotalPendingPayments = currentMonthPayments.stream()
                .filter(p -> "PENDING".equalsIgnoreCase(p.getPayosStatus())) // Giả định trường status là payosStatus
                .count();
        long currentMonthTotalPayments = currentMonthPayments.size();

        List<Payment> previousMonthPayments = paymentRepository.findByPaidAtBetween(previousMonthStart, previousMonthEnd);
        double previousMonthTotalRevenue = previousMonthPayments.stream()
                .mapToDouble(Payment::getAmount)
                .sum();
        long previousMonthTotalPaidPayments = previousMonthPayments.stream()
                .filter(p -> "PAID".equalsIgnoreCase(p.getPayosStatus()))
                .count();
        long previousMonthTotalPendingPayments = previousMonthPayments.stream()
                .filter(p -> "PENDING".equalsIgnoreCase(p.getPayosStatus()))
                .count();
        long previousMonthTotalPayments = previousMonthPayments.size();

        return PaymentComparisonResponse.builder()
                .monthYear(currentMonth.toString())
                // Tổng doanh thu
                .currentMonthTotalRevenue(currentMonthTotalRevenue)
                .revenueChangePercentage(calculateChangePercentage(currentMonthTotalRevenue, previousMonthTotalRevenue))
                .revenueStatus(getChangeStatus(currentMonthTotalRevenue, previousMonthTotalRevenue))
                // Tổng Paid Payments
                .currentMonthTotalPaidPayments(currentMonthTotalPaidPayments)
                .paidPaymentsChangePercentage(calculateChangePercentage(currentMonthTotalPaidPayments, previousMonthTotalPaidPayments))
                .paidPaymentsStatus(getChangeStatus(currentMonthTotalPaidPayments, previousMonthTotalPaidPayments))
                // Tổng Pending Payments
                .currentMonthTotalPendingPayments(currentMonthTotalPendingPayments)
                .pendingPaymentsChangePercentage(calculateChangePercentage(currentMonthTotalPendingPayments, previousMonthTotalPendingPayments))
                .pendingPaymentsStatus(getChangeStatus(currentMonthTotalPendingPayments, previousMonthTotalPendingPayments))
                // Tổng số lượng Payments
                .currentMonthTotalPayments(currentMonthTotalPayments)
                .totalPaymentsChangePercentage(calculateChangePercentage(currentMonthTotalPayments, previousMonthTotalPayments))
                .totalPaymentsStatus(getChangeStatus(currentMonthTotalPayments, previousMonthTotalPayments))
                .build();
    }

    private double calculateChangePercentage(double current, double previous) {
        if (previous == 0) {
            return current == 0 ? 0.0 : 100.0; // Nếu tháng trước 0, tháng này 0 thì 0%, nếu tháng trước 0 tháng này >0 thì 100% tăng
        }
        return ((current - previous) / previous) * 100.0;
    }

    private String getChangeStatus(double current, double previous) {
        if (current > previous) {
            return "increase";
        } else if (current < previous) {
            return "decrease";
        } else {
            return "no_change";
        }
    }

}