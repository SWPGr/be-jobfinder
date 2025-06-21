package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.simple.SimpleNameCreationRequest;
import com.example.jobfinder.dto.simple.SimpleNameUpdateRequest;
import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.model.JobType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")

public interface JobTypeMapper {
    JobType toJobType(SimpleNameCreationRequest request);

    @Mapping(source = "name", target = "name")
    SimpleNameResponse toJobTypeResponse(JobType jobType);

    @Mapping(target = "id", ignore = true)
    void updateJobType(@MappingTarget JobType jobType, SimpleNameUpdateRequest request);
}