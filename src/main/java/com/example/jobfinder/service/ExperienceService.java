// src/main/java/com/example/jobfinder/service/ExperienceService.java
package com.example.jobfinder.service;

import com.example.jobfinder.dto.simple.SimpleNameCreationRequest;
import com.example.jobfinder.dto.simple.SimpleNameUpdateRequest;
import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.Experience;
import com.example.jobfinder.repository.ExperienceRepository;
import com.example.jobfinder.repository.BaseNameRepository; // Import BaseNameRepository
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service class for managing Experience entities.
 * Extends BaseNameService to provide generic CRUD operations for Experience.
 */
@Service
@Slf4j
public class ExperienceService extends BaseNameService<Experience> {

    public ExperienceService(ExperienceRepository repository) {
        super(repository);
    }

    @Override
    protected Experience createEntity(SimpleNameCreationRequest request) {
        return Experience.builder()
                .name(request.getName())
                .build();
    }

    @Override
    protected void updateEntity(Experience entity, SimpleNameUpdateRequest request) {
        entity.setName(request.getName());
    }

    @Override
    protected SimpleNameResponse toResponse(Experience entity) {
        return SimpleNameResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    @Override
    protected ErrorCode getExistedErrorCode() {
        return ErrorCode.EXPERIENCE_EXISTED;
    }

    @Override
    protected ErrorCode getNotFoundErrorCode() {
        return ErrorCode.EXPERIENCE_NOT_FOUND;
    }

    @Override
    protected String getEntityNameForLog() {
        return "Experience";
    }
}