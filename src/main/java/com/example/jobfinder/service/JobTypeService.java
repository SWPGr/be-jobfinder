package com.example.jobfinder.service;

import com.example.jobfinder.dto.simple.SimpleNameCreationRequest;
import com.example.jobfinder.dto.simple.SimpleNameUpdateRequest;
import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.JobTypeMapper;
import com.example.jobfinder.model.JobType;
import com.example.jobfinder.repository.JobTypeRepository;
import org.springframework.stereotype.Service;

@Service
public class JobTypeService extends BaseNameService<JobType> {

    private final JobTypeMapper jobTypeMapper;

    public JobTypeService(JobTypeRepository jobTypeRepository, JobTypeMapper jobTypeMapper) {
        super(jobTypeRepository);
        this.jobTypeMapper = jobTypeMapper;
    }

    @Override
    protected JobType createEntity(SimpleNameCreationRequest request) {
        return jobTypeMapper.toJobType(request);
    }

    @Override
    protected void updateEntity(JobType entity, SimpleNameUpdateRequest request) {
        jobTypeMapper.updateJobType(entity, request);
    }

    @Override
    protected SimpleNameResponse toResponse(JobType entity) {
        return jobTypeMapper.toJobTypeResponse(entity);
    }

    @Override
    protected ErrorCode getExistedErrorCode() {
        return ErrorCode.JOB_TYPE_NAME_EXISTED;
    }

    @Override
    protected ErrorCode getNotFoundErrorCode() {
        return ErrorCode.JOB_TYPE_NOT_FOUND;
    }

    @Override
    protected String getEntityNameForLog() {
        return "Job Type";
    }
}