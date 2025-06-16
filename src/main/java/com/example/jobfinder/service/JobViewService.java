package com.example.jobfinder.service;

import com.example.jobfinder.dto.JobViewRequest;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.JobView;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.JobRepository;
import com.example.jobfinder.repository.JobViewRepository;
import com.example.jobfinder.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Service
public class JobViewService {
    private static final Logger log = LoggerFactory.getLogger(JobViewService.class);

    private final JobViewRepository jobViewRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    public JobViewService(JobViewRepository jobViewRepository, UserRepository userRepository, JobRepository jobRepository) {
        this.jobViewRepository = jobViewRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
    }

    public JobView recordJobView(JobViewRequest request) {
        log.debug("Processing job view request: {}", request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.debug("Authenticated username: {}", email);
        User jobSeeker = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String role = jobSeeker.getRole().getName();
        log.debug("User role: {}", role);
        if(!role.equals("JOB_SEEKER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Job seeker roles are allowed");
        }

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found" + request.getJobId()));

        LocalDateTime startOfDay = LocalDate.now().atTime(LocalTime.MIN);
        if(jobViewRepository.findByJobSeekerIdAndJobIdAndViewedAtAfter(jobSeeker.getId(), job.getId(), startOfDay).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already view this job today");
        }

        JobView jobView = new JobView();
        jobView.setJobSeeker(jobSeeker);
        jobView.setJob(job);
        jobView.setViewedAt(LocalDateTime.now());
        log.debug("Recording job view for user: {} and job: {}", jobSeeker.getId(), job.getId());
        return jobViewRepository.save(jobView);
    }
}
