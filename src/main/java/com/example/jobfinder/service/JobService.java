package com.example.jobfinder.service;


import com.example.jobfinder.dto.PageResponse;
import com.example.jobfinder.dto.job.JobCreationRequest;
import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.job.JobStatusUpdateRequest;
import com.example.jobfinder.dto.job.JobUpdateRequest;
import com.example.jobfinder.dto.simple.SimpleNameResponse;
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
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.criteria.Predicate;
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
    ApplicationRepository applicationRepository;
    NotificationService notificationService;
    SavedJobRepository savedJobRepository;

    public Job createJob(JobCreationRequest jobCreationRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User employer = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException(currentUsername));

        String location = employer.getUserDetail().getLocation();

        if (!employer.getRole().getName().equals("EMPLOYER") && !employer.getRole().getName().equals("COMPANY_ADMIN")) {
            throw new AppException(ErrorCode.UNAUTHORIZED); // Thay vì USER_EXIST
        }
        Category category = categoryRepository.findById(jobCreationRequest.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        System.out.println("DEBUG: Fetched Category: ID=" + category.getId() + ", Name=" + category.getName());


        JobLevel jobLevel = jobLevelRepository.findById(jobCreationRequest.getJobLevelId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_EXIST));
        System.out.println("DEBUG: Fetched JobLevel: ID=" + jobLevel.getId() + ", Name=" + jobLevel.getName());

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
        newJob.setActive(true);

            Job savedJob = jobRepository.save(newJob);

            notifyJobSeekersOfNewJob(savedJob);
        return savedJob;
    }

    private void notifyJobSeekersOfNewJob(Job newJob) {
        List<Long> jobSeekerIds = savedJobRepository.findDistinctJobSeekerIdsByEmployerId(newJob.getEmployer().getId());

        for (Long jobSeekerId : jobSeekerIds) {
            User jobSeeker = userRepository.findById(jobSeekerId)
                    .orElse(null);
            if (jobSeeker != null) {
                String message = String.format(" '%s' has recently posted a new job. You might be interested.",
                        newJob.getEmployer().getUserDetail().getCompanyName());

                notificationService.createNotification(jobSeeker.getId(), message);
            }
        }
    }


    @Transactional
    public JobResponse updateJob(Long jobId, JobUpdateRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
        jobMapper.updateJob(job, request);
        if (request.getEmployerId() != null && !request.getEmployerId().equals(job.getEmployer().getId())) {
            User newEmployer = userRepository.findById(request.getEmployerId())
                    .orElseThrow(() -> new AppException(ErrorCode.EMPLOYER_NOT_FOUND));

            if (!"EMPLOYER".equals(newEmployer.getRole().getName()) && !"ADMIN".equals(newEmployer.getRole().getName())) { // Added ADMIN check for flexibility
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            job.setEmployer(newEmployer);
        }
        if (request.getCategoryId() != null && (job.getCategory() == null || !request.getCategoryId().equals(job.getCategory().getId()))) {
            Category newCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            job.setCategory(newCategory);
        }
        if (request.getJobLevelId() != null && (job.getJobLevel() == null || !request.getJobLevelId().equals(job.getJobLevel().getId()))) {
            JobLevel newJobLevel = jobLevelRepository.findById(request.getJobLevelId())
                    .orElseThrow(() -> new AppException(ErrorCode.JOB_LEVEL_NOT_FOUND));
            job.setJobLevel(newJobLevel);
        }
        if (request.getJobTypeId() != null && (job.getJobType() == null || !request.getJobTypeId().equals(job.getJobType().getId()))) {
            JobType newJobType = jobTypeRepository.findById(request.getJobTypeId())
                    .orElseThrow(() -> new AppException(ErrorCode.JOB_TYPE_NOT_FOUND));
            job.setJobType(newJobType);
        }
        if (request.getJobEducationId() != null && (job.getEducation() == null || !request.getJobEducationId().equals(job.getEducation().getId()))) {
            Education newEducation = educationRepository.findById(request.getJobEducationId())
                    .orElseThrow(() -> new AppException(ErrorCode.EDUCATION_NOT_FOUND));
            job.setEducation(newEducation);
        }
        if (request.getJobExperienceId() != null && (job.getExperience() == null || !request.getJobExperienceId().equals(job.getExperience().getId()))) {
            Experience newExperience = experienceRepository.findById(request.getJobExperienceId())
                    .orElseThrow(() -> new AppException(ErrorCode.EXPERIENCE_NOT_FOUND));
            job.setExperience(newExperience);
        }
        return jobMapper.toJobResponse(jobRepository.save(job));
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
        Page<Job> jobPage;
        if (currentUserId != null) {
            jobPage = jobRepository.findAllActiveJobsNotSavedByUser(currentUserId, pageable);
        } else {
            jobPage = jobRepository.findAllActive(pageable);
        }
        return jobPage.map(job -> {
            JobResponse response = jobMapper.toJobResponse(job);
            response.setIsSave(false);

            Long applicationCount = applicationRepository.countByJob_Id(job.getId());
            response.setJobApplicationCounts(applicationCount);
            return response;
        });
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getAllJobsForAdmin(Pageable pageable) {
        Page<Job> jobPage = jobRepository.findAll(pageable);

        return jobPage.map(job -> {
            JobResponse response = jobMapper.toJobResponse(job);
            response.setIsSave(false);
            Long applicationCount = applicationRepository.countByJob_Id(job.getId());
            response.setJobApplicationCounts(applicationCount);
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

    public PageResponse<JobResponse> getAllJobsForCurrentEmployer(
            int page,
            int size,
            String sortBy,
            String sortDir,
            Boolean isActive,
            String jobTitle,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User not authenticated.");
        }

        String userEmail = authentication.getName();
        User currentEmployer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Employer not found for authenticated user: " + userEmail));

        if (!currentEmployer.getRole().getName().equals("EMPLOYER")) {
            throw new IllegalStateException("Access denied: User is not an employer.");
        }

        Long employerId = currentEmployer.getId();

        Long totalApplicationCount = applicationRepository.countByJob_Employer_Id(employerId);
        Long totalOpenJobCount = jobRepository.countByEmployer_IdAndActiveTrue(employerId);

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Job> jobsPage = jobRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("employer").get("id"), employerId));

            if (jobTitle != null && !jobTitle.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + jobTitle.toLowerCase() + "%"));
            }
            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), isActive));
            }
            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }
            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);


        List<JobResponse> jobResponses = jobsPage.getContent().stream()
                .map(job -> {
                    SimpleNameResponse categoryResponse = null;
                    if (job.getCategory() != null) {
                        categoryResponse = SimpleNameResponse.builder()
                                .id(job.getCategory().getId())
                                .name(job.getCategory().getName())
                                .build();
                    }
                    SimpleNameResponse jobLevelResponse = null;
                    if (job.getJobLevel() != null) {
                        jobLevelResponse = SimpleNameResponse.builder()
                                .id(job.getJobLevel().getId())
                                .name(job.getJobLevel().getName())
                                .build();
                    }
                    SimpleNameResponse jobTypeResponse = null;
                    if (job.getJobType() != null) {
                        jobTypeResponse = SimpleNameResponse.builder()
                                .id(job.getJobType().getId())
                                .name(job.getJobType().getName())
                                .build();
                    }
                    SimpleNameResponse educationResponse = null;
                    if (job.getEducation() != null) {
                        educationResponse = SimpleNameResponse.builder()
                                .id(job.getEducation().getId())
                                .name(job.getEducation().getName())
                                .build();
                    }
                    SimpleNameResponse experienceResponse = null;
                    if (job.getExperience() != null) {
                        experienceResponse = SimpleNameResponse.builder()
                                .id(job.getExperience().getId())
                                .name(job.getExperience().getName())
                                .build();
                    }
                    Long jobApplicationCounts = applicationRepository.countByJob_Id(job.getId());
                    return JobResponse.builder()
                            .id(job.getId())
                            .title(job.getTitle())
                            .description(job.getDescription())
                            .location(job.getLocation())
                            .createdAt(job.getCreatedAt())
                            .salaryMin(job.getSalaryMin())
                            .salaryMax(job.getSalaryMax())
                            .responsibility(job.getResponsibility())
                            .expiredDate(job.getExpiredDate())
                            .vacancy(job.getVacancy())
                            .active(job.getActive())
                            .updatedAt(job.getUpdatedAt())
                            .category(categoryResponse)
                            .jobLevel(jobLevelResponse)
                            .jobType(jobTypeResponse)
                            .jobApplicationCounts(jobApplicationCounts)
                            .isSave(job.isSave())
                            .education(educationResponse)
                            .experience(experienceResponse)
                            .build();
                })
                .collect(Collectors.toList());

        return PageResponse.<JobResponse>builder()
                .pageNumber(jobsPage.getNumber())
                .pageSize(jobsPage.getSize())
                .totalElements(jobsPage.getTotalElements())
                .totalPages(jobsPage.getTotalPages())
                .isLast(jobsPage.isLast())
                .isFirst(jobsPage.isFirst())
                .content(jobResponses)
                .totalApplication(totalApplicationCount)
                .totalOpenJob(totalOpenJobCount)
                .build();
    }

    @Transactional
    public void updateJobStatus(JobStatusUpdateRequest request) {
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
        job.setActive(request.getIsActive());
        jobRepository.save(job);
        log.info("Job with ID {} active status updated to {}", request.getJobId(), request.getIsActive());
    }
}
