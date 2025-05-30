package com.example.jobfinder.mapper;


import com.example.jobfinder.dto.JobCreationRequest;
import com.example.jobfinder.dto.JobResponse;
import com.example.jobfinder.dto.JobUpdateRequest;
import com.example.jobfinder.model.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
@Mapper(componentModel = "spring") // Đánh dấu đây là một MapStruct Mapper và được Spring quản lý
public interface JobMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employer", ignore = true) // Bỏ qua trường 'employer' vì nó sẽ được tìm và gán thủ công
    @Mapping(target = "category", ignore = true) // Bỏ qua trường 'category' vì nó sẽ được tìm và gán thủ công
    @Mapping(target = "createdAt", ignore = true) // Bỏ qua trường 'createdAt' nếu dùng @CreatedDate hoặc set thủ công
    Job toJob(JobCreationRequest request);


    @Mapping(target = "id", ignore = true) // Không cập nhật ID của Job
    @Mapping(target = "employer", ignore = true) // Không cập nhật 'employer' qua request DTO
    @Mapping(target = "category", ignore = true) // Không cập nhật 'category' qua request DTO
    @Mapping(target = "createdAt", ignore = true) // Không cập nhật 'createdAt'
    void updateJob(@MappingTarget Job job, JobUpdateRequest request);

    @Mapping(source = "employer.id", target = "employerId")
    JobResponse toJobResponse(Job job);


    List<JobResponse> toJobResponseList(List<Job> jobs);
}
