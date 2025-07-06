// D:\Code-Window\JobFinderProject\be-jobfinder\trunglecode\src\main\java\com\example\jobfinder\mapper\JobMapper.java
package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.job.JobCreationRequest;
import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.job.JobUpdateRequest;
import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.model.Job;

import com.example.jobfinder.model.JobDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {SimpleNameMapper.class, UserMapper.class})

public interface JobMapper {

    @Mapping(target = "id", ignore = true)
    Job toJob(JobCreationRequest request);

    @Mapping(target = "id", ignore = true)
    void updateJob(@MappingTarget Job job, JobUpdateRequest request);

    JobResponse toJobResponse(Job job);

    @Mapping(target = "employer", ignore = true)
    JobResponse toJobResponse(JobDocument jobDocument);

    List<JobResponse> toJobResponseList(List<Job> jobs);

    default SimpleNameResponse map(Long id) {
        return id == null ? null : new SimpleNameResponse(id, null);
    }

    default SimpleNameResponse map(String name) {
        return name == null ? null : new SimpleNameResponse(null, name);
    }
}