package com.example.jobfinder.service;


import com.example.jobfinder.dto.job.JobCreationRequest;
import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.job.JobUpdateRequest;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.JobMapper;
import com.example.jobfinder.model.*;
import com.example.jobfinder.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobService {
    JobRepository jobRepository;
    JobMapper jobMapper;
    UserRepository userRepository;
    JobTypeRepository jobTypeRepository;
    JobLevelRepository jobLevelRepository;
    CategoryRepository categoryRepository;

    public Job createJob(JobCreationRequest jobCreationRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();


        User employer = userRepository.findByEmail(currentUsername);

        if (employer == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND); // Thay vì USER_EXIST
        }

        // 3. Lấy Category Entity từ ID
        Category category = categoryRepository.findById(jobCreationRequest.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        System.out.println("DEBUG: Fetched Category: ID=" + category.getId() + ", Name=" + category.getName());


        if (employer.getRole() == null ||
                (!employer.getRole().getName().equals("EMPLOYER") &&
                        !employer.getRole().getName().equals("COMPANY_ADMIN"))) {
            throw new AppException(ErrorCode.UNAUTHORIZED); // <-- CHÍNH LÀ DÒNG LỖI CỦA BẠN!
        }

        if (jobRepository.existsByTitleAndEmployerId(jobCreationRequest.getTitle(), employer.getId())) {
            throw new AppException(ErrorCode.JOB_ALREADY_EXISTS);
        }

        JobLevel jobLevel = jobLevelRepository.findById(jobCreationRequest.getJobLevelId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_EXIST));
        System.out.println("DEBUG: Fetched JobLevel: ID=" + jobLevel.getId() + ", Name=" + jobLevel.getName());


        // 5. Lấy JobType Entity từ ID
        JobType jobType = jobTypeRepository.findById(jobCreationRequest.getJobTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_EXIST));
        System.out.println("DEBUG: Fetched JobType: ID=" + jobType.getId() + ", Name=" + jobType.getName());

        Job newJob = jobMapper.toJob(jobCreationRequest);

        newJob.setEmployer(employer);
        newJob.setCategory(category);
        newJob.setJobLevel(jobLevel);
        newJob.setJobType(jobType);


        return jobRepository.save(newJob);
    }

    public JobResponse updateJob(Long jobId, JobUpdateRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        jobMapper.updateJob(job, request);

        if (request.getEmployerId() != null && !request.getEmployerId().equals(job.getEmployer().getId())) {
            User newEmployer = userRepository.findById(request.getEmployerId())
                    .orElseThrow(() -> new AppException(ErrorCode.EMPLOYER_NOT_FOUND));

            if (!"EMPLOYER".equals(newEmployer.getRole().getName()) && !"COMPANY_ADMIN".equals(newEmployer.getRole().getName())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            job.setEmployer(newEmployer);
        }



        return jobMapper.toJobResponse(jobRepository.save(job));
    }

    public void deleteJob(Long jobId) {
        if (!jobRepository.existsById(jobId)) {
            throw new AppException(ErrorCode.JOB_NOT_FOUND);
        }
        jobRepository.deleteById(jobId);
    }

    public List<JobResponse> getAllJobs() {
        return jobRepository.findAll()
                .stream()
                .map(jobMapper::toJobResponse)
                .collect(Collectors.toList());
    }

    public JobResponse getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        return jobMapper.toJobResponse(job);
    }
}
