package com.example.jobfinder.mapper;
import com.example.jobfinder.dto.SimpleNameCreationRequest;
import com.example.jobfinder.dto.SimpleNameResponse;
import com.example.jobfinder.dto.SimpleNameUpdateRequest;
import com.example.jobfinder.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(SimpleNameCreationRequest request);

    @Mapping(source = "name", target = "name")
    SimpleNameResponse toCategoryResponse(Category category);
    @Mapping(target = "id", ignore = true)
    void updateCategory(@MappingTarget Category category, SimpleNameUpdateRequest request);
}