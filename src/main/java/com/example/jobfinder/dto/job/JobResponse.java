package com.example.jobfinder.dto.job;
import com.example.jobfinder.dto.SimpleNameResponse;
import com.example.jobfinder.dto.user.UserResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private Double salaryMin;
    private Double salaryMax;

    // Đảm bảo kiểu là LocalDateTime. @JsonFormat giúp định dạng khi chuyển sang JSON.
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt; // <-- PHẢI CÓ TRƯỜNG NÀY VỚI KIỂU NÀY

    UserResponse employer; // <-- Phải là EmployerDto, không phải Long employerId
    SimpleNameResponse category; // <-- Phải là CategoryDto, không phải Long categoryId, String categoryName
    SimpleNameResponse jobLevel; // <-- Phải là JobLevelDto, không phải Long jobLevelId, String jobLevelName
    SimpleNameResponse jobType;   // <-- Phải là JobTypeDto, không phải Long jobTypeId, String jobTypeName
}
