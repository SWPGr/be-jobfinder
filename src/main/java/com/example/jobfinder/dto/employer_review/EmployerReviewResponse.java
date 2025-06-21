package com.example.jobfinder.dto.employer_review;

import com.example.jobfinder.dto.user.UserResponse; // Import UserResponse DTO
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployerReviewResponse {
    private Long id;
    private UserResponse jobSeeker;
    private UserResponse employer;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}