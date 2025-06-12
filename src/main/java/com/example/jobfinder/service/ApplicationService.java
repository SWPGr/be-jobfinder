package com.example.jobfinder.service;

import com.example.jobfinder.dto.ApplicationRequest;
import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.user.UserResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.JobMapper;
import com.example.jobfinder.mapper.UserMapper;
import com.example.jobfinder.model.Application;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.User;
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
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
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

    public Application applyJob(ApplicationRequest request) throws Exception {
        log.debug("Processing job application with request: {}", request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.debug("Authenticated email: {}", email);
        User jobSeeker = userRepository.findByEmail(email);
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
        application.setStatus("PENDING");
        application.setAppliedAt(LocalDateTime.now());

        log.debug("Saving application for user: {} and job: {}", jobSeeker.getId(), job.getId());
        return applicationRepository.save(application);
    }

    public List<JobResponse> getAppliedJobsByUserId(Long userId) {

        // Tìm tất cả các đơn ứng tuyển của người dùng này
        List<Application> applications = applicationRepository.findByJobSeekerId(userId);

        // Lấy danh sách các Job từ các đơn ứng tuyển
        List<Job> appliedJobs = applications.stream()
                .map(Application::getJob)
                .collect(Collectors.toList());

        // Ánh xạ danh sách Job Entity sang danh sách JobResponse DTO
        return jobMapper.toJobResponseList(appliedJobs);
    }

    public List<UserResponse> getCandidatesByJobId(Long jobId) {
        // Kiểm tra Job có tồn tại không
        jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        // Lấy tất cả các Application cho Job này
        List<Application> applications = applicationRepository.findByJob_Id(jobId);

        // Chuyển đổi danh sách Application thành danh sách Job Seekers (User)
        // và sau đó map sang UserResponse DTO
        List<User> jobSeekers = applications.stream()
                .map(Application::getJobSeeker)
                .collect(Collectors.toList());

        // Sử dụng UserMapper để chuyển đổi List<User> sang List<UserResponse>
        return userMapper.toUserResponseList(jobSeekers); // <-- Cần có toUserResponseList trong UserMapper
    }
}
