package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.simple.SimpleNameCreationRequest;
import com.example.jobfinder.dto.simple.SimpleNameUpdateRequest;
import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.model.SocialType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")

public interface SocialTypeMapper {
    SocialTypeMapper INSTANCE = Mappers.getMapper(SocialTypeMapper.class);

    SocialType toSocialType(SimpleNameCreationRequest request);


    @Mapping(source = "name", target = "name") // Ánh xạ trường 'name' của entity vào response
    SimpleNameResponse toSimpleNameResponse(SocialType socialType);

    // Cập nhật SocialType từ SimpleNameUpdateRequest
    @Mapping(target = "id", ignore = true) // ID không cập nhật
    @Mapping(source = "name", target = "name")
    void updateSocialType(@MappingTarget SocialType socialType, SimpleNameUpdateRequest request);
}