package com.example.jobfinder.mapper;

// import com.example.jobfinder.dto.user_detail.UserDetailResponse; // Nếu bạn có UserDetailResponse DTO
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserDetailMapper {
    UserDetailMapper INSTANCE = Mappers.getMapper(UserDetailMapper.class);
}