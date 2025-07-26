package com.example.jobfinder.dto.job;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime; // Dùng LocalDateTime cho expiredDate

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobUpdateRequest {
    private String title;
    private String description;
    private String location;
    private Double salaryMin;
    private Double salaryMax;
    private Integer vacancy;
    private String responsibility;
    private LocalDateTime expiredDate;
    private Long employerId;
    private Long categoryId;
    private Long jobLevelId;
    private Long jobTypeId;
    private Long jobEducationId;
    private Long jobExperienceId;
}