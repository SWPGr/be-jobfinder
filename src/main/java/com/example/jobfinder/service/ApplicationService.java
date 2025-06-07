package com.example.jobfinder.service;

import com.example.jobfinder.dto.ApplicationRequest;
import com.example.jobfinder.model.Application;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.ApplicationRepository;
import com.example.jobfinder.repository.JobRepository;
import com.example.jobfinder.repository.UserRepository;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class ApplicationService {
    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    public ApplicationService(ApplicationRepository applicationRepository, UserRepository userRepository,
                              JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
    }

    public Application applyJob(ApplicationRequest request) throws Exception {
        log.debug("Processing job application with request: {}", request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.debug("Authenticated email: {}", email);
        User jobSeeker = userRepository.findByEmail(email);
        if (jobSeeker == null) {
            throw new Exception("User not found");
        }
        if (!jobSeeker.isVerified()) {
            throw new Exception("Please verify your email first");
        }

        String role = jobSeeker.getRole().getName();
        log.debug("User role: {}", role);
        if(!role.equals("JOB_SEEKER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only JOB_SEEKER can apply a job");
        }

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found: " + request.getJobId()));

        if(applicationRepository.findByJobSeekerIdAndJobId(jobSeeker.getId(), job.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already applied for this job");
        }

        Application application = new Application();
        application.setJobSeeker(jobSeeker);
        application.setJob(job);
        application.setStatus("PENDING");
        application.setAppliedAt(LocalDateTime.now());

        log.debug("Saving application for user: {} and job: {}", jobSeeker.getId(), job.getId());
        return applicationRepository.save(application);
    }
}
