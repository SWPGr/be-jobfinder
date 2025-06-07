package com.example.jobfinder.service;

import com.example.jobfinder.dto.JobRequest;
import com.example.jobfinder.model.*;
import com.example.jobfinder.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;


@Service
public class JobService {
    private static final Logger log = LoggerFactory.getLogger(JobService.class);
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final JobLevelRepository jobLevelRepository;
    private final JobTypeRepository jobTypeRepository;

    public JobService(
            JobRepository jobRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            JobLevelRepository jobLevelRepository,
            JobTypeRepository jobTypeRepository
    ) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.jobLevelRepository = jobLevelRepository;
        this.jobTypeRepository = jobTypeRepository;
    }

    public Job createJob(JobRequest request) {
        log.debug("Creating job with request: {}", request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        String email = authentication.getName();
        User employer = userRepository.findByEmail(email);
        if(employer == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        String role = employer.getRole().getName();
        if(!role.equals("EMPLOYER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only EMPLOYER roles are allowed");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found: " + request.getCategoryId()));

        JobLevel jobLevel = jobLevelRepository.findById(request.getJobLevelId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "JobLevel not found: " + request.getJobLevelId()));

        JobType jobType = jobTypeRepository.findById(request.getJobTypeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "JobType not found: " + request.getJobTypeId()));

        if(request.getSalaryMin() != null && request.getSalaryMax() != null && request.getSalaryMin() > request.getSalaryMax()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Salary min cannot be greater than max");
        }

        Job job = new Job();
        job.setEmployerId(employer);
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setCategory(category);
        job.setJobLevel(jobLevel);
        job.setJobType(jobType);
        job.setCreatedAt(LocalDateTime.now());

        return jobRepository.save(job);
    }
}
