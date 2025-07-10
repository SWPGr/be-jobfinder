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


    @Mapping(target = "employer", source = "employer") // Ánh xạ từ Job.employer sang JobResponse.employer
    @Mapping(target = "category", source = "category")
    @Mapping(target = "jobLevel", source = "jobLevel")
    @Mapping(target = "jobType", source = "jobType")
    @Mapping(target = "jobApplicationCounts", ignore = true)
    JobResponse toJobResponse(Job job);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "location", target = "location")
    @Mapping(source = "isSave", target = "isSave")
    @Mapping(target = "employer", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "jobLevel", ignore = true)
    @Mapping(target = "jobType", ignore = true)
    @Mapping(target = "education", ignore = true)
    @Mapping(target = "salaryMin", ignore = true)
    @Mapping(target = "salaryMax", ignore = true)
    @Mapping(target = "responsibility", ignore = true)
    @Mapping(target = "expiredDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    JobResponse toJobResponse(JobDocument jobDocument);

    List<JobResponse> toJobResponseList(List<Job> jobs);

    default SimpleNameResponse map(Long id) {
        return id == null ? null : new SimpleNameResponse(id, null);
    }

    default SimpleNameResponse map(String name) {
        return name == null ? null : new SimpleNameResponse(null, name);
    }
}