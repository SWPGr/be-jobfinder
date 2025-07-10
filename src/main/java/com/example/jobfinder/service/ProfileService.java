package com.example.jobfinder.service;

import com.example.jobfinder.dto.auth.ProfileRequest;
import com.example.jobfinder.dto.auth.ProfileResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.*;
import com.example.jobfinder.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional; // Import Optional

@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final EducationRepository educationRepository;
    private final CloudinaryService cloudinaryService;
    private final ExperienceRepository experienceRepository;
    private final OrganizationRepository organizationRepository;

    // Constants for role names - better practice
    private static final String ROLE_JOB_SEEKER = "JOB_SEEKER";
    private static final String ROLE_EMPLOYER = "EMPLOYER";

    public ProfileService(UserRepository userRepository, UserDetailsRepository userDetailsRepository,
                          EducationRepository educationRepository, CloudinaryService cloudinaryService,
                          ExperienceRepository experienceRepository, OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.educationRepository = educationRepository;
        this.cloudinaryService = cloudinaryService;
        this.experienceRepository = experienceRepository;
        this.organizationRepository = organizationRepository;
    }

    public ProfileResponse updateProfile(ProfileRequest request) throws Exception { // Ném ra Exception vẫn được, nhưng tốt hơn là AppException
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // Sử dụng AppException cho lỗi User not found
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Không cần kiểm tra if (user == null) sau orElseThrow

        // UserDetail phải tồn tại, nếu không ném lỗi
        UserDetail userDetail = userDetailsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));
        // Không cần kiểm tra if (userDetail == null)

        String roleName = user.getRole().getName();

        if (roleName.equals(ROLE_JOB_SEEKER)) { // Sử dụng hằng số
            // Kiểm tra null cho từng trường để chỉ cập nhật nếu giá trị được cung cấp
            if (request.getEducation() != null) {
                Long educationId = request.getEducation().getId();
                // Tìm kiếm Education theo ID
                Optional<Education> educationOptional = educationRepository.findById(educationId);

                if (educationOptional.isPresent()) {
                    // Nếu tìm thấy, set vào userDetail
                    userDetail.setEducation(educationOptional.get());
                } else {
                    // Nếu không tìm thấy, ném ngoại lệ
                    throw new AppException(ErrorCode.EDUCATION_NOT_FOUND);
                }
            }

            Optional.ofNullable(request.getLocation()).ifPresent(userDetail::setLocation);
            Optional.ofNullable(request.getFullName()).ifPresent(userDetail::setFullName);
            Optional.ofNullable(request.getPhone()).ifPresent(userDetail::setPhone);

            if (request.getUserExperience() != null) {
                Long experienceId = request.getUserExperience().getId(); // Lấy ID từ request

                // Tìm Experience theo ID, nếu không tìm thấy thì ném lỗi ngay lập tức
                Experience experience = experienceRepository.findById(experienceId)
                        .orElseThrow(() -> new AppException(ErrorCode.EXPERIENCE_NOT_FOUND));

                // Nếu tìm thấy, gán vào userDetail
                userDetail.setExperience(experience);
            }
<<<<<<< HEAD
            if (request.getResumeUrl() != null && !request.getResumeUrl().isEmpty()) {
                String resume = cloudinaryService.uploadFile(request.getResumeUrl());
                userDetail.setResumeUrl(resume);
            }
        }else if(roleName.equals("EMPLOYER")) {
            if(request.getCompanyName() == null || request.getCompanyName().isEmpty()) {
                throw new Exception("Company name is required for employer");
            }
            userDetail.setCompanyName(request.getCompanyName());
            userDetail.setLocation(request.getLocation());
            userDetail.setDescription(request.getDescription());
            userDetail.setWebsite(request.getWebsite());
            if (request.getOrganization() != null) {
                Organization organization = organizationRepository.findById(request.getOrganization().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_NOT_FOUND));
                userDetail.setOrganization(organization);
            }

        }else {
            throw new Exception("Invalid role");
=======

            Optional.ofNullable(request.getResumeUrl()).ifPresent(userDetail::setResumeUrl);

        } else if (roleName.equals(ROLE_EMPLOYER)) { // Sử dụng hằng số
            if (request.getCompanyName() == null || request.getCompanyName().isEmpty()) {
                throw new AppException(ErrorCode.COMPANY_NAME_REQUIRED); // Thêm ErrorCode này
            }

            // Xử lý upload banner nếu có
            if (request.getBanner() != null && !request.getBanner().isEmpty()) {
                String bannerUrl = cloudinaryService.uploadFile(request.getBanner());
                userDetail.setBanner(bannerUrl);
            } else if (request.getBanner() != null && request.getBanner().isEmpty()) {
                // Nếu client gửi chuỗi rỗng để xóa banner
                userDetail.setBanner(null);
            }


            Optional.ofNullable(request.getCompanyName()).ifPresent(userDetail::setCompanyName);
            Optional.ofNullable(request.getLocation()).ifPresent(userDetail::setLocation);
            Optional.ofNullable(request.getDescription()).ifPresent(userDetail::setDescription);
            Optional.ofNullable(request.getWebsite()).ifPresent(userDetail::setWebsite);
            Optional.ofNullable(request.getTeamSize()).ifPresent(userDetail::setTeamSize);
            Optional.ofNullable(request.getYearOfEstablishment()).ifPresent(userDetail::setYearOfEstablishment);
            Optional.ofNullable(request.getMapLocation()).ifPresent(userDetail::setMapLocation);
            Optional.ofNullable(request.getOrganizationType()).ifPresent(userDetail::setOrganizationType);

        } else {
            throw new AppException(ErrorCode.INVALID_ROLE); // Thêm ErrorCode này
>>>>>>> 9125635c534fa49d4e82a6d4b822f01e31aa7529
        }

        // Xử lý upload avatar (chung cho cả hai vai trò)
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            String avatarUrl = cloudinaryService.uploadFile(request.getAvatar());
            userDetail.setAvatarUrl(avatarUrl);
        } else if (request.getAvatar() != null && request.getAvatar().isEmpty()) {
            // Nếu client gửi chuỗi rỗng để xóa avatar
            userDetail.setAvatarUrl(null);
        }

        userDetailsRepository.save(userDetail);
        return mapToProfileResponse(user, userDetail);
    }

    public List<ProfileResponse> listCurrentUserProfiles() throws Exception { // Nên đổi thành throws AppException
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Bỏ kiểm tra if (currentUser == null)

        if (currentUser.getVerified() == 0) {
            throw new AppException(ErrorCode.USER_NOT_VERIFIED); // Thêm ErrorCode này
        }

        UserDetail userDetail = userDetailsRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        // Bỏ kiểm tra if (userDetail == null) và trả về Collections.emptyList()
        // Vì nếu đã đến đây mà không tìm thấy UserDetail thì đã ném lỗi PROFILE_NOT_FOUND

        ProfileResponse response = mapToProfileResponse(currentUser, userDetail);
        return Collections.singletonList(response);
    }

    private ProfileResponse mapToProfileResponse(User user, UserDetail userDetail) {
        ProfileResponse response = new ProfileResponse();
        response.setEmail(user.getEmail());
        response.setRoleName(user.getRole().getName());
        response.setLocation(userDetail.getLocation());
        if (userDetail.getEducation() != null) {
            response.setEducation(userDetail.getEducation());
        }
        response.setFullName(userDetail.getFullName());
        response.setPhone(userDetail.getPhone());
        if (userDetail.getExperience() != null) {
            response.setExperience(userDetail.getExperience());
        }
        response.setResumeUrl(userDetail.getResumeUrl());
        response.setCompanyName(userDetail.getCompanyName());
        response.setDescription(userDetail.getDescription());
        response.setWebsite(userDetail.getWebsite());
        response.setAvatarUrl(userDetail.getAvatarUrl());
<<<<<<< HEAD
        if (userDetail.getOrganization() != null) {
            response.setOrganizationId(userDetail.getOrganization().getId());
            response.setOrganizationType(userDetail.getOrganization().getName());
        }
=======
        response.setBanner(userDetail.getBanner()); // Có thể bạn muốn thêm banner vào đây
        response.setTeamSize(userDetail.getTeamSize()); // Và các trường khác
        response.setYearOfEstablishment(userDetail.getYearOfEstablishment());
        response.setMapLocation(userDetail.getMapLocation());
        response.setOrganizationType(userDetail.getOrganizationType());

>>>>>>> 9125635c534fa49d4e82a6d4b822f01e31aa7529
        return response;
    }
}