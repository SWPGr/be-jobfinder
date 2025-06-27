package com.example.jobfinder.service;

import com.example.jobfinder.dto.job.SavedJobRequest;
import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.job.SavedJobResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.JobMapper;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.SavedJob;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.JobRepository;
import com.example.jobfinder.repository.SavedJobRepository;
import com.example.jobfinder.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SavedJobService {

    private static final Logger log = LoggerFactory.getLogger(SavedJobService.class);

    SavedJobRepository savedJobRepository;
    UserRepository userRepository;
    JobRepository jobRepository;
    JobMapper  jobMapper;

    public List<JobResponse> getSavedJobsByJobSeekerId(Long jobSeekerId) { // <-- Thay đổi kiểu trả về thành List<JobResponse>
        userRepository.findById(jobSeekerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<SavedJob> savedJobs = savedJobRepository.findByJobSeeker_Id(jobSeekerId);

        List<Job> jobs = savedJobs.stream()
                .map(SavedJob::getJob)
                .collect(Collectors.toList());

        // Sử dụng JobMapper để chuyển đổi List<Job> sang List<JobResponse>
        return jobMapper.toJobResponseList(jobs); // <-- Đã sửa lại để dùng mapper
    }


    public SavedJobResponse savedJob(SavedJobRequest request) {
        log.debug("Processing save job request: {}", request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        String email = authentication.getName();
        log.debug("Authenticated email: {}", email);
        User jobSeeker = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        if (jobSeeker == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        String role = jobSeeker.getRole().getName();
        log.debug("Role: {}", role);
        if (!role.equals("JOB_SEEKER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only job seekers can save jobs");
        }

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found" + request.getJobId()));

        if (savedJobRepository.findByJobSeekerIdAndJobId(jobSeeker.getId(), job.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already saved this job");
        }

        SavedJob savedJob = new SavedJob();
        savedJob.setJobSeeker(jobSeeker);
        savedJob.setJob(job);
        savedJob.setSavedAt(LocalDateTime.now());

        savedJobRepository.save(savedJob);
        return mapToSavedJobResponse(savedJob);
    }

    private SavedJobResponse mapToSavedJobResponse(SavedJob saved) {
        return SavedJobResponse.builder()
                .id(saved.getId())
                .jobId(saved.getJob().getId())
                .jobTitle(saved.getJob().getTitle())
                .jobSeekerEmail(saved.getJobSeeker().getEmail())
                .savedAt(saved.getSavedAt())
                .build();
    }

    public void unSaveJob(Long jobId) {
        log.debug("Processing save job request: {}", jobId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.debug("Authenticated email: {}", email);
        User jobSeeker = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String role = jobSeeker.getRole().getName();
        if (!role.equals("JOB_SEEKER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only job seekers can unsave jobs");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job not found" + jobId));

        SavedJob savedJob = savedJobRepository.findByJobSeekerIdAndJobId(jobSeeker.getId(), job.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have not saved this job"));

        savedJobRepository.delete(savedJob);
        log.debug("unsaved job for user: {} and job: {}", jobSeeker.getId(), job.getId());
    }

//    @Transactional // Đảm bảo phương thức này chạy trong một transaction
//    public SavedJobResponse saveJob(SavedJobRequest request) { // Đổi tên phương thức để rõ ràng hơn
//        log.debug("Processing save job request: {}", request);
//
//        // 1. Xác thực người dùng và vai trò
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || authentication.getName() == null) {
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
//        }
//        String email = authentication.getName();
//        log.debug("Authenticated email: {}", email);
//
//        User jobSeeker = userRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException(email));
//
//        String role = jobSeeker.getRole().getName(); // Role đã được tải EAGERLY (như bạn đã sửa trước đó)
//        log.debug("Role: {}", role);
//        if (!role.equals("JOB_SEEKER")) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only job seekers can save jobs");
//        }
//
//        // 2. Tìm kiếm Job
//        Job job = jobRepository.findById(request.getJobId())
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found with ID: " + request.getJobId()));
//
//        // 3. Kiểm tra trùng lặp
//        if (savedJobRepository.findByJobSeekerIdAndJobId(jobSeeker.getId(), job.getId()).isPresent()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already saved this job");
//        }
//
//        // 4. Tạo và lưu SavedJob entity
//        SavedJob savedJob = new SavedJob();
//        savedJob.setJobSeeker(jobSeeker);
//        savedJob.setJob(job);
//        // savedAt được tự động điền bởi @PrePersist trong entity nếu bạn đã có,
//        // nếu không, hãy uncomment dòng dưới:
//        // savedJob.setSavedAt(LocalDateTime.now());
//
//        SavedJob createdSavedJob = savedJobRepository.save(savedJob);
//
//        // 5. Chuyển đổi SavedJob entity sang SavedJobResponse DTO để trả về
//        // Quan trọng: Vì @Transactional, các đối tượng jobSeeker và job
//        // vẫn đang trong trạng thái managed và có thể được truy cập để lấy dữ liệu.
//        return SavedJobResponse.builder()
//                .id(createdSavedJob.getId())
//                .savedAt(createdSavedJob.getSavedAt())
//                .jobSeekerId(createdSavedJob.getJobSeeker().getId())
//                .jobSeekerEmail(createdSavedJob.getJobSeeker().getEmail()) // Lấy email từ đối tượng User đã tải
//                .jobId(createdSavedJob.getJob().getId())
//                .jobTitle(createdSavedJob.getJob().getTitle()) // Lấy title từ đối tượng Job đã tải
//                .build();
//    }
}
