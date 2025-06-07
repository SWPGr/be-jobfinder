// D:\Code-Window\JobFinderProject\be-jobfinder\trunglecode\src\main\java\com\example\jobfinder\mapper\JobMapper.java
package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.job.JobCreationRequest;
import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.job.JobUpdateRequest;
import com.example.jobfinder.model.Job;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {SimpleNameMapper.class, UserMapper.class}) // Đảm bảo các mapper này tồn tại và được import đúng
public interface JobMapper {

    /**
     * Mapper từ JobCreationRequest DTO sang Job Entity.
     * CHỈ BỎ QUA CÁC TRƯỜNG MÀ DB TỰ TẠO HOẶC JPA TỰ QUẢN LÝ (ví dụ: id, createdAt, updatedAt).
     * CÁC TRƯỜNG NHƯ 'employer', 'category', 'jobLevel', 'jobType' SẼ ĐƯỢC GÁN TRONG SERVICE LAYER
     * VÀ KHÔNG CÓ TRONG DTO NGUỒN, VÌ VẬY KHÔNG CẦN DÙNG `@Mapping(target = "...", ignore = true)` CHO CHÚNG.
     */
    @Mapping(target = "id", ignore = true) // ID được DB tạo
    Job toJob(JobCreationRequest request);

    /**
     * Mapper để cập nhật Job Entity từ JobUpdateRequest DTO.
     * Tương tự, CHỈ BỎ QUA ID VÀ CÁC TIMESTAMP KHÔNG NÊN CẬP NHẬT TRỰC TIẾP.
     * Các đối tượng quan hệ (employer, category...) sẽ được cập nhật ở Service Layer.
     */
    @Mapping(target = "id", ignore = true) // Không cập nhật ID
    void updateJob(@MappingTarget Job job, JobUpdateRequest request);

    /**
     * Ánh xạ từ Job Entity sang JobResponse DTO.
     * Dựa vào `uses` và tên trường để MapStruct tự động gọi các mapper con.
     */
    JobResponse toJobResponse(Job job);

    List<JobResponse> toJobResponseList(List<Job> jobs);
}