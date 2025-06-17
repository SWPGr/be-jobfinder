package com.example.jobfinder.service;

import com.example.jobfinder.dto.JobViewRequest;
import com.example.jobfinder.dto.job.JobViewResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.JobLevel;
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

    /****
     * Constructs a JobViewService with the required repositories for managing job view records.
     */
    public JobViewService(JobViewRepository jobViewRepository, UserRepository userRepository, JobRepository jobRepository) {
        this.jobViewRepository = jobViewRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
    }

    /**
     * Records a job view event for the authenticated job seeker, ensuring only one view per job per day is stored.
     *
     * If the job seeker has not viewed the specified job today, creates and saves a new job view record.
     * If a view already exists for today, returns the existing record.
     * Only users with the "JOB_SEEKER" role are permitted to record job views.
     *
     * @param request the job view request containing the job ID to be viewed
     * @return a response DTO representing the job view event
     *
     * @throws AppException if the authenticated user is not found
     * @throws ResponseStatusException if the user is not a job seeker or the job does not exist
     */
    public JobViewResponse recordJobView(JobViewRequest request) {
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
        boolean alreadyViewedToday = jobViewRepository.findByJobSeekerIdAndJobIdAndViewedAtAfter(jobSeeker.getId(), job.getId(), startOfDay)
                .isPresent();
        if(!alreadyViewedToday) {
            JobView jobView = new JobView();
            jobView.setJobSeeker(jobSeeker);
            jobView.setJob(job);
            jobView.setViewedAt(LocalDateTime.now());
            log.debug("Recording job view for user: {} and job: {}", jobSeeker.getId(), job.getId());
            jobViewRepository.save(jobView);
            return mapToJobViewResponse(jobView);
        } else {
            log.debug("Job already viewed today by user: {} for job: {}", jobSeeker.getId(), job.getId());
            JobView existingView = jobViewRepository.findByJobSeekerIdAndJobIdAndViewedAtAfter(jobSeeker.getId(), job.getId(), startOfDay)
                    .get();
            return mapToJobViewResponse(existingView);
        }


    }

    /**
     * Converts a JobView entity to a JobViewResponse DTO containing job and job seeker details.
     *
     * @param jobView the JobView entity to convert
     * @return a JobViewResponse with job ID, title, job seeker information, and view timestamp
     */
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
