package com.example.jobfinder.dto.statistic_employer;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationCountDto {
    private Long jobId;
    private String jobTitle;
    private Long applicationCount;
}