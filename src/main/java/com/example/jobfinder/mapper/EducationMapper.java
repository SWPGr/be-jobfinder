// D:\Code-Window\JobFinderProject\be-jobfinder\trunglecode\src\main\java\com\example\jobfinder\mapper\EducationMapper.java
package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.SimpleNameCreationRequest;
import com.example.jobfinder.dto.SimpleNameUpdateRequest;
import com.example.jobfinder.dto.SimpleNameResponse;
import com.example.jobfinder.model.Education;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EducationMapper {
    Education toEducation(SimpleNameCreationRequest request);

    @Mapping(source = "name", target = "name")
    SimpleNameResponse toEducationResponse(Education education);

    // Bỏ qua trường 'id' khi cập nhật để không ghi đè ID hiện có
    @Mapping(target = "id", ignore = true)
    // Ánh xạ các thuộc tính từ request vào entity hiện có
    void updateEducation(@MappingTarget Education education, SimpleNameUpdateRequest request);
}