package com.example.jobfinder.dto.job;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobViewResponse {
    Long id;
    Long jobId;
    String jobTitle;
    Long jobSeekerId;
    String jobSeekerEmail;
    LocalDateTime viewedAt;
}
