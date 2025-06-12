// D:\Code-Window\JobFinderProject\be-jobfinder\trunglecode\src\main\java\com\example\jobfinder\service\EducationService.java
package com.example.jobfinder.service;

import com.example.jobfinder.dto.simple.SimpleNameCreationRequest;
import com.example.jobfinder.dto.simple.SimpleNameUpdateRequest;
import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.exception.ErrorCode; // Cần import ErrorCode
import com.example.jobfinder.mapper.EducationMapper; // Cần import EducationMapper
import com.example.jobfinder.model.Education; // Cần import Education entity
import com.example.jobfinder.repository.EducationRepository; // Cần import EducationRepository
import org.springframework.stereotype.Service;

@Service // Đánh dấu đây là một Spring Service bean
public class EducationService extends BaseNameService<Education> {

    private final EducationMapper educationMapper;

    // Spring sẽ tự động inject EducationRepository và EducationMapper vào constructor này
    public EducationService(EducationRepository educationRepository, EducationMapper educationMapper) {
        super(educationRepository); // Gọi constructor của lớp cha BaseNameService
        this.educationMapper = educationMapper;
    }

    @Override
    protected Education createEntity(SimpleNameCreationRequest request) {
        return educationMapper.toEducation(request);
    }

    @Override
    protected void updateEntity(Education entity, SimpleNameUpdateRequest request) {
        educationMapper.updateEducation(entity, request);
    }

    @Override
    protected SimpleNameResponse toResponse(Education entity) {
        return educationMapper.toEducationResponse(entity);
    }

    @Override
    protected ErrorCode getExistedErrorCode() {
        return ErrorCode.EDUCATION_NAME_EXISTED; // Sẽ thêm vào ErrorCode
    }

    @Override
    protected ErrorCode getNotFoundErrorCode() {
        return ErrorCode.EDUCATION_NOT_FOUND; // Sẽ thêm vào ErrorCode
    }

    @Override
    protected String getEntityNameForLog() {
        return "Education";
    }
}