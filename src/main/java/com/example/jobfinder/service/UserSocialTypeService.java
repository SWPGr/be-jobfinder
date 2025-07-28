// src/main/java/com/example/jobfinder/service/UserSocialTypeService.java
package com.example.jobfinder.service;

import com.example.jobfinder.dto.user_social_type.UserSocialTypeRequest;
import com.example.jobfinder.dto.user_social_type.UserSocialTypeResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.UserSocialTypeMapper;
import com.example.jobfinder.model.SocialType;
import com.example.jobfinder.model.User;
import com.example.jobfinder.model.UserDetail;
import com.example.jobfinder.model.UserSocialType;
import com.example.jobfinder.repository.SocialTypeRepository;
import com.example.jobfinder.repository.UserDetailsRepository;
import com.example.jobfinder.repository.UserRepository;
import com.example.jobfinder.repository.UserSocialTypeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserSocialTypeService {

    UserSocialTypeRepository userSocialTypeRepository;
    UserRepository userRepository;
    UserDetailsRepository userDetailRepository;
    SocialTypeRepository socialTypeRepository;
    UserSocialTypeMapper userSocialTypeMapper;

    // Helper method to get authenticated user entity
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    // Helper method to get UserDetail of the authenticated user
    private UserDetail getAuthenticatedUserDetail() {
        User user = getAuthenticatedUser();
        return userDetailRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
    }

    @Transactional
    public UserSocialTypeResponse createUserSocialLink(UserSocialTypeRequest request) {
        UserDetail userDetail = getAuthenticatedUserDetail();

        SocialType socialType = socialTypeRepository.findById(request.getSocialTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.SOCIAL_TYPE_NOT_FOUND)); // Ném lỗi nếu không tìm thấy

        // 2. Kiểm tra xem người dùng đã có liên kết cho loại mạng xã hội này chưa
        if (userSocialTypeRepository.existsByUserDetail_IdAndSocialType_Id(userDetail.getId(), socialType.getId())) {
            throw new AppException(ErrorCode.USER_SOCIAL_TYPE_ALREADY_EXISTS);
        }

        // 3. Ánh xạ request sang entity và thiết lập các mối quan hệ
        UserSocialType userSocialType = userSocialTypeMapper.toUserSocialType(request);
        userSocialType.setUserDetail(userDetail);
        userSocialType.setSocialType(socialType);

        UserSocialType savedLink = userSocialTypeRepository.save(userSocialType);
        return userSocialTypeMapper.toUserSocialTypeResponse(savedLink);
    }

    public List<UserSocialTypeResponse> getMySocialLinks() {
        UserDetail userDetail = getAuthenticatedUserDetail();
        List<UserSocialType> socialLinks = userSocialTypeRepository.findByUserDetail_Id(userDetail.getId());
        return userSocialTypeMapper.toUserSocialTypeResponseList(socialLinks);
    }

    public UserSocialTypeResponse getSocialLinkById(Long id) {
        UserDetail userDetail = getAuthenticatedUserDetail();
        UserSocialType socialLink = userSocialTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_SOCIAL_TYPE_NOT_FOUND));

        // Đảm bảo người dùng đang đăng nhập là chủ sở hữu của liên kết
        if (!socialLink.getUserDetail().getId().equals(userDetail.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_USER_SOCIAL_ACTION);
        }
        return userSocialTypeMapper.toUserSocialTypeResponse(socialLink);
    }

    @Transactional
    public UserSocialTypeResponse updateUserSocialLink(Long id, UserSocialTypeRequest request) {
        UserDetail userDetail = getAuthenticatedUserDetail();
        UserSocialType existingLink = userSocialTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_SOCIAL_TYPE_NOT_FOUND));

        // Đảm bảo người dùng đang đăng nhập là chủ sở hữu của liên kết
        if (!existingLink.getUserDetail().getId().equals(userDetail.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_USER_SOCIAL_ACTION);
        }

        // 1. Tìm SocialType mới dựa trên newSocialTypeId
        Long newSocialTypeId = request.getSocialTypeId();
        if (newSocialTypeId == null) {
            throw new AppException(ErrorCode.SOCIAL_TYPE_NOT_FOUND);
        }
        SocialType newSocialType = socialTypeRepository.findById(newSocialTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.SOCIAL_TYPE_NOT_FOUND));

        // 2. Kiểm tra trùng lặp nếu loại mạng xã hội thay đổi
        // Tránh trường hợp người dùng đã có liên kết cho newSocialType này
        if (!existingLink.getSocialType().getId().equals(newSocialType.getId()) &&
                userSocialTypeRepository.existsByUserDetail_IdAndSocialType_Id(userDetail.getId(), newSocialType.getId())) {
            throw new AppException(ErrorCode.USER_SOCIAL_TYPE_ALREADY_EXISTS);
        }

        // 3. Cập nhật entity bằng mapper
        userSocialTypeMapper.updateUserSocialType(existingLink, request);
        existingLink.setSocialType(newSocialType); // Gán SocialType mới

        // 4. Lưu và trả về
        UserSocialType updatedLink = userSocialTypeRepository.save(existingLink);
        return userSocialTypeMapper.toUserSocialTypeResponse(updatedLink);
    }

    @Transactional
    public void deleteUserSocialLink(Long id) { //Dùng cho người dùng tự xóa social link của mình, không validate
        UserSocialType socialLink = userSocialTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_SOCIAL_TYPE_NOT_FOUND));
        userSocialTypeRepository.delete(socialLink);
    }
}