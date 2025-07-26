package com.example.jobfinder.service;

import com.example.jobfinder.dto.PageResponse;
import com.example.jobfinder.dto.application.ApplicationRequest;
import com.example.jobfinder.dto.application.ApplicationResponse;
import com.example.jobfinder.dto.application.ApplicationStatusUpdateRequest;
import com.example.jobfinder.dto.job.CandidateDetailResponse;
import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.statistic_admin.DailyApplicationCountResponse;
import com.example.jobfinder.dto.statistic_admin.MonthlyApplicationStatsResponse;
import com.example.jobfinder.dto.user.JobSeekerResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.ApplicationMapper;
import com.example.jobfinder.mapper.JobMapper;
import com.example.jobfinder.model.*;
import com.example.jobfinder.model.enums.ApplicationStatus;
import com.example.jobfinder.repository.*;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
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
import java.net.URL;

import org.apache.pdfbox.pdmodel.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationService {
    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

     final ApplicationRepository applicationRepository;
     final UserRepository userRepository;
     final JobRepository jobRepository;
     final JobMapper jobMapper;
     final ApplicationMapper applicationMapper;
     final UserDetailsRepository userDetailsRepository;
     final CloudinaryService cloudinaryService;
     final GeminiService geminiService;
     final ResumeSummaryRepository resumeSummaryRepository;
        final EmailService emailService;

    @Transactional
    public ApplicationResponse applyJob(ApplicationRequest request) throws IOException {
        log.debug("Processing apply job request: {}", request);


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHENTICATED.getErrorMessage());
        }
        String email = authentication.getName();
        log.debug("Authenticated email: {}", email);

        User jobSeeker = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND.getErrorMessage()));
        UserDetail userDetail = userDetailsRepository.findByUserId(jobSeeker.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND.getErrorMessage()));

        String role = jobSeeker.getRole().getName();
        if (!role.equals("JOB_SEEKER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ErrorCode.UNAUTHORIZED.getErrorMessage());
        }

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.JOB_NOT_FOUND.getErrorMessage()));

        Optional<Application> existingApplication = applicationRepository.findByJobSeekerIdAndJobId(jobSeeker.getId(), job.getId());
        if (existingApplication.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCode.APPLICATION_ALREADY_SUBMITTED.getErrorMessage());
        }

        MultipartFile resumeFile = request.getResume();
        String resumeUrl;

        Application application = new Application();
        application.setJobSeeker(jobSeeker);
        application.setJob(job);
        application.setStatus(ApplicationStatus.PENDING);
        application.setEmail(request.getEmail());
        application.setPhone(request.getPhone());
        if (resumeFile != null && !resumeFile.isEmpty()) {
            resumeUrl = cloudinaryService.uploadFile(resumeFile);
        } else {
            resumeUrl = userDetail.getResumeUrl();
        }
        application.setResume(resumeUrl);
        application.setCoverLetter(request.getCoverLetter());
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
                                                       Long employerId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.getJob().getEmployer().getId().equals(employerId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_APPLICATION_UPDATE);
        }

        ApplicationStatus newStatus;
        try {
            newStatus = ApplicationStatus.fromString(request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_APPLICATION_STATUS);
        }

        application.setStatus(newStatus);
        Application updatedApplication = applicationRepository.save(application);

        try {
            String jobSeekerEmail = updatedApplication.getJobSeeker().getEmail();
            String jobTitle = updatedApplication.getJob().getTitle();
            String employerCompanyName = updatedApplication.getJob().getEmployer().getUserDetail().getCompanyName();
            String statusDisplayName = newStatus.getValue();

            emailService.sendApplicationStatusUpdateEmail(
                    jobSeekerEmail,
                    jobTitle,
                    statusDisplayName,
                    employerCompanyName,
                    request.getEmployerMessage()
            );
        } catch (MessagingException e) {
            System.err.println("Failed to send application status update email: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error fetching data for application status update email or other issue: " + e.getMessage());
        }
        return applicationMapper.toApplicationResponse(updatedApplication);
    }

    @Transactional(readOnly = true) // Thêm chú thích này vì đây là một thao tác chỉ đọc
    public Page<ApplicationResponse> getApplicationsByJobSeekerId(Long userId, Pageable pageable) {

        User jobSeeker = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND.getErrorMessage()));

        if (!jobSeeker.getRole().getName().equals("JOB_SEEKER") && !jobSeeker.getRole().getName().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ErrorCode.UNAUTHORIZED.getErrorMessage());
        }

        // Lấy danh sách các đơn ứng tuyển của jobSeeker
        Page<Application> applicationsPage = applicationRepository.findByJobSeekerId(userId, pageable); // Điều chỉnh nếu tên phương thức trong repo là findByJobSeeker_Id

        // Chuyển đổi List<Application> sang List<ApplicationResponse>
        // SỬ DỤNG PHƯƠNG THỨC MAPPER MỚI: toApplicationResponseListWithoutJobSeeker
        List<ApplicationResponse> applicationResponses = applicationsPage.getContent().stream()
                .map(applicationMapper::toApplicationResponseWithoutJobSeeker)
                .collect(Collectors.toList());

        // Tạo một đối tượng Page<ApplicationResponse> mới
        return new PageImpl<>(applicationResponses, pageable, applicationsPage.getTotalElements());
    }

    public List<CandidateDetailResponse> getCandidatesDetailByJobId(Long jobId) {
        List<User> applicants = applicationRepository.findApplicantsWithDetailsByJobId(jobId);
        return applicants.stream().map(user -> {
            JobSeekerResponse jobSeekerResponse = null;
            Education education = user.getUserDetail().getEducation();
            Experience experience = user.getUserDetail().getExperience();
            Long applicationId = applicationRepository.findByJobSeekerIdAndJobId(user.getId(), jobId)
                    .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND))
                    .getId();
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

            return CandidateDetailResponse.builder()
                    .userId(user.getId())
                    .applicationId(applicationId)
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
        LocalDateTime startDateTime = startDate.atStartOfDay(); // Bắt đầu từ 00:00:00 của startDate
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // Kết thúc vào 23:59:59.999... của endDate

        List<Object[]> rawCounts = applicationRepository.countApplicationsByDateTimeRange(startDateTime, endDateTime);


        Map<LocalDate, Long> dailyCountsMap = rawCounts.stream()
                .collect(Collectors.toMap(

                        arr -> ((Date) arr[0]).toLocalDate(),
                        arr -> (Long) arr[1],
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        List<DailyApplicationCountResponse> dailyStats = new ArrayList<>();
        long totalApplications = 0;

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

    @Transactional(readOnly = true)
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
        if ("newest".equalsIgnoreCase(sortOrder)) {
            sort = Sort.by("appliedAt").descending();
        } else if ("latest".equalsIgnoreCase(sortOrder)) {
            sort = Sort.by("appliedAt").ascending();
        } else {
            sort = Sort.by("appliedAt").descending();
        }
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Application> applicationsPage = applicationRepository.getEmployerJobApplicationsForSpecificJob(
                employerId, jobId,
                name, minExperience, maxExperience,
                jobTypeId, educationId, jobLevelId,
                pageable // Pass the Pageable object
        );

        // --- 5. Map Entities to DTOs and Build PageResponse ---
        return buildPageResponse(applicationsPage);
    }

    @Transactional
    public String summarizeResumeWithGemini(Long applicationId) throws IOException { // IOException được ném nếu có lỗi từ PDFBox/Gemini
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));

        // 1. Kiểm tra xem bản tóm tắt đã tồn tại trong DB chưa
        Optional<ResumeSummary> existingSummary = resumeSummaryRepository.findByApplication(application);
        if (existingSummary.isPresent()) {
            log.info("Returning cached resume summary for application ID: {}", applicationId);
            return existingSummary.get().getSummaryContent(); // Trả về nội dung đã lưu
        }

        // 2. Nếu chưa có, tiến hành phân tích và tóm tắt
        String resumeUrl = application.getResume(); // Giả sử trường resumeUrl trong Application là 'resume'
        if (resumeUrl == null || resumeUrl.trim().isEmpty()) {
            throw new AppException(ErrorCode.RESUME_NOT_FOUND_FOR_APPLICATION);
        }

        String resumeContent = "";
        try {
            URL url = new URL(resumeUrl);
            PDDocument document = PDDocument.load(url.openStream());
            PDFTextStripper pdfStripper = new PDFTextStripper();
            resumeContent = pdfStripper.getText(document);
            document.close();
            log.info("Successfully extracted content from resume URL: {}", resumeUrl);
        } catch (IOException e) {
            log.error("Error reading resume content from URL {} using PDFBox: {}", resumeUrl, e.getMessage(), e);
            throw new AppException(ErrorCode.RESUME_PROCESSING_ERROR);
        } catch (Exception e) {
            log.error("An unexpected error occurred while processing resume URL {}: {}", resumeUrl, e.getMessage(), e);
            throw new AppException(ErrorCode.RESUME_PROCESSING_ERROR);
        }

        if (resumeContent.trim().isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_RESUME_CONTENT);
        }

        String prompt = """
            Bạn là một chuyên gia tóm tắt hồ sơ ứng viên.
            Hãy đọc kỹ và tóm tắt nội dung resume dưới đây một cách chi tiết nhưng súc tích, tập trung vào những điểm chính sau:
            - **Thông tin liên hệ cơ bản**: Tên, email, số điện thoại (nếu có).
            - **Mục tiêu nghề nghiệp/Tóm tắt bản thân**: Tóm tắt ngắn gọn nếu có.
            - **Kinh nghiệm làm việc**:
                - Liệt kê các vị trí công việc gần đây nhất (tối đa 3 vị trí).
                - Với mỗi vị trí, nêu tên công ty, chức danh, thời gian làm việc và 1-2 gạch đầu dòng mô tả các trách nhiệm chính hoặc thành tựu nổi bật nhất.
            - **Kỹ năng**:
                - Phân loại và liệt kê các kỹ năng chính (ví dụ: Ngôn ngữ lập trình, Frameworks, Cơ sở dữ liệu, Công cụ, Kỹ năng mềm).
                - Chỉ liệt kê các kỹ năng được đề cập rõ ràng trong resume.
            - **Học vấn**: Liệt kê bằng cấp cao nhất, tên trường và thời gian tốt nghiệp.
            - **Dự án/Hoạt động (nếu có)**: Tóm tắt 1-2 dự án hoặc hoạt động nổi bật, nêu rõ vai trò và kết quả chính.

            Đảm bảo tóm tắt bằng tiếng Việt, mạch lạc, chuyên nghiệp và không thêm thông tin suy diễn.
            Nếu một phần thông tin không có trong resume, hãy bỏ qua phần đó.

            --- Bắt đầu Resume ---
            """ + resumeContent + """
            --- Kết thúc Resume ---
            """;

        String resumeSummary = geminiService.getGeminiResponse(prompt);

        ResumeSummary newSummary = ResumeSummary.builder()
                .application(application)
                .summaryContent(resumeSummary)
                .createdAt(LocalDateTime.now()) // Đảm bảo tự động điền nếu không dùng @PrePersist
                .updatedAt(LocalDateTime.now()) // Đảm bảo tự động điền nếu không dùng @PrePersist
                .build();
        resumeSummaryRepository.save(newSummary);
        log.info("Saved new resume summary for application ID: {}", applicationId);

        return resumeSummary;
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationDetails(Long applicationId, Long currentUserId, String currentUserRole) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));

        // Kiểm tra phân quyền (giữ nguyên logic này)
        boolean authorized = false;
        if ("ADMIN".equals(currentUserRole)) {
            authorized = true;
        } else if ("JOB_SEEKER".equals(currentUserRole)) {
            if (application.getJobSeeker().getId().equals(currentUserId)) {
                authorized = true;
            }
        } else if ("EMPLOYER".equals(currentUserRole)) {
            if (application.getJob().getEmployer().getId().equals(currentUserId)) {
                authorized = true;
            }
        }

        if (!authorized) {
            log.warn("Unauthorized access attempt to application {}. User ID: {}, Role: {}", applicationId, currentUserId, currentUserRole);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Sử dụng MapStruct mapper để chuyển đổi Entity sang DTO
        return applicationMapper.toApplicationResponse(application);
    }

    private PageResponse<ApplicationResponse> buildPageResponse(Page<Application> applicationsPage) {
        List<ApplicationResponse> applicationResponses = applicationsPage.getContent().stream()
                .map(applicationMapper::toApplicationResponse) // Sử dụng ApplicationMapper
                .collect(Collectors.toList());

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
