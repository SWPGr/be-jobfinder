// D:\Code-Window\JobFinderProject\be-jobfinder\trunglecode\src\main\java\com\example\jobfinder\mapper\UserMapper.java
package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.user.UserResponse; // Import UserResponse
import com.example.jobfinder.model.User; // Import User entity

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {SimpleNameMapper.class}) // <-- Rất quan trọng: UserMapper sử dụng SimpleNameMapper
// để ánh xạ trường 'role'
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);


    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "verified", source = "verified")
    @Mapping(target = "role", source = "role")
    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponseList(List<User> users);
}