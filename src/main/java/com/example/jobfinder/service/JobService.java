package com.example.jobfinder.service;


import com.example.jobfinder.dto.JobCreationRequest;
import com.example.jobfinder.dto.JobResponse;
import com.example.jobfinder.dto.JobUpdateRequest;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.JobMapper;
import com.example.jobfinder.model.Category;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.CategoryRepository;
import com.example.jobfinder.repository.JobRepository;
import com.example.jobfinder.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobService {
    JobRepository jobRepository;
    JobMapper jobMapper;

    UserRepository userRepository;
    public Job createJob(JobCreationRequest jobCreationRequest) {

        User employer = userRepository.findById(jobCreationRequest.getEmployer().getId())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYER_NOT_FOUND));

        if (jobRepository.existsById(jobCreationRequest.getId())) {
            throw new AppException(ErrorCode.JOB_EXIST);
        }

        if (!"EMPLOYER".equals(employer.getRole().getName()) && !"COMPANY_ADMIN".equals(employer.getRole().getName())) {
            throw new AppException(ErrorCode.UNAUTHORIZED); // Ném lỗi nếu người dùng không có quyền
        }

        Job newJob = jobMapper.toJob(jobCreationRequest);
        return jobRepository.save(newJob);
    }

    public JobResponse updateJob(Long jobId, JobUpdateRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        jobMapper.updateJob(job, request);

        if (request.getEmployer().getId() != null && !request.getEmployer().getId().equals(job.getEmployer().getId())) {
            User newEmployer = userRepository.findById(request.getEmployer().getId())
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

    public List<JobResponse> getAllJobs() {
        return jobRepository.findAll()
                .stream()
                .map(jobMapper::toJobResponse)
                .collect(Collectors.toList());
    }

    public JobResponse getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        return jobMapper.toJobResponse(job);
    }
}
