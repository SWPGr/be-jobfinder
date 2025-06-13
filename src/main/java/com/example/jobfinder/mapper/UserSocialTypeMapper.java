package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.social_type.UserSocialTypeRequest;
import com.example.jobfinder.dto.social_type.UserSocialTypeResponse;
import com.example.jobfinder.model.UserSocialType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {SocialTypeMapper.class, UserDetailMapper.class}) // <-- Cần SocialTypeMapper và UserDetailMapper
public interface UserSocialTypeMapper {
    UserSocialTypeMapper INSTANCE = Mappers.getMapper(UserSocialTypeMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userDetail", ignore = true)   // Sẽ được set thủ công trong service
    @Mapping(target = "socialType", ignore = true)   // Sẽ được set thủ công trong service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserSocialType toUserSocialType(UserSocialTypeRequest request);

    @Mapping(source = "socialType", target = "socialType") // Ánh xạ SocialType entity sang SocialTypeResponse DTO
        // @Mapping(source = "userDetail", target = "userDetail") // Tùy chọn nếu bạn muốn trả về UserDetailResponse
    UserSocialTypeResponse toUserSocialTypeResponse(UserSocialType userSocialType);

    List<UserSocialTypeResponse> toUserSocialTypeResponseList(List<UserSocialType> userSocialTypes);
}