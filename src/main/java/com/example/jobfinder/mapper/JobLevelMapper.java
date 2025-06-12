package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.simple.SimpleNameCreationRequest;
import com.example.jobfinder.dto.simple.SimpleNameUpdateRequest;
import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.model.JobLevel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface JobLevelMapper {
    JobLevel toJobLevel(SimpleNameCreationRequest request);

    @Mapping(source = "name", target = "name")
    SimpleNameResponse toJobLevelResponse(JobLevel jobLevel);

    @Mapping(target = "id", ignore = true)
    void updateJobLevel(@MappingTarget JobLevel jobLevel, SimpleNameUpdateRequest request);
}