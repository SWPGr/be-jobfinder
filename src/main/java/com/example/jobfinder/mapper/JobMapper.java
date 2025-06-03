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

    // Mapper từ JobCreationRequest DTO sang Job Entity
    @Mapping(target = "id", ignore = true) // ID sẽ được DB tạo
    @Mapping(target = "employer", ignore = true) // Employer sẽ được gán thủ công trong service
    @Mapping(target = "category", ignore = true) // Category sẽ được tìm và gán thủ công trong service
    @Mapping(target = "jobLevel", ignore = true) // JobLevel sẽ được tìm và gán thủ công trong service
    @Mapping(target = "jobType", ignore = true) // JobType sẽ được tìm và gán thủ công trong service
    @Mapping(target = "createdAt", ignore = true) // createdAt sẽ được @CreatedDate xử lý hoặc set thủ công
    Job toJob(JobCreationRequest request);

    // Mapper để cập nhật Job Entity từ JobUpdateRequest DTO
    @Mapping(target = "id", ignore = true) // Không cập nhật ID
    @Mapping(target = "employer", ignore = true) // Không cập nhật employer
    @Mapping(target = "category", ignore = true) // Category sẽ được tìm và gán thủ công trong service
    @Mapping(target = "jobLevel", ignore = true) // JobLevel sẽ được tìm và gán thủ công trong service
    @Mapping(target = "jobType", ignore = true) // JobType sẽ được tìm và gán thủ công trong service
    @Mapping(target = "createdAt", ignore = true) // Không cập nhật createdAt
    void updateJob(@MappingTarget Job job, JobUpdateRequest request);

    // Mapper từ Job Entity sang JobResponse DTO
    // Điều này sẽ ánh xạ các ID của các đối tượng liên quan (Employer, Category, JobLevel, JobType)
    // sang các trường DTO tương ứng của chúng.
    @Mapping(source = "employer.id", target = "employerId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "jobLevel.id", target = "jobLevelId")
    @Mapping(source = "jobType.id", target = "jobTypeId")
    @Mapping(source = "category.name", target = "categoryName") // Thêm ánh xạ tên category
    @Mapping(source = "jobLevel.name", target = "jobLevelName") // Thêm ánh xạ tên jobLevel
    @Mapping(source = "jobType.name", target = "jobTypeName") // Thêm ánh xạ tên jobType
    JobResponse toJobResponse(Job job);

    // Mapper cho danh sách Job sang danh sách JobResponse
    List<JobResponse> toJobResponseList(List<Job> jobs);
}
