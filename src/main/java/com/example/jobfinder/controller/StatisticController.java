// src/main/java/com/example/jobfinder/controller/StatisticController.java
package com.example.jobfinder.controller;

import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.dto.statistic.*;
import com.example.jobfinder.service.StatisticService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StatisticController {

    StatisticService statisticService;

    @GetMapping("/today-hourly-activity")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<HourlyActivityResponse>> getTodayHourlyActivity() {
        log.info("API: Lấy các hoạt động theo giờ trong ngày hôm nay (tính toán realtime).");
        List<HourlyActivityResponse> hourlyActivities = statisticService.getTodayHourlyActivities();
        return ApiResponse.<List<HourlyActivityResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Hourly activities for today fetched successfully")
                .result(hourlyActivities)
                .build();
    }

    // Các API khác của StatisticController đã có...
    @GetMapping("/calculated-monthly-trends")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN mới có thể xem thống kê
    public ApiResponse<List<MonthlyTrendResponse>> getCalculatedMonthlyTrends() {
        log.info("API: Lấy sự biến động các chỉ số qua từng tháng (tính toán realtime).");
        List<MonthlyTrendResponse> trends = statisticService.getMonthlyTrendsCalculatedOnTheFly();
        return ApiResponse.<List<MonthlyTrendResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Monthly trends calculated and fetched successfully")
                .result(trends)
                .build();
    }

    @GetMapping("/calculated-daily-trends")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN mới có thể xem thống kê theo ngày
    public ApiResponse<List<DailyTrendResponse>> getDailyTrends() {
        log.info("API: Lấy sự biến động các chỉ số qua từng ngày (tính toán realtime).");
        List<DailyTrendResponse> trends = statisticService.getDailyTrendsCalculatedOnTheFly();
        return ApiResponse.<List<DailyTrendResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Daily trends calculated and fetched successfully")
                .result(trends)
                .build();
    }

    @GetMapping("/month-over-month-comparison")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN mới có thể xem so sánh
    public ApiResponse<MonthlyComparisonResponse> getMonthOverMonthComparison() {
        log.info("API: Lấy so sánh giữa tháng này và tháng trước.");
        MonthlyComparisonResponse comparison = statisticService.getMonthOverMonthComparison();
        return ApiResponse.<MonthlyComparisonResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Month-over-month comparison fetched successfully")
                .result(comparison)
                .build();
    }

    @GetMapping("/job-posts-by-category/{dateString}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<JobPostCountByCategoryAndDateResponse>> getJobPostCountByCategoryForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateString) {
        log.info("API: Lấy số lượng bài đăng theo ngành nghề cho ngày: {}", dateString);
        List<JobPostCountByCategoryAndDateResponse> categoryCounts =
                statisticService.getJobPostCountByCategoryForDate(dateString);
        return ApiResponse.<List<JobPostCountByCategoryAndDateResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Job post counts by category for date fetched successfully")
                .result(categoryCounts)
                .build();
    }
}