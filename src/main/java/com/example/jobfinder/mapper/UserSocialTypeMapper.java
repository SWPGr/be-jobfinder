// src/main/java/com/example/jobfinder/mapper/UserSocialTypeMapper.java
package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.user_social_type.UserSocialTypeRequest;
import com.example.jobfinder.dto.user_social_type.UserSocialTypeResponse;
import com.example.jobfinder.model.UserSocialType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserSocialTypeMapper {

    @Mapping(target = "socialType", ignore = true)
    @Mapping(target = "userDetail", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserSocialType toUserSocialType(UserSocialTypeRequest request);

    @Mapping(source = "socialType.name", target = "socialTypeName")
    UserSocialTypeResponse toUserSocialTypeResponse(UserSocialType entity);

    List<UserSocialTypeResponse> toUserSocialTypeResponseList(List<UserSocialType> entities);

    @Mapping(target = "socialType", ignore = true) // SocialType sẽ được cập nhật thủ công
    @Mapping(target = "userDetail", ignore = true) // userDetail không đổi
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateUserSocialType(@MappingTarget UserSocialType entity, UserSocialTypeRequest request);
}