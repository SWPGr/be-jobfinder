package com.example.jobfinder.service;

import com.example.jobfinder.dto.job.SavedJobRequest;
import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.job.SavedJobResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.JobMapper;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.SavedJob;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.JobRepository;
import com.example.jobfinder.repository.SavedJobRepository;
import com.example.jobfinder.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SavedJobService {

    private static final Logger log = LoggerFactory.getLogger(SavedJobService.class);

    SavedJobRepository savedJobRepository;
    UserRepository userRepository;
    JobRepository jobRepository;
    JobMapper  jobMapper;

    public Page<JobResponse> getSavedJobsByJobSeekerId(Long jobSeekerId, Pageable pageable) {
        userRepository.findById(jobSeekerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Page<SavedJob> savedJobsPage = savedJobRepository.findByJobSeeker_Id(jobSeekerId, pageable);

        List<Job> jobs = savedJobsPage.getContent().stream()
                .map(SavedJob::getJob)
                .collect(Collectors.toList());

        List<JobResponse> jobResponses = jobMapper.toJobResponseList(jobs);
        return new PageImpl<>(jobResponses, pageable, savedJobsPage.getTotalElements());
    }
    public SavedJobResponse savedJob(SavedJobRequest request) {
        log.debug("Processing save job request: {}", request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        String email = authentication.getName();
        log.debug("Authenticated email: {}", email);
        User jobSeeker = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
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

        savedJobRepository.save(savedJob);
        return mapToSavedJobResponse(savedJob);
    }

    private SavedJobResponse mapToSavedJobResponse(SavedJob saved) {
        return SavedJobResponse.builder()
                .id(saved.getId())
                .jobId(saved.getJob().getId())
                .jobSeekerId(saved.getJobSeeker().getId())
                .jobTitle(saved.getJob().getTitle())
                .jobSeekerEmail(saved.getJobSeeker().getEmail())
                .savedAt(saved.getSavedAt())
                .build();
    }

    public void unSaveJob(Long jobId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User jobSeeker = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        String role = jobSeeker.getRole().getName();
        if (!role.equals("JOB_SEEKER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only job seekers can unSave jobs");
        }
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
        SavedJob savedJob = savedJobRepository.findByJobSeekerIdAndJobId(jobSeeker.getId(), job.getId())
                .orElseThrow(() -> new AppException(ErrorCode.SAVED_JOB_NOT_FOUND));

        savedJobRepository.delete(savedJob);
        log.debug("unsaved job for user: {} and job: {}", jobSeeker.getId(), job.getId());
    }
}
