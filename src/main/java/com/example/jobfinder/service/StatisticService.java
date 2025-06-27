// src/main/java/com/example/jobfinder/service/StatisticService.java
package com.example.jobfinder.service;

import com.example.jobfinder.dto.statistic_admin.*;
import com.example.jobfinder.model.Application;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.ApplicationRepository;
import com.example.jobfinder.repository.JobRepository;
import com.example.jobfinder.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional(readOnly = true)
    public List<MonthlyTrendResponse> getMonthlyTrendsCalculatedOnTheFly() {
        log.info("Service: Tính toán sự biến động các chỉ số qua từng tháng từ dữ liệu hiện có.");

        List<MonthlyTrendResponse> trends = new ArrayList<>();

        // 1. Xác định tháng bắt đầu thống kê
        // Lấy ngày tạo của user sớm nhất để làm mốc bắt đầu (hoặc bạn có thể đặt một mốc cố định)
        LocalDate startDate = userRepository.findFirstByOrderByCreatedAtAsc() // Giả định bạn có phương thức này
                .map(User::getCreatedAt)
                .map(LocalDateTime::toLocalDate)
                .orElse(LocalDate.of(2023, 1, 1)); // Mốc mặc định nếu không có user nào

        YearMonth startMonth = YearMonth.from(startDate);
        YearMonth currentMonth = YearMonth.now();

        // 2. Lặp qua từng tháng từ tháng bắt đầu đến tháng hiện tại
        for (YearMonth ym = startMonth; !ym.isAfter(currentMonth); ym = ym.plusMonths(1)) {
            LocalDateTime endDateOfMonth = ym.atEndOfMonth().atTime(23, 59, 59); // Cuối ngày cuối cùng của tháng

            // 3. Tính toán tổng số lượng cho từng category đến cuối tháng đó
            long totalJobSeekers = userRepository.countUsersByRoleNameAndCreatedAtBeforeOrEquals("JOB_SEEKER", endDateOfMonth);
            long totalEmployers = userRepository.countUsersByRoleNameAndCreatedAtBeforeOrEquals("EMPLOYER", endDateOfMonth);
            long totalJobs = jobRepository.countTotalJobsPostedBeforeOrEquals(endDateOfMonth);
            long totalAppliedJobs = applicationRepository.countUniqueAppliedJobsBeforeOrEquals(endDateOfMonth); // Sử dụng đếm unique applied jobs

            // 4. Tính Total Active Jobs
            long totalActiveJobs = totalJobs - totalAppliedJobs;
            if (totalActiveJobs < 0) { // Đảm bảo không có giá trị âm nếu có sự không nhất quán dữ liệu
                totalActiveJobs = 0;
            }

            // 5. Thêm vào danh sách kết quả
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

        // 1. Xác định ngày bắt đầu thống kê
        Optional<User> firstUserOptional = userRepository.findFirstByOrderByCreatedAtAsc();
        LocalDate startDate = firstUserOptional
                .map(User::getCreatedAt)
                .map(LocalDateTime::toLocalDate)
                .orElse(LocalDate.of(2023, 1, 1)); // Mốc mặc định nếu không có user nào

        LocalDate currentDate = LocalDate.now();

        // 2. Lặp qua từng ngày từ ngày bắt đầu đến ngày hiện tại
        // Đảm bảo startDate không muộn hơn currentDate
        if (startDate.isAfter(currentDate)) {
            log.warn("Ngày bắt đầu ({}) muộn hơn ngày hiện tại ({}). Không có dữ liệu để thống kê theo ngày.", startDate, currentDate);
            return trends; // Trả về danh sách trống
        }

        for (LocalDate date = startDate; !date.isAfter(currentDate); date = date.plusDays(1)) {
            LocalDateTime endOfDay = date.atTime(23, 59, 59); // Cuối ngày

            // 3. Tính toán tổng số lượng cho từng category đến cuối ngày đó
            long totalJobSeekers = userRepository.countUsersByRoleNameAndCreatedAtBeforeOrEquals("JOB_SEEKER", endOfDay);
            long totalEmployers = userRepository.countUsersByRoleNameAndCreatedAtBeforeOrEquals("EMPLOYER", endOfDay);
            long totalJobs = jobRepository.countTotalJobsPostedBeforeOrEquals(endOfDay);
            long totalAppliedJobs = applicationRepository.countUniqueAppliedJobsBeforeOrEquals(endOfDay); // Đếm unique applied jobs

            // 4. Tính Total Active Jobs
            long totalActiveJobs = totalJobs - totalAppliedJobs;
            if (totalActiveJobs < 0) {
                totalActiveJobs = 0;
            }

            // 5. Thêm vào danh sách kết quả
            trends.add(DailyTrendResponse.builder()
                    .date(date.toString()) // Format "YYYY-MM-DD"
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
        log.info("Service: Bắt đầu tính toán so sánh giữa tháng này và tháng trước.");

        // Xác định tháng hiện tại và tháng trước
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        // Lấy số liệu của tháng hiện tại (cuối tháng hiện tại)
        LocalDateTime endOfCurrentMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        long currentMonthTotalJobs = jobRepository.countTotalJobsPostedBeforeOrEquals(endOfCurrentMonth);
        long currentMonthTotalAppliedJobs = applicationRepository.countUniqueAppliedJobsBeforeOrEquals(endOfCurrentMonth);
        long currentMonthTotalJobSeekers = userRepository.countUsersByRoleNameAndCreatedAtBeforeOrEquals("JOB_SEEKER", endOfCurrentMonth);
        long currentMonthTotalEmployers = userRepository.countUsersByRoleNameAndCreatedAtBeforeOrEquals("EMPLOYER", endOfCurrentMonth);

        // Lấy số liệu của tháng trước (cuối tháng trước)
        LocalDateTime endOfPreviousMonth = previousMonth.atEndOfMonth().atTime(23, 59, 59);
        long previousMonthTotalJobs = jobRepository.countTotalJobsPostedBeforeOrEquals(endOfPreviousMonth);
        long previousMonthTotalAppliedJobs = applicationRepository.countUniqueAppliedJobsBeforeOrEquals(endOfPreviousMonth);
        long previousMonthTotalJobSeekers = userRepository.countUsersByRoleNameAndCreatedAtBeforeOrEquals("JOB_SEEKER", endOfPreviousMonth);
        long previousMonthTotalEmployers = userRepository.countUsersByRoleNameAndCreatedAtBeforeOrEquals("EMPLOYER", endOfPreviousMonth);

        // --- Tính toán phần trăm thay đổi và trạng thái cho từng chỉ số ---
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

    // --- Hàm tiện ích để tính toán phần trăm thay đổi và trạng thái ---
    // Sử dụng Reflection để set giá trị, giúp code gọn hơn.
    // Nếu bạn không muốn dùng Reflection, bạn sẽ phải viết 4 khối if/else riêng biệt cho mỗi chỉ số.
    private void calculateAndSetComparison(MonthlyComparisonResponse.MonthlyComparisonResponseBuilder builder,
                                           long currentValue, long previousValue,
                                           String percentageFieldName, String statusFieldName) {
        double changePercentage = 0.0;
        String status = "no_change";

        if (previousValue != 0) {
            changePercentage = ((double) (currentValue - previousValue) / previousValue) * 100;
        } else if (currentValue > 0) {
            // Nếu tháng trước là 0 và tháng này > 0, coi như tăng trưởng vô hạn (hoặc 100% nếu muốn)
            // hoặc bạn có thể định nghĩa là "increase_from_zero"
            changePercentage = 100.0; // Hoặc một giá trị tượng trưng cho sự tăng trưởng lớn
        }
        // else if (currentValue == 0 && previousValue == 0) -> changePercentage = 0, status = "no_change" (đã khởi tạo)

        if (changePercentage > 0) {
            status = "increase";
        } else if (changePercentage < 0) {
            status = "decrease";
        }

        try {
            // Sử dụng Reflection để set giá trị vào builder
            // Đây là cách gọn, nhưng nếu không thích Reflection, bạn có thể viết thủ công
            // builder.jobsChangePercentage(changePercentage)
            // builder.jobsStatus(status)
            // ...
            java.lang.reflect.Method setPercentageMethod = builder.getClass().getMethod(percentageFieldName, double.class);
            setPercentageMethod.invoke(builder, changePercentage);

            java.lang.reflect.Method setStatusMethod = builder.getClass().getMethod(statusFieldName, String.class);
            setStatusMethod.invoke(builder, status);

        } catch (Exception e) {
            log.error("Lỗi khi setting giá trị bằng Reflection: {}", e.getMessage());
            // Xử lý lỗi hoặc ném ngoại lệ tùy theo nhu cầu
        }
    }

    public List<HourlyActivityResponse> getTodayHourlyActivities() {
        log.info("Calculating hourly activities for today on the fly.");

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay(); // Bắt đầu từ 00:00:00 của hôm nay
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX); // Kết thúc vào 23:59:59.999999999 của hôm nay

        // Lấy tất cả người dùng được tạo trong ngày hôm nay
        List<User> usersToday = userRepository.findByCreatedAtBetween(startOfDay, endOfDay);
        // Lấy tất cả công việc được tạo trong ngày hôm nay
        List<Job> jobsToday = jobRepository.findByCreatedAtBetween(startOfDay, endOfDay);
        // Lấy tất cả đơn ứng tuyển được tạo trong ngày hôm nay
        List<Application> applicationsToday = applicationRepository.findByAppliedAtBetween(startOfDay, endOfDay);


        // Nhóm người dùng theo giờ tạo
        Map<Integer, Long> usersByHour = usersToday.stream()
                .collect(Collectors.groupingBy(user -> user.getCreatedAt().getHour(), Collectors.counting()));

        // Nhóm công việc theo giờ tạo
        Map<Integer, Long> jobsByHour = jobsToday.stream()
                .collect(Collectors.groupingBy(job -> job.getCreatedAt().getHour(), Collectors.counting()));

        // Nhóm đơn ứng tuyển theo giờ tạo
        Map<Integer, Long> applicationsByHour = applicationsToday.stream()
                .collect(Collectors.groupingBy(application -> application.getAppliedAt().getHour(), Collectors.counting()));

        List<HourlyActivityResponse> hourlyActivities = new ArrayList<>();

        // Tạo danh sách kết quả cho 24 giờ trong ngày
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

    @Transactional(readOnly = true) // Chỉ đọc dữ liệu
    public List<JobPostCountByCategoryAndDateResponse> getTotalJobPostCountByCategory() {
        log.info("Calculating total job post count by category.");

        // Gọi phương thức từ JobRepository để lấy kết quả dạng List<Object[]>
        List<Object[]> rawResults = jobRepository.countTotalJobsByCategory();

        // Chuyển đổi List<Object[]> sang List<JobPostCountByCategoryAndDateResponse>
        return rawResults.stream()
                .map(result -> JobPostCountByCategoryAndDateResponse.builder()
                        .categoryName((String) result[0]) // result[0] là tên category
                        .jobCount((Long) result[1])       // result[1] là tổng số lượng
                        .build())
                .collect(Collectors.toList());
    }

}