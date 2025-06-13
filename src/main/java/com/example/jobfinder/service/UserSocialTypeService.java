package com.example.jobfinder.service;

import com.example.jobfinder.dto.social_type.UserSocialTypeRequest;
import com.example.jobfinder.dto.social_type.UserSocialTypeResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.UserSocialTypeMapper;
import com.example.jobfinder.model.SocialType;
import com.example.jobfinder.model.User; // Để lấy User chính
import com.example.jobfinder.model.UserDetails;
import com.example.jobfinder.model.UserSocialType;
import com.example.jobfinder.repository.SocialTypeRepository;
import com.example.jobfinder.repository.UserDetailsRepository; // <-- Cần UserDetailRepository
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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserSocialTypeService {

    UserSocialTypeRepository userSocialTypeRepository;
    UserRepository userRepository; // Để lấy User từ Authentication
    UserDetailsRepository userDetailRepository; // <-- Cần UserDetailRepository để tìm UserDetail
    SocialTypeRepository socialTypeRepository; // Để tìm SocialType
    UserSocialTypeMapper userSocialTypeMapper;

    // Helper method to get authenticated user entity
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)); // Hoặc UNAUTHENTICATED
    }

    // Helper method to get UserDetail of the authenticated user
    private UserDetails getAuthenticatedUserDetail() {
        User user = getAuthenticatedUser();
        return userDetailRepository.findByUser(user) // <-- Giả định UserDetailRepository có phương thức này
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND)); // UserDetail not found for this user
    }

    /**
     * Tạo một liên kết mạng xã hội mới cho người dùng đang đăng nhập.
     * @param request DTO chứa socialTypeId và URL.
     * @return UserSocialTypeResponse của liên kết đã tạo.
     */
    @Transactional
    public UserSocialTypeResponse createUserSocialLink(UserSocialTypeRequest request) {
        UserDetails userDetail = getAuthenticatedUserDetail();

        SocialType socialType = socialTypeRepository.findById(request.getSocialTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.SOCIAL_TYPE_NOT_FOUND));

        if (userSocialTypeRepository.existsByUserDetail_IdAndSocialType_Id(userDetail.getId(), request.getSocialTypeId())) {
            throw new AppException(ErrorCode.USER_SOCIAL_TYPE_ALREADY_EXISTS);
        }

        UserSocialType userSocialType = userSocialTypeMapper.toUserSocialType(request);
        userSocialType.setUserDetail(userDetail);
        userSocialType.setSocialType(socialType);

        UserSocialType savedLink = userSocialTypeRepository.save(userSocialType);
        return userSocialTypeMapper.toUserSocialTypeResponse(savedLink);
    }

    public List<UserSocialTypeResponse> getMySocialLinks() {
        UserDetails userDetail = getAuthenticatedUserDetail();
        List<UserSocialType> socialLinks = userSocialTypeRepository.findByUserDetail_Id(userDetail.getId());
        return userSocialTypeMapper.toUserSocialTypeResponseList(socialLinks);
    }

    public UserSocialTypeResponse getSocialLinkById(Long id) {
        UserDetails userDetail = getAuthenticatedUserDetail(); // Đảm bảo người dùng đã xác thực
        UserSocialType socialLink = userSocialTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_SOCIAL_TYPE_NOT_FOUND));

        // Kiểm tra quyền: Người dùng đang đăng nhập phải là chủ sở hữu của liên kết này
        if (!socialLink.getUserDetail().getId().equals(userDetail.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_USER_SOCIAL_ACTION);
        }
        return userSocialTypeMapper.toUserSocialTypeResponse(socialLink);
    }

    @Transactional
    public UserSocialTypeResponse updateUserSocialLink(Long id, UserSocialTypeRequest request) {
        UserDetails userDetail = getAuthenticatedUserDetail();
        UserSocialType existingLink = userSocialTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_SOCIAL_TYPE_NOT_FOUND));

        if (!existingLink.getUserDetail().getId().equals(userDetail.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_USER_SOCIAL_ACTION);
        }

        SocialType newSocialType = socialTypeRepository.findById(request.getSocialTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.SOCIAL_TYPE_NOT_FOUND));

        if (!existingLink.getSocialType().getId().equals(request.getSocialTypeId()) &&
                userSocialTypeRepository.existsByUserDetail_IdAndSocialType_Id(userDetail.getId(), request.getSocialTypeId())) {
            throw new AppException(ErrorCode.USER_SOCIAL_TYPE_ALREADY_EXISTS);
        }

        existingLink.setSocialType(newSocialType);
        existingLink.setUrl(request.getUrl());

        UserSocialType updatedLink = userSocialTypeRepository.save(existingLink);
        return userSocialTypeMapper.toUserSocialTypeResponse(updatedLink);
    }

    @Transactional
    public void deleteUserSocialLink(Long id) {
        User authenticatedUser = getAuthenticatedUser();
        UserDetails userDetail = userDetailRepository.findByUser(authenticatedUser)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        UserSocialType socialLink = userSocialTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_SOCIAL_TYPE_NOT_FOUND));

        if (!socialLink.getUserDetail().getId().equals(userDetail.getId()) &&
                !authenticatedUser.getRole().equals("ADMIN")) {
            throw new AppException(ErrorCode.UNAUTHORIZED_USER_SOCIAL_ACTION);
        }

        userSocialTypeRepository.delete(socialLink);
    }
}