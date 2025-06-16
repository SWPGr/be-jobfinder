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
    public Application applyJob(ApplicationRequest request) throws Exception {
        log.debug("Processing job application with request: {}", request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.debug("Authenticated email: {}", email);
        User jobSeeker = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        if (jobSeeker == null) {
            throw new Exception("User not found");
        }
        if (!jobSeeker.isVerified()) {
            throw new Exception("Please verify your email first");
        }

        String role = jobSeeker.getRole().getName();
        log.debug("User role: {}", role);
        if(!role.equals("JOB_SEEKER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only JOB_SEEKER can apply a job");
        }

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found: " + request.getJobId()));

        if(applicationRepository.findByJobSeekerIdAndJobId(jobSeeker.getId(), job.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already applied for this job");
        }

        Application application = new Application();
        application.setJobSeeker(jobSeeker);
        application.setJob(job);
        application.setStatus(ApplicationStatus.PENDING);
        application.setAppliedAt(LocalDateTime.now());

        //Gửi thông báo cho JobSeeker:
        String jobSeekerNotificationMessage = String.format("Bạn đã ứng tuyển thành công vào vị trí '%s' của công ty '%s'.",
                job.getTitle(), job.getEmployer().getEmail());
        notificationService.createNotification(jobSeeker.getId(), jobSeekerNotificationMessage);

        // Ghi thông báo cho Employer:
        String employerNotificationMessage = String.format("%s (%s) đã ứng tuyển vào vị trí '%s' của bạn.",
                jobSeeker.getEmail(), jobSeeker.getId(), job.getTitle());
        notificationService.createNotification(job.getEmployer().getId(), employerNotificationMessage);

        log.debug("Saving application for user: {} and job: {}", jobSeeker.getId(), job.getId());
        return applicationRepository.save(application);
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
