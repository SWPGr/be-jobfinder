package com.example.jobfinder.dto.report;

import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.ReportType;
import com.example.jobfinder.model.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportResponse {
    Long id;
    LocalDate createdAt;
    ReportType type;
    String user;
    String email;
    Long jobId;
    String subject;
    String content;

}
