package com.example.jobfinder.dto.job;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SavedJobResponse {
    private Long id;
    private LocalDateTime savedAt;
    private Long jobSeekerId;
    private String jobSeekerEmail; // Hoặc username của job seeker
    private Long jobId;
    private String jobTitle; // Tiêu đề công việc
}


