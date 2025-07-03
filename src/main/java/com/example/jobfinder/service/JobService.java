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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JobService {
    JobRepository jobRepository;
    JobMapper jobMapper;
    UserRepository userRepository;
    JobTypeRepository jobTypeRepository;
    JobLevelRepository jobLevelRepository;
    CategoryRepository categoryRepository;
    EducationRepository educationRepository;
    ExperienceRepository experienceRepository;
    SavedJobRepository savedJobRepository;


    public Job createJob(JobCreationRequest jobCreationRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();


        User employer = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException(currentUsername));

        String location = employer.getUserDetail().getLocation();

        if (employer == null ||
                (!employer.getRole().getName().equals("EMPLOYER") &&
                        !employer.getRole().getName().equals("COMPANY_ADMIN"))) {
            throw new AppException(ErrorCode.UNAUTHORIZED); // Thay vì USER_EXIST
        }

//        if (jobRepository.existsByTitleAndEmployerId(jobCreationRequest.getTitle(), employer.getId())) {
//            throw new AppException(ErrorCode.JOB_ALREADY_EXISTS);
//        }

        // 3. Lấy Category Entity từ ID
        Category category = categoryRepository.findById(jobCreationRequest.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        System.out.println("DEBUG: Fetched Category: ID=" + category.getId() + ", Name=" + category.getName());


        JobLevel jobLevel = jobLevelRepository.findById(jobCreationRequest.getJobLevelId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_EXIST));
        System.out.println("DEBUG: Fetched JobLevel: ID=" + jobLevel.getId() + ", Name=" + jobLevel.getName());


        // 5. Lấy JobType Entity từ ID
        JobType jobType = jobTypeRepository.findById(jobCreationRequest.getJobTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_EXIST));
        System.out.println("DEBUG: Fetched JobType: ID=" + jobType.getId() + ", Name=" + jobType.getName());

        Education education = educationRepository.findById(jobCreationRequest.getEducationId())
                .orElseThrow(() -> new AppException(ErrorCode.EDUCATION_NOT_FOUND));

        Experience experience = experienceRepository.findById(jobCreationRequest.getExperienceId())
                .orElseThrow(() -> new AppException(ErrorCode.EXPERIENCE_NOT_FOUND));

        Job newJob = jobMapper.toJob(jobCreationRequest);

        newJob.setEmployer(employer);
        newJob.setCategory(category);
        newJob.setJobLevel(jobLevel);
        newJob.setJobType(jobType);
        newJob.setEducation(education);
        newJob.setExperience(experience);

        newJob.setTitle(jobCreationRequest.getTitle());
        newJob.setDescription(jobCreationRequest.getDescription());
        newJob.setLocation(location);
        newJob.setSalaryMin(jobCreationRequest.getSalaryMin());
        newJob.setSalaryMax(jobCreationRequest.getSalaryMax());
        newJob.setExpiredDate(jobCreationRequest.getExpiredDate());
        newJob.setVacancy(jobCreationRequest.getVacancy());
        newJob.setResponsibility(jobCreationRequest.getResponsibility());

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

    public Page<JobResponse> getAllJobs(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());

        Long currentUserId = null;
        if (isAuthenticated) {
            String currentUserEmail = authentication.getName();
            Optional<User> userOptional = userRepository.findByEmail(currentUserEmail);
            currentUserId = userOptional.map(User::getId).orElse(null);
        }

        Page<Job> jobPage = jobRepository.findAll(pageable);

        Long finalUserId = currentUserId; // cần biến final để dùng trong lambda
        return jobPage.map(job -> {
            JobResponse response = jobMapper.toJobResponse(job);

            if (finalUserId != null) {
                boolean saved = savedJobRepository.existsByJobIdAndJobSeekerId(job.getId(), finalUserId);
                response.setSave(saved);
            } else {
                response.setSave(false);
            }

            return response;
        });
    }


    @Transactional(readOnly = true)
    public long getTotalJobs() {
        log.info("Service: Đếm tổng số công việc.");
        return jobRepository.countAllJobs();
    }

    public JobResponse getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        return jobMapper.toJobResponse(job);
    }

    public List<JobResponse> getLatestJob(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Job> jobs = jobRepository.findTopNJobs(pageable);

        return jobs.stream()
                .map(jobMapper::toJobResponse)
                .toList();
    }
}
