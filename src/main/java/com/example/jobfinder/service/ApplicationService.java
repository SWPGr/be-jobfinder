package com.example.jobfinder.service;

import com.example.jobfinder.dto.PageResponse;
import com.example.jobfinder.dto.application.ApplicationRequest;
import com.example.jobfinder.dto.application.ApplicationResponse;
import com.example.jobfinder.dto.application.ApplicationStatusUpdateRequest;
import com.example.jobfinder.dto.application.CandidateFilterRequest;
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
    Logger log = LoggerFactory.getLogger(ApplicationService.class);
    ApplicationRepository applicationRepository;
    UserRepository userRepository;
    JobRepository jobRepository;
    ApplicationMapper applicationMapper;
    UserDetailsRepository userDetailsRepository;
    CloudinaryService cloudinaryService;
    GeminiService geminiService;
    ResumeSummaryRepository resumeSummaryRepository;
    EmailService emailService;
    NotificationService notificationService;

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
        Application createdApplication = applicationRepository.save(application);

        // Xây dựng nội dung thông báo
        String jobSeekerName = application.getJobSeeker().getUserDetail() != null
                ? application.getJobSeeker().getUserDetail().getFullName()
                : null;

        Long employerId = job.getEmployer().getId();
        String employerName = job.getEmployer().getUserDetail() != null
                ? job.getEmployer().getUserDetail().getCompanyName()
                : "Nhà tuyển dụng";
        String notificationMessage = String.format(
                "%s đã nộp đơn vào công việc '%s' của bạn.",
                jobSeekerName != null && !jobSeekerName.isEmpty() ? jobSeekerName : jobSeeker.getEmail(),
                job.getTitle()
        );
        try {
            notificationService.createNotification(employerId, notificationMessage);
            log.info("Notification sent to employer {} (ID: {}) for new application on job '{}' (ID: {}).",
                    employerName, employerId, job.getTitle(), job.getId());
        } catch (Exception e) {
            log.error("Failed to create notification for employer {} (ID: {}) for new application: {}",
                    employerName, employerId, e.getMessage());
        }
        return applicationMapper.toApplicationResponse(createdApplication);
    }

    public boolean isJobOwnedByEmployer(Long jobId, Long employerId) {
        return jobRepository.existsByIdAndEmployerId(jobId, employerId);
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

        if (newStatus == ApplicationStatus.ACCEPTED) {
            Job job = application.getJob();
            Integer currentVacancy = job.getVacancy();
            if (currentVacancy != null && currentVacancy > 0) {
                job.setVacancy(currentVacancy - 1);
            } else {
                throw new AppException(ErrorCode.JOB_NO_VACANCY);
            }
        }

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

    @Transactional
    public void rejectRemainingApplicationsForJob(Long jobId, Long employerId, String employerMessage) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        if (!job.getEmployer().getId().equals(employerId)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        List<Application> applicationsToReject = applicationRepository.findByJobAndStatusIsNot(job, ApplicationStatus.ACCEPTED);
        for (Application app : applicationsToReject) {
            if (app.getStatus() != ApplicationStatus.REJECTED) {
                app.setStatus(ApplicationStatus.REJECTED);
                applicationRepository.save(app);
            }
            try {
                String jobSeekerEmail = app.getJobSeeker().getEmail();
                String jobTitle = app.getJob().getTitle();
                String employerCompanyName = app.getJob().getEmployer().getUserDetail().getCompanyName();
                String statusDisplayName = ApplicationStatus.REJECTED.getValue();

                emailService.sendApplicationStatusUpdateEmail(
                        jobSeekerEmail,
                        jobTitle,
                        statusDisplayName,
                        employerCompanyName,
                        employerMessage
                );
            } catch (MessagingException e) {
                System.err.println("Failed to send rejection email for application " + app.getId() + ": " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error processing email for application " + app.getId() + ": " + e.getMessage());
            }
        }
    }

    @Transactional(readOnly = true) // Thêm chú thích này vì đây là một thao tác chỉ đọc
    public Page<ApplicationResponse> getApplicationsByJobSeekerId(Long userId, Pageable pageable) {

        User jobSeeker = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND.getErrorMessage()));

        if (!jobSeeker.getRole().getName().equals("JOB_SEEKER") && !jobSeeker.getRole().getName().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ErrorCode.UNAUTHORIZED.getErrorMessage());
        }
        Page<Application> applicationsPage = applicationRepository.findByJobSeekerId(userId, pageable);
        List<ApplicationResponse> applicationResponses = applicationsPage.getContent().stream()
                .map(applicationMapper::toApplicationResponseWithoutJobSeeker)
                .collect(Collectors.toList());
        return new PageImpl<>(applicationResponses, pageable, applicationsPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<CandidateDetailResponse> getCandidatesDetailByJobId(Long jobId,
                                                                    CandidateFilterRequest filterRequest,
                                                                    Pageable pageable) {

        Page<User> applicantsPage = applicationRepository.findApplicantsWithDetailsByJobIdAndFilters(
                jobId,
                filterRequest.getFullName(),
                filterRequest.getEmail(),
                filterRequest.getLocation(),
                filterRequest.getExperienceName(),
                filterRequest.getEducationName(),
                filterRequest.getIsPremium(),
                filterRequest.getStatus(),
                pageable
        );
        List<CandidateDetailResponse> candidateDetails = applicantsPage.getContent().stream().map(user -> {
            JobSeekerResponse jobSeekerResponse = null;
            Education education = null;
            Experience experience = null;

            if (user.getUserDetail() != null) {
                education = user.getUserDetail().getEducation();
                experience = user.getUserDetail().getExperience();

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
            } else {
                jobSeekerResponse = JobSeekerResponse.builder().build();
            }

            Long applicationId = applicationRepository.findByJobSeekerIdAndJobId(user.getId(), jobId)
                    .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND))
                    .getId();

            ApplicationStatus status = applicationRepository.findByJobSeekerIdAndJobId(user.getId(), jobId)
                    .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND))
                    .getStatus();

            return CandidateDetailResponse.builder()
                    .userId(user.getId())
                    .applicationId(applicationId)
                    .fullname(user.getUserDetail() != null ? user.getUserDetail().getFullName() : user.getEmail())
                    .email(user.getEmail())
                    .role(user.getRole().getName())
                    .status(status)
                    .seekerDetail(jobSeekerResponse)
                    .build();
        }).collect(Collectors.toList());

        return new PageImpl<>(candidateDetails, pageable, applicantsPage.getTotalElements());
    }

    public MonthlyApplicationStatsResponse getApplicationsLast3Months() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(3);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
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

        String userEmail = authentication.getName();
        User currentEmployer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Employer not found for authenticated user: " + userEmail));

        if (!currentEmployer.getRole().getName().equals("EMPLOYER")) {
            throw new IllegalStateException("Access denied: User is not an employer."); // Only employers can access
        }

        Long employerId = currentEmployer.getId();
        boolean isJobOwnedByEmployer = jobRepository.existsByIdAndEmployerId(jobId, employerId);
        if (!isJobOwnedByEmployer) {
            throw new AppException(ErrorCode.JOB_ALREADY_EXISTS);
        }
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
                pageable
        );

        return buildPageResponse(applicationsPage);
    }

    @Transactional
    public String summarizeResumeWithGemini(Long applicationId) throws IOException {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));
        String resumeSummaryContent;
        Optional<ResumeSummary> existingSummary = resumeSummaryRepository.findByApplication(application);

        if (existingSummary.isPresent()) {
            resumeSummaryContent = existingSummary.get().getSummaryContent();
            log.info("Returning cached resume summary for application ID: {}", applicationId);
        } else {
            // Nếu chưa có, tiến hành đọc PDF và gọi AI để tóm tắt
            String resumeUrl = application.getResume();
            if (resumeUrl == null || resumeUrl.trim().isEmpty()) {
                throw new AppException(ErrorCode.RESUME_NOT_FOUND_FOR_APPLICATION);
            }

            String resumeRawContent = "";
            try {
                URL url = new URL(resumeUrl);
                PDDocument document = PDDocument.load(url.openStream());
                PDFTextStripper pdfStripper = new PDFTextStripper();
                resumeRawContent = pdfStripper.getText(document);
                document.close();
                log.info("Successfully extracted content from resume URL: {}", resumeUrl);
            } catch (IOException e) {
                log.error("Error reading resume content from URL {} using PDFBox: {}", resumeUrl, e.getMessage(), e);
                throw new AppException(ErrorCode.RESUME_PROCESSING_ERROR);
            } catch (Exception e) {
                log.error("An unexpected error occurred while processing resume URL {}: {}", resumeUrl, e.getMessage(), e);
                throw new AppException(ErrorCode.RESUME_PROCESSING_ERROR);
            }

            if (resumeRawContent.trim().isEmpty()) {
                throw new AppException(ErrorCode.EMPTY_RESUME_CONTENT);
            }

            String promptForSummary = """
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
                    """ + resumeRawContent + """
                    --- Kết thúc Resume ---
                    """;

            resumeSummaryContent = geminiService.getGeminiResponse(promptForSummary);

            ResumeSummary newSummary = ResumeSummary.builder()
                    .application(application)
                    .summaryContent(resumeSummaryContent)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            resumeSummaryRepository.save(newSummary); // ✅ LƯU BẢN TÓM TẮT RESUME VÀO DB
            log.info("Saved new resume summary for application ID: {}", applicationId);
        }

        Job job = application.getJob();
        if (job == null) {
            log.warn("Application ID {} does not have an associated Job. Cannot perform job fit analysis.", applicationId);
            return resumeSummaryContent + "\n\n--- Phân tích sự phù hợp công việc ---\nKhông thể phân tích sự phù hợp do thông tin công việc không có sẵn.";
        }
        String jobTitle = job.getTitle() != null ? job.getTitle() : "Không có tiêu đề.";
        String jobDescription = job.getDescription() != null ? job.getDescription() : "Không có mô tả công việc.";
        String jobLocation = job.getLocation() != null ? job.getLocation() : "Không có địa điểm.";
        String salaryRange = "";
        if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
            salaryRange = "Từ " + job.getSalaryMin() + " đến " + job.getSalaryMax() + " USD.";
        } else if (job.getSalaryMin() != null) {
            salaryRange = "Tối thiểu " + job.getSalaryMin() + " USD.";
        } else if (job.getSalaryMax() != null) {
            salaryRange = "Tối đa " + job.getSalaryMax() + " USD.";
        } else {
            salaryRange = "Không rõ.";
        }
        String responsibility = job.getResponsibility() != null ? job.getResponsibility() : "Không có mô tả trách nhiệm.";
        String expiredDate = job.getExpiredDate() != null ? job.getExpiredDate().toString() : "Không rõ ngày hết hạn.";
        String vacancy = job.getVacancy() != null ? String.valueOf(job.getVacancy()) : "Không rõ số lượng.";
        String jobCategory = job.getCategory() != null ? job.getCategory().getName() : "Không rõ.";
        String jobLevel = job.getJobLevel() != null ? job.getJobLevel().getName() : "Không rõ.";
        String jobType = job.getJobType() != null ? job.getJobType().getName() : "Không rõ.";
        String requiredEducation = job.getEducation() != null ? job.getEducation().getName() : "Không có yêu cầu học vấn.";
        String requiredExperience = job.getExperience() != null ? job.getExperience().getName() : "Không có yêu cầu kinh nghiệm.";


        // --- BƯỚC 3: Xây dựng Prompt cho AI để so sánh và tạo ra một chuỗi tổng hợp ---
        String promptForComparison = String.format("""
                        Bạn là một chuyên gia phân tích hồ sơ ứng viên và so sánh với yêu cầu công việc.
                        
                        **Thông tin ứng viên (được tóm tắt từ Resume):**
                        %s
                        
                        **Thông tin công việc đang ứng tuyển:**
                        - **Tiêu đề:** %s
                        - **Mô tả:** %s
                        - **Yêu cầu:** %s
                        - **Kỹ năng cần thiết:** %s
                        - **Lợi ích:** %s
                        
                        ---
                        
                        **Yêu cầu:**
                        1.  **Đánh giá mức độ phù hợp về kỹ năng và kinh nghiệm** của ứng viên với các yêu cầu của công việc. Nêu rõ các điểm mạnh (skills/experience matching) và các kỹ năng/kinh nghiệm còn thiếu (gaps).
                        2.  **Đánh giá tiềm năng phát triển và sự phù hợp về mục tiêu nghề nghiệp** của ứng viên với tính chất công việc.
                        3.  **Cung cấp một điểm số tổng thể** cho mức độ phù hợp của ứng viên với công việc (từ 0 đến 100).
                        4.  **Tổng hợp toàn bộ đánh giá này thành một đoạn văn bản mạch lạc, dễ hiểu**, bằng tiếng Việt, có độ dài khoảng 3-5 câu.
                        
                        Ví dụ định dạng đầu ra mong muốn:
                        "Ứng viên [Tên Ứng Viên] cho vị trí [Tên Công Việc]: Phù hợp [Mức độ phù hợp, ví dụ: Cao/Trung bình/Thấp] ([Điểm số]/100). Ứng viên nổi bật với [Điểm mạnh]. Tuy nhiên, cần cải thiện/thiếu [Kỹ năng/kinh nghiệm còn thiếu]. Mục tiêu nghề nghiệp của ứng viên có [Mô tả về sự phù hợp mục tiêu]."
                        ---
                        Bắt đầu phân tích:
                        """,
                resumeSummaryContent,
                jobTitle, jobDescription, jobLocation, salaryRange, responsibility, expiredDate, vacancy,
                jobCategory, jobLevel, jobType, requiredEducation, requiredExperience
        );

        String jobFitAnalysis = geminiService.getGeminiResponse(promptForComparison);
        log.info("Job Fit Analysis from Gemini for application ID {}: {}", applicationId, jobFitAnalysis);

        // --- BƯỚC 4: Kết hợp bản tóm tắt resume và phân tích sự phù hợp vào một chuỗi duy nhất ---
        String finalOutput = "--- Tóm tắt Resume ---\n" +
                resumeSummaryContent +
                "\n\n--- Phân tích sự phù hợp với Công việc (Job Fit Analysis) ---\n" +
                jobFitAnalysis;

        return finalOutput;
    }

    @Transactional(readOnly = true)
    public boolean isJobOwnedByEmployerByApplicationId(Long applicationId, Long employerId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));
        Job job = application.getJob();
        if (job == null) {
            return false;
        }
        return job.getEmployer().getId().equals(employerId);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationDetails(Long applicationId, Long currentUserId, String currentUserRole) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));

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

        return applicationMapper.toApplicationResponse(application);
    }

    private PageResponse<ApplicationResponse> buildPageResponse(Page<Application> applicationsPage) {
        List<ApplicationResponse> applicationResponses = applicationsPage.getContent().stream()
                .map(applicationMapper::toApplicationResponse)
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
