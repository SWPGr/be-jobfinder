// src/main/java/com/example/jobfinder/dto/response/HourlyActivityResponse.java
package com.example.jobfinder.dto.statistic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HourlyActivityResponse {
    private int hourOfDay;       // Giờ trong ngày (0-23)
    private long newUsers;       // Số lượng người dùng mới
    private long newJobs;        // Số lượng công việc mới được đăng
    private long newApplications; // Số lượng đơn ứng tuyển mới
    // Bạn có thể thêm các hoạt động khác ở đây, ví dụ:
    // private long newReviews;
    // private long newSubscriptions;
}