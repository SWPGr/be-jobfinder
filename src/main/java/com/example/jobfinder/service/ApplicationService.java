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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public List<JobResponse> getAppliedJobsByUserId(Long userId) {

        List<Application> applications = applicationRepository.findByJobSeekerId(userId);

        User jobSeeker = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND.getErrorMessage()));

        if (!jobSeeker.getRole().getName().equals("JOB_SEEKER") && !jobSeeker.getRole().getName().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ErrorCode.UNAUTHORIZED.getErrorMessage());
        }

        List<Job> appliedJobs = applications.stream()
                .map(Application::getJob)
                .collect(Collectors.toList());

        return jobMapper.toJobResponseList(appliedJobs);
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

    @Transactional(readOnly = true)
    public PageResponse<ApplicationResponse> getEmployerJobApplications(
            int page, int size, String sortBy, String sortDir,
            String fullName, String email, String location,
            Integer minYearsExperience, Long educationId) {

        // 1. Xác thực và lấy thông tin nhà tuyển dụng
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

        // 2. Chuẩn bị phân trang
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // 3. Gọi Repository để lấy dữ liệu đã được lọc và phân trang
        Page<Application> applicationsPage = applicationRepository.findApplicationsForEmployerWithFilters(
                employerId, fullName, email, location, minYearsExperience, educationId, pageable);

        // 4. Ánh xạ từ Entity sang DTO
        List<ApplicationResponse> applicationResponses = applicationsPage.getContent().stream()
                .map(application -> {
                    // Mapping Job Simple Response
                    JobResponse jobSimpleResponse = null;
                    if (application.getJob() != null) {
                        jobSimpleResponse = JobResponse.builder()
                                .id(application.getJob().getId())
                                .title(application.getJob().getTitle())
                                .location(application.getJob().getLocation())
                                .salaryMin(application.getJob().getSalaryMin())
                                .salaryMax(application.getJob().getSalaryMax())
                                .build();
                    }

                    // Mapping Applicant Response (User và UserDetail)
                    ApplicantResponse applicantResponse = null;
                    User applicantUser = application.getJobSeeker();
                    if (applicantUser != null) {
                        UserDetail applicantDetail = applicantUser.getUserDetail(); // Lấy UserDetail
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
                                .experience(applicantDetail != null ? applicantDetail.getExperience() : null)
                                .phone(applicantDetail != null ? applicantDetail.getPhone() : null)
                                .education(educationResponse)
                                .build();
                    }

                    return ApplicationResponse.builder()
                            .id(application.getId())
                            .appliedAt(application.getAppliedAt())
                            .status(application.getStatus())
                            .job(jobSimpleResponse)
                            .build();
                })
                .collect(Collectors.toList());

        // 5. Xây dựng PageResponse
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
