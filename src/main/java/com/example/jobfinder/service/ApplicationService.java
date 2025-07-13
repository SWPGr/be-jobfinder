package com.example.jobfinder.service;

import com.example.jobfinder.dto.PageResponse;
import com.example.jobfinder.dto.application.ApplicantResponse;
import com.example.jobfinder.dto.application.ApplicationRequest;
import com.example.jobfinder.dto.application.ApplicationResponse;
import com.example.jobfinder.dto.application.ApplicationStatusUpdateRequest;
import com.example.jobfinder.dto.job.CandidateDetailResponse;
import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.dto.statistic_admin.DailyApplicationCountResponse;
import com.example.jobfinder.dto.statistic_admin.MonthlyApplicationStatsResponse;
import com.example.jobfinder.dto.user.JobSeekerResponse;
import com.example.jobfinder.dto.user.UserResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.ApplicationMapper;
import com.example.jobfinder.mapper.JobMapper;
import com.example.jobfinder.mapper.UserMapper;
import com.example.jobfinder.model.*;
import com.example.jobfinder.model.enums.ApplicationStatus;
import com.example.jobfinder.repository.ApplicationRepository;
import com.example.jobfinder.repository.JobRepository;
import com.example.jobfinder.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.sql.Date;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationService {
    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

     final ApplicationRepository applicationRepository;
     final UserRepository userRepository;
     final JobRepository jobRepository;
     final JobMapper jobMapper;
     final UserMapper userMapper;
     final NotificationService notificationService;
     final ApplicationMapper applicationMapper;

    @Transactional
    public ApplicationResponse applyJob(ApplicationRequest request) {
        log.debug("Processing apply job request: {}", request);

        // 1. Xác thực người dùng và vai trò
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            // Sử dụng ErrorCode.UNAUTHENTICATED
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHENTICATED.getErrorMessage());
            // Hoặc nếu bạn có một cơ chế exception cụ thể hơn để truyền ErrorCode object
            // throw new AppException(ErrorCode.UNAUTHENTICATED); // Nếu bạn có lớp AppException
        }
        String email = authentication.getName();
        log.debug("Authenticated email: {}", email);

        User jobSeeker = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND.getErrorMessage()));
        // Hoặc nếu bạn muốn chi tiết hơn với UsernameNotFoundException,
        // GlobalExceptionHandler của bạn cần handle nó để trả về USER_NOT_FOUND.

        String role = jobSeeker.getRole().getName();
        if (!role.equals("JOB_SEEKER")) {
            // Sử dụng ErrorCode.UNAUTHORIZED
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ErrorCode.UNAUTHORIZED.getErrorMessage());
        }

        // 2. Tìm kiếm Job
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.JOB_NOT_FOUND.getErrorMessage()));

        // 3. Kiểm tra xem người dùng đã nộp đơn cho công việc này chưa
        Optional<Application> existingApplication = applicationRepository.findByJobSeekerIdAndJobId(jobSeeker.getId(), job.getId());
        if (existingApplication.isPresent()) {
            // Sử dụng ErrorCode.APPLICATION_ALREADY_SUBMITTED
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.APPLICATION_ALREADY_SUBMITTED.getErrorMessage());
        }

        // 4. Tạo đối tượng Application
        Application application = new Application();
        application.setJobSeeker(jobSeeker);
        application.setJob(job);
        application.setStatus(ApplicationStatus.PENDING);
        application.setAppliedAt(LocalDateTime.now());

        // 5. Lưu Application vào database
        Application createdApplication = applicationRepository.save(application);

        // 6. Chuyển đổi Entity sang DTO và trả về bằng MapStruct
        return applicationMapper.toApplicationResponse(createdApplication);
    }

    public boolean isJobOwnedByEmployer(Long jobId, Long employerId) {
        return jobRepository.existsByIdAndEmployerId(jobId, employerId); // Cần phương thức này trong JobRepository
    }

    @Transactional(readOnly = true)
    public long getTotalApplications() {
        log.info("Service: Đếm tổng số ứng tuyển công việc.");
        return applicationRepository.countAllApplications();
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId,
                                                       ApplicationStatusUpdateRequest request,
                                                       Long employerId) { // EmployerId là người đang đăng nhập

        // 1. Tìm Application
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));

        // 2. Kiểm tra quyền: Đảm bảo nhà tuyển dụng đang đăng nhập là chủ sở hữu của Job này
        if (!application.getJob().getEmployer().getId().equals(employerId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_APPLICATION_UPDATE);
        }

        // 3. Chuyển đổi String status từ request sang Enum
        ApplicationStatus newStatus;
        try {
            newStatus = ApplicationStatus.fromString(request.getStatus());//Lấy giá trị status từ request gửi đi
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_APPLICATION_STATUS);
        }

        // 4. Cập nhật trạng thái
        application.setStatus(newStatus);
        Application updatedApplication = applicationRepository.save(application);

        return applicationMapper.toApplicationResponse(updatedApplication);
    }

    public Page<JobResponse> getAppliedJobsByUserId(Long userId, Pageable pageable) {

        User jobSeeker = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND.getErrorMessage()));

        if (!jobSeeker.getRole().getName().equals("JOB_SEEKER") && !jobSeeker.getRole().getName().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ErrorCode.UNAUTHORIZED.getErrorMessage());
        }

        // Thay đổi từ findByJobSeekerId thành findByJobSeekerId (trả về Page)
        // Spring Data JPA sẽ tự động tạo query phân trang
        Page<Application> applicationsPage = applicationRepository.findByJobSeekerId(userId, pageable);

        // Lấy danh sách Job từ Page<Application>
        List<Job> appliedJobs = applicationsPage.getContent().stream()
                .map(Application::getJob)
                .collect(Collectors.toList());

        // Chuyển đổi List<Job> sang List<JobResponse>
        List<JobResponse> jobResponses = jobMapper.toJobResponseList(appliedJobs);

        // Tạo một đối tượng Page<JobResponse> mới từ List<JobResponse> và thông tin phân trang của applicationsPage
        return new PageImpl<>(jobResponses, pageable, applicationsPage.getTotalElements());
    }

    public List<CandidateDetailResponse> getCandidatesDetailByJobId(Long jobId) {
        // Sử dụng phương thức mới từ repository để lấy User cùng với SeekerDetail
        List<User> applicants = applicationRepository.findApplicantsWithDetailsByJobId(jobId);

        // Chuyển đổi List<User> sang List<CandidateDetailResponse>
        return applicants.stream().map(user -> {
            JobSeekerResponse jobSeekerResponse = null;
            Education education = user.getUserDetail().getEducation();
            Experience experience = user.getUserDetail().getExperience();
            if (user.getUserDetail() != null) {
                jobSeekerResponse = JobSeekerResponse.builder()
                        .userId(user.getUserDetail().getId())
                        .phone(user.getUserDetail().getPhone())
                        .location(user.getUserDetail().getLocation())
                        .resumeUrl(user.getUserDetail().getResumeUrl())
                        .userEmail(user.getEmail())
                        .fullName(user.getUserDetail().getFullName())
                        .educationName(education != null ? education.getName() : null)
                        .experienceName(experience != null ? experience.getName() : null)
                        .build();
            }

            return CandidateDetailResponse.builder()
                    .userId(user.getId())
                    .fullname(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().getName()) // Lấy tên role
                    .seekerDetail(jobSeekerResponse)
                    .build();
        }).collect(Collectors.toList());
    }

    public MonthlyApplicationStatsResponse getApplicationsLast3Months() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(3);

        // Chuyển đổi LocalDate thành LocalDateTime cho start và end của khoảng thời gian
        LocalDateTime startDateTime = startDate.atStartOfDay(); // Bắt đầu từ 00:00:00 của startDate
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // Kết thúc vào 23:59:59.999... của endDate

        List<Object[]> rawCounts = applicationRepository.countApplicationsByDateTimeRange(startDateTime, endDateTime);


        Map<LocalDate, Long> dailyCountsMap = rawCounts.stream()
                .collect(Collectors.toMap(
                        // arr[0] là java.sql.Date, cần chuyển đổi sang java.time.LocalDate
                        arr -> ((Date) arr[0]).toLocalDate(), // <-- CHỈNH SỬA DÒNG NÀY
                        arr -> (Long) arr[1],
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        List<DailyApplicationCountResponse> dailyStats = new ArrayList<>();
        long totalApplications = 0;

        // Điền dữ liệu cho tất cả các ngày trong khoảng thời gian,
        // kể cả những ngày không có đơn ứng tuyển (count = 0)
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            Long count = dailyCountsMap.getOrDefault(currentDate, 0L);
            dailyStats.add(DailyApplicationCountResponse.builder()
                    .date(currentDate)
                    .count(count)
                    .build());
            totalApplications += count;
            currentDate = currentDate.plusDays(1);
        }

        return MonthlyApplicationStatsResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .dailyCounts(dailyStats)
                .totalApplications(totalApplications)
                .build();
    }

    // Giữ lại method cũ nếu bạn vẫn muốn truy vấn theo từng ngày
    public DailyApplicationCountResponse getDailyApplicationCount(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        Long count = applicationRepository.countByAppliedAt(date.atStartOfDay()); // Hoặc countApplicationsByDay(date)
        return DailyApplicationCountResponse.builder()
                .date(date)
                .count(count)
                .build();
    }

    @Transactional(readOnly = true) // Optimize read operations, no data modification
    public PageResponse<ApplicationResponse> getEmployerJobApplicationsForSpecificJob(
            Long jobId, int page, int size, String sortOrder,
            String name, Integer minExperience, Integer maxExperience,
            Long jobTypeId, Long educationId, Long jobLevelId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User not authenticated."); // User must be logged in
        }

        // Get the email (principal name) of the authenticated user
        String userEmail = authentication.getName();
        User currentEmployer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Employer not found for authenticated user: " + userEmail));

        // Verify that the authenticated user has the 'EMPLOYER' role
        if (!currentEmployer.getRole().getName().equals("EMPLOYER")) {
            throw new IllegalStateException("Access denied: User is not an employer."); // Only employers can access
        }

        Long employerId = currentEmployer.getId();

        // --- 2. Job Ownership Verification ---
        // Ensure that the job being queried belongs to the authenticated employer
        boolean isJobOwnedByEmployer = jobRepository.existsByIdAndEmployerId(jobId, employerId);
        if (!isJobOwnedByEmployer) {
            // Use custom AppException for specific error codes/messages
            throw new AppException(ErrorCode.JOB_ALREADY_EXISTS); // Job not found or access denied
        }

        // --- 3. Prepare Pagination and Sorting ---
        Sort sort;
        // Determine sorting direction based on 'sortOrder' parameter
        if ("newest".equalsIgnoreCase(sortOrder)) {
            // For "newest", sort by 'appliedAt' in descending order
            sort = Sort.by("appliedAt").descending();
        } else if ("latest".equalsIgnoreCase(sortOrder)) {
            // For "latest", sort by 'appliedAt' in ascending order
            sort = Sort.by("appliedAt").ascending();
        } else {
            // Default sort order if 'sortOrder' is neither "newest" nor "latest"
            sort = Sort.by("appliedAt").descending(); // Default to newest if invalid input
        }
        // Create a Pageable object for pagination and sorting
        Pageable pageable = PageRequest.of(page, size, sort);

        // --- 4. Call Repository to Fetch Filtered and Paginated Data ---
        // This is the call you specifically asked for, with the exact method name
        Page<Application> applicationsPage = applicationRepository.getEmployerJobApplicationsForSpecificJob(
                employerId, jobId,
                name, minExperience, maxExperience,
                jobTypeId, educationId, jobLevelId,
                pageable // Pass the Pageable object
        );

        // --- 5. Map Entities to DTOs and Build PageResponse ---
        return buildPageResponse(applicationsPage);
    }


    private PageResponse<ApplicationResponse> buildPageResponse(Page<Application> applicationsPage) {
        List<ApplicationResponse> applicationResponses = applicationsPage.getContent().stream()
                .map(application -> {
                    // Map JobSimpleResponse details
                    JobResponse jobSimpleResponse = null;
                    Job jobEntity = application.getJob(); // Get associated Job entity
                    if (jobEntity != null) {
                        User employerEntity = jobEntity.getEmployer(); // Get associated Employer (User)
                        UserResponse employerResponse = null;
                        if (employerEntity != null) {
                            employerResponse = UserResponse.builder()
                                    .id(employerEntity.getId())
                                    .email(employerEntity.getEmail())
                                    .build(); // Build employer DTO
                        }

                        SimpleNameResponse categoryResponse = null;
                        if (jobEntity.getCategory() != null) {
                            categoryResponse = SimpleNameResponse.builder()
                                    .id(jobEntity.getCategory().getId())
                                    .name(jobEntity.getCategory().getName())
                                    .build(); // Build category DTO
                        }

                        SimpleNameResponse jobLevelResponse = null;
                        if (jobEntity.getJobLevel() != null) {
                            jobLevelResponse = SimpleNameResponse.builder()
                                    .id(jobEntity.getJobLevel().getId())
                                    .name(jobEntity.getJobLevel().getName())
                                    .build(); // Build job level DTO
                        }

                        SimpleNameResponse jobTypeResponse = null;
                        if (jobEntity.getJobType() != null) {
                            jobTypeResponse = SimpleNameResponse.builder()
                                    .id(jobEntity.getJobType().getId())
                                    .name(jobEntity.getJobType().getName())
                                    .build(); // Build job type DTO
                        }

                        jobSimpleResponse = JobResponse.builder()
                                .id(jobEntity.getId())
                                .title(jobEntity.getTitle())
                                .description(jobEntity.getDescription())
                                .location(jobEntity.getLocation())
                                .salaryMin(jobEntity.getSalaryMin())
                                .salaryMax(jobEntity.getSalaryMax())
                                .responsibility(jobEntity.getResponsibility())
                                .expiredDate(jobEntity.getExpiredDate())
                                .createdAt(jobEntity.getCreatedAt())
                                .employer(employerResponse)
                                .category(categoryResponse)
                                .jobLevel(jobLevelResponse)
                                .jobType(jobTypeResponse)
                                // .jobApplicationCounts(jobApplicationCountForJob) // Remove or calculate if needed
                                .isSave(false) // Assuming default or determined logic
                                .build();
                    }

                    // Map ApplicantResponse (JobSeeker) details
                    ApplicantResponse applicantResponse = null;
                    // **IMPORTANT:** Replace 'getJobSeeker()' with 'getApplicant()' if your Application entity uses 'applicant'
                    User applicantUser = application.getJobSeeker();
                    if (applicantUser != null) {
                        UserDetail applicantDetail = applicantUser.getUserDetail();
                        SimpleNameResponse educationResponse = null;
                        if (applicantDetail != null && applicantDetail.getEducation() != null) {
                            educationResponse = SimpleNameResponse.builder()
                                    .id(applicantDetail.getEducation().getId())
                                    .name(applicantDetail.getEducation().getName())
                                    .build();
                        }

                        applicantResponse = ApplicantResponse.builder()
                                .id(applicantUser.getId())
                                .email(applicantUser.getEmail())
                                .fullName(applicantDetail != null ? applicantDetail.getFullName() : null)
                                .location(applicantDetail != null ? applicantDetail.getLocation() : null)
                                .phone(applicantDetail != null ? applicantDetail.getPhone() : null)
                                .education(educationResponse)
                                // Removed salary fields as per requirement
                                .build();
                    }

                    // Build the main ApplicationResponse DTO
                    return ApplicationResponse.builder()
                            .id(application.getId())
                            .jobSeeker(applicantResponse) // Assuming ApplicationResponse has a jobSeeker field
                            .job(jobSimpleResponse)
                            .status(application.getStatus())
                            .appliedAt(application.getAppliedAt())

                            .build();
                })
                .collect(Collectors.toList());

        // Build and return the PageResponse metadata and content
        return PageResponse.<ApplicationResponse>builder()
                .pageNumber(applicationsPage.getNumber())
                .pageSize(applicationsPage.getSize())
                .totalElements(applicationsPage.getTotalElements())
                .totalPages(applicationsPage.getTotalPages())
                .isLast(applicationsPage.isLast())
                .isFirst(applicationsPage.isFirst())
                .content(applicationResponses)
                .build();
    }

}
