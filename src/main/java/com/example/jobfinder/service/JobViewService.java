package com.example.jobfinder.service;

import com.example.jobfinder.dto.job.JobViewRequest;
import com.example.jobfinder.dto.job.JobViewResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.JobView;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.JobRepository;
import com.example.jobfinder.repository.JobViewRepository;
import com.example.jobfinder.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;



@Service
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class JobViewService {
    Logger log = LoggerFactory.getLogger(JobViewService.class);
    JobViewRepository jobViewRepository;
    UserRepository userRepository;
    JobRepository jobRepository;

    public JobViewResponse recordJobView(JobViewRequest request) {
        log.debug("Processing job view request: {}", request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            log.debug("Anonymous user viewing job ID: {}", request.getJobId());
            return null;
        }

        String email = authentication.getName();
        User jobSeeker = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!"JOB_SEEKER".equals(jobSeeker.getRole().getName())) {
            log.debug("User is not JOB_SEEKER, skipping job view record");
            return null;
        }

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        JobView jobView = new JobView();
        jobView.setJobSeeker(jobSeeker);
        jobView.setJob(job);
        jobView.setViewedAt(LocalDateTime.now());

        log.debug("Recording job view for user: {} and job: {}", jobSeeker.getId(), job.getId());
        jobViewRepository.save(jobView);

        return mapToJobViewResponse(jobView);
    }


    private JobViewResponse mapToJobViewResponse(JobView jobView) {
        return JobViewResponse.builder()
                .id(jobView.getId())
                .jobId(jobView.getJob().getId())
                .jobTitle(jobView.getJob().getTitle())
                .jobSeekerId(jobView.getJobSeeker().getId())
                .jobSeekerEmail(jobView.getJobSeeker().getEmail())
                .viewedAt(jobView.getViewedAt())
                .build();
    }
}
