package com.example.jobfinder.dto.report;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportRequest {
    LocalDate createdAt;
    Long reportTypeId;
    Long jobId;
    String subject;
    String content;
}
