package com.example.jobfinder.service;

import com.example.jobfinder.dto.application.ApplicationRequest;
import com.example.jobfinder.dto.application.ApplicationResponse;
import com.example.jobfinder.dto.application.ApplicationStatusUpdateRequest;
import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.user.UserResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.ApplicationMapper;
import com.example.jobfinder.mapper.JobMapper;
import com.example.jobfinder.mapper.UserMapper;
import com.example.jobfinder.model.Application;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.User;
import com.example.jobfinder.model.enums.ApplicationStatus;
import com.example.jobfinder.repository.ApplicationRepository;
import com.example.jobfinder.repository.JobRepository;
import com.example.jobfinder.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

        List<Job> appliedJobs = applications.stream()
                .map(Application::getJob)
                .collect(Collectors.toList());

        return jobMapper.toJobResponseList(appliedJobs);
    }

    public List<UserResponse> getCandidatesByJobId(Long jobId) {
        jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        // Lấy tất cả các Application cho Job này
        List<Application> applications = applicationRepository.findByJob_Id(jobId);

        List<User> jobSeekers = applications.stream()
                .map(Application::getJobSeeker)
                .collect(Collectors.toList());

        return userMapper.toUserResponseList(jobSeekers);
    }
}
