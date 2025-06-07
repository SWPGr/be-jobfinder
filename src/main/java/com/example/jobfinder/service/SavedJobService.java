package com.example.jobfinder.service;

import com.example.jobfinder.dto.SavedJobRequest;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.SavedJob;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.JobRepository;
import com.example.jobfinder.repository.SavedJobRepository;
import com.example.jobfinder.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class SavedJobService {

    private static final Logger log = LoggerFactory.getLogger(SavedJobService.class);

    private final SavedJobRepository savedJobRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    public SavedJobService(SavedJobRepository savedJobRepository, UserRepository userRepository, JobRepository jobRepository) {
        this.savedJobRepository = savedJobRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
    }

    public SavedJob savedJob(SavedJobRequest request) {
        log.debug("Processing save job request: {}", request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        String email = authentication.getName();
        log.debug("Authenticated email: {}", email);
        User jobSeeker = userRepository.findByEmail(email);
        if (jobSeeker == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        String role = jobSeeker.getRole().getName();
        log.debug("Role: {}", role);
        if (!role.equals("JOB_SEEKER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only job seekers can save jobs");
        }

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found" + request.getJobId()));

        if (savedJobRepository.findByJobSeekerIdAndJobId(jobSeeker.getId(), job.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already saved this job");
        }

        SavedJob savedJob = new SavedJob();
        savedJob.setJobSeeker(jobSeeker);
        savedJob.setJob(job);
        savedJob.setSavedAt(LocalDateTime.now());

        return savedJobRepository.save(savedJob);
    }
}
