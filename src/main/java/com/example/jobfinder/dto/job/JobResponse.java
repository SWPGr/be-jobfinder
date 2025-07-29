package com.example.jobfinder.dto.job;
import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.dto.user.UserResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
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
    private Float salaryMin;
    private Float salaryMax;
    private String responsibility;
    private LocalDate expiredDate;
    private Boolean isSave;
    private Boolean active;
    private Integer vacancy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    UserResponse employer;
    SimpleNameResponse category;
    SimpleNameResponse jobLevel;
    SimpleNameResponse jobType;
    SimpleNameResponse education;
    SimpleNameResponse experience;
    Long jobApplicationCounts;
}
