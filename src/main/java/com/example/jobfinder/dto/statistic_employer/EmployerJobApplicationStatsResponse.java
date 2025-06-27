package com.example.jobfinder.dto.statistic_employer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerJobApplicationStatsResponse {
    private Long employerId;
    private String employerName; // Tên nhà tuyển dụng
    private List<JobApplicationCountDto> jobApplicationCounts;
    private Long totalApplicationsAcrossJobs; // Tổng số đơn ứng tuyển của tất cả các job của employer
}