package com.example.jobfinder.service;

import com.example.jobfinder.dto.report.ReportRequest;
import com.example.jobfinder.dto.report.ReportResponse;
import com.example.jobfinder.dto.report.ReportTypeResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.Report;
import com.example.jobfinder.model.ReportType;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.JobRepository;
import com.example.jobfinder.repository.ReportRepository;
import com.example.jobfinder.repository.ReportTypeRepository;
import com.example.jobfinder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final UserRepository userRepository;
    private final ReportTypeRepository reportTypeRepository;
    private final JobRepository jobRepository;
    private final ReportRepository reportRepository;

    public ReportResponse submitReport(ReportRequest request) {
        log.debug("Processing save job request: {}", request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String email = authentication.getName();
        log.debug("Authenticated email: {}", email);
        User jobSeeker = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        String role = jobSeeker.getRole().getName();
        log.debug("Role: {}", role);
        if (!role.equals("JOB_SEEKER")) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        ReportType reportType = reportTypeRepository.findById(request.getReportTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.REPORT_TYPE_NOT_FOUND));

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        boolean alreadyReported = reportRepository.existsByUserAndJobAndSubjectAndContent(
                jobSeeker, job, request.getSubject(), request.getContent());
        if (alreadyReported) {
            throw new AppException(ErrorCode.ALREADY_REPORTED);
        }

        Report report = Report.builder()
                .reportType(reportType)
                .job(job)
                .user(jobSeeker)
                .subject(request.getSubject())
                .content(request.getContent())
                .createdAt(request.getCreatedAt() != null ? request.getCreatedAt() : LocalDateTime.now())
                .build();
        reportRepository.save(report);

        return ReportResponse.builder()
                .id(report.getId())
                .createdAt(report.getCreatedAt())
                .email(jobSeeker.getEmail())
                .jobId(job.getId())
                .subject(report.getSubject())
                .content(report.getContent())
                .type(reportType)
                .build();
    }

    public Page<ReportResponse> getAllReports(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Report> reports = reportRepository.findAll(pageable);

        return reports.map(report -> ReportResponse.builder().
                id(report.getId())
                .email(report.getUser().getEmail())
                .jobId(report.getJob().getId())
                .subject(report.getSubject())
                .content(report.getContent())
                .createdAt(report.getCreatedAt())
                .type(report.getReportType())
                .build());
    }

    public List<ReportTypeResponse> getAllReportTypes() {
        List<ReportType> reportTypes = reportTypeRepository.findAll();

        return reportTypes.stream()
                .map(type -> ReportTypeResponse.builder()
                        .id(type.getId())
                        .name(type.getName())
                        .build())
                .collect(Collectors.toList());
    }

    public Page<ReportResponse> searchReportsByType(String reportTypeName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Report> reports = reportRepository.findByReportTypeNameContainingIgnoreCase(reportTypeName, pageable);

        return reports.map(report -> ReportResponse.builder()
                .id(report.getId())
                .email(report.getUser().getEmail())
                .jobId(report.getJob().getId())
                .subject(report.getSubject())
                .content(report.getContent())
                .createdAt(report.getCreatedAt())
                .type(report.getReportType())
                .build());
    }

}
