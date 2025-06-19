// src/main/java/com/example/jobfinder/service/StatisticService.java
package com.example.jobfinder.service;

import com.example.jobfinder.dto.statistic.MonthlyTrendResponse;
import com.example.jobfinder.dto.statistic.DailyTrendResponse; // DTO mới
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
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

}