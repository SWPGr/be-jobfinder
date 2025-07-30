package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.simple.SimpleNameCreationRequest;
import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.dto.simple.SimpleNameUpdateRequest;
import com.example.jobfinder.model.Experience;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ExperienceMapper {
    Experience toExperience(SimpleNameCreationRequest request);

    @Mapping(source = "name", target = "name")
    SimpleNameResponse toExperienceResponse(Experience experience);

    // Bỏ qua trường 'id' khi cập nhật để không ghi đè ID hiện có
    @Mapping(target = "id", ignore = true)
    // Ánh xạ các thuộc tính từ request vào entity hiện có
    void updateExperience(@MappingTarget Experience experience, SimpleNameUpdateRequest request);
}