// D:\Code-Window\JobFinderProject\be-jobfinder\trunglecode\src\main\java\com\example\jobfinder\mapper\SimpleNameMapper.java
package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.model.Category;
import com.example.jobfinder.model.JobLevel;
import com.example.jobfinder.model.JobType;
import com.example.jobfinder.model.Role; // <-- Import Role Entity

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SimpleNameMapper {
    SimpleNameResponse toSimpleNameResponse(Category category);
    SimpleNameResponse toSimpleNameResponse(JobLevel jobLevel);
    SimpleNameResponse toSimpleNameResponse(JobType jobType);
    SimpleNameResponse toSimpleNameResponse(Role role);
    // <-- Thêm phương thức này cho Role
}