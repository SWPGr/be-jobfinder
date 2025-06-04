package com.example.jobfinder.service;

import com.example.jobfinder.dto.SimpleNameCreationRequest;
import com.example.jobfinder.dto.SimpleNameUpdateRequest;
import com.example.jobfinder.dto.SimpleNameResponse;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.JobLevelMapper;
import com.example.jobfinder.model.JobLevel;
import com.example.jobfinder.repository.JobLevelRepository;
import org.springframework.stereotype.Service;

@Service
public class JobLevelService extends BaseNameService<JobLevel> {

    private final JobLevelMapper jobLevelMapper;

    public JobLevelService(JobLevelRepository jobLevelRepository, JobLevelMapper jobLevelMapper) {
        super(jobLevelRepository);
        this.jobLevelMapper = jobLevelMapper;
    }

    @Override
    protected JobLevel createEntity(SimpleNameCreationRequest request) {
        return jobLevelMapper.toJobLevel(request);
    }

    @Override
    protected void updateEntity(JobLevel entity, SimpleNameUpdateRequest request) {
        jobLevelMapper.updateJobLevel(entity, request);
    }

    @Override
    protected SimpleNameResponse toResponse(JobLevel entity) {
        return jobLevelMapper.toJobLevelResponse(entity);
    }

    @Override
    protected ErrorCode getExistedErrorCode() {
        return ErrorCode.JOB_LEVEL_NAME_EXISTED;
    }

    @Override
    protected ErrorCode getNotFoundErrorCode() {
        return ErrorCode.JOB_LEVEL_NOT_FOUND;
    }

    @Override
    protected String getEntityNameForLog() {
        return "Job Level";
    }
}