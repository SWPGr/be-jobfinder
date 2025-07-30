package com.example.jobfinder.service;

import com.example.jobfinder.dto.auth.ProfileRequest;
import com.example.jobfinder.dto.auth.ProfileResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.*;
import com.example.jobfinder.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileService {
    UserRepository userRepository;
    UserDetailsRepository userDetailsRepository;
    EducationRepository educationRepository;
    CloudinaryService cloudinaryService;
    ExperienceRepository experienceRepository;
    OrganizationRepository organizationRepository;
    CategoryRepository categoryRepository;
    AICompanyAnalysisService aiCompanyAnalysisService;
    JobseekerAnalysisService jobseekerAnalysisService;
    JobseekerAnalysisRepository jobseekerAnalysisRepository;

     static String ROLE_JOB_SEEKER = "JOB_SEEKER";
     static String ROLE_EMPLOYER = "EMPLOYER";


    public ProfileResponse updateProfile(ProfileRequest request) throws Exception { // Ném ra Exception vẫn được, nhưng tốt hơn là AppException
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        boolean resumeUpdated = false;

        boolean resumeUrlUpdatedOrCleared = false;

        // Sử dụng AppException cho lỗi User not found
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        UserDetail userDetail = userDetailsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        String roleName = user.getRole().getName();

        if (roleName.equals(ROLE_JOB_SEEKER)) {

            if (request.getEducation() != null) {
                Education education = educationRepository.findById(request.getEducation().getId())
                        .orElseThrow(() -> new Exception("invalid education id"));
                userDetail.setEducation(education);
            }

            Optional.ofNullable(request.getLocation()).ifPresent(userDetail::setLocation);
            Optional.ofNullable(request.getFullName()).ifPresent(userDetail::setFullName);
            Optional.ofNullable(request.getPhone()).ifPresent(userDetail::setPhone);

            if (request.getUserExperience() != null) {
                Experience experience = experienceRepository.findById(request.getUserExperience().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.EXPERIENCE_NOT_FOUND));
                userDetail.setExperience(experience);
            }

            if (request.getResumeUrl() != null) {
                if (!request.getResumeUrl().isEmpty()) { // Có URL mới
                    String newResumeUrl = cloudinaryService.uploadFile(request.getResumeUrl());
                    if (!newResumeUrl.equals(userDetail.getResumeUrl())) { // Chỉ cập nhật nếu URL thay đổi
                        userDetail.setResumeUrl(newResumeUrl);
                        resumeUrlUpdatedOrCleared = true;
                        log.info("Resume URL updated for user: {}", user.getId());
                    }
                } else { // Client gửi chuỗi rỗng để xóa resume
                    if (userDetail.getResumeUrl() != null) { // Chỉ xóa nếu hiện có resume
                        userDetail.setResumeUrl(null);
                        resumeUrlUpdatedOrCleared = true;
                        log.info("Resume URL cleared for user: {}", user.getId());
                    }
                }
            }
        } else if (roleName.equals(ROLE_EMPLOYER)) {
            if (request.getCompanyName() == null || request.getCompanyName().isEmpty()) {
                throw new AppException(ErrorCode.COMPANY_NAME_REQUIRED);
            }

            if (request.getBanner() != null && !request.getBanner().isEmpty()) {
                String bannerUrl = cloudinaryService.uploadFile(request.getBanner());
                userDetail.setBanner(bannerUrl);
            } else if (request.getBanner() != null && request.getBanner().isEmpty()) {
                userDetail.setBanner(null);
            }

          if (request.getOrganization() != null) {
                Organization organization = organizationRepository.findById(request.getOrganization().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_NOT_FOUND));
                userDetail.setOrganization(organization);
            }

          if (request.getCategory() != null) {
              Category category = categoryRepository.findById(request.getOrganization().getId())
                      .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
              userDetail.setCategory(category);
          }
            Optional.ofNullable(request.getCompanyName()).ifPresent(userDetail::setCompanyName);
            Optional.ofNullable(request.getLocation()).ifPresent(userDetail::setLocation);
            Optional.ofNullable(request.getDescription()).ifPresent(userDetail::setDescription);
            Optional.ofNullable(request.getWebsite()).ifPresent(userDetail::setWebsite);
            Optional.ofNullable(request.getTeamSize()).ifPresent(userDetail::setTeamSize);
            Optional.ofNullable(request.getYearOfEstablishment()).ifPresent(userDetail::setYearOfEstablishment);
            Optional.ofNullable(request.getMapLocation()).ifPresent(userDetail::setMapLocation);
            Optional.ofNullable(request.getCompanyVision()).ifPresent(userDetail::setCompanyVision);
            Optional.ofNullable(request.getPhone()).ifPresent(userDetail::setPhone);

        } else {
            throw new AppException(ErrorCode.INVALID_ROLE);
        }
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            String avatarUrl = cloudinaryService.uploadFile(request.getAvatar());
            userDetail.setAvatarUrl(avatarUrl);
        } else if (request.getAvatar() != null && request.getAvatar().isEmpty()) {
            userDetail.setAvatarUrl(null);
        }

        UserDetail savedUserDetail = userDetailsRepository.save(userDetail);

        try {
            if (savedUserDetail.getCompanyName() != null && !savedUserDetail.getCompanyName().isEmpty() &&
                    savedUserDetail.getDescription() != null && !savedUserDetail.getDescription().isEmpty()) {
                aiCompanyAnalysisService.analyzeAndSaveCompanyProfile(savedUserDetail);
            } else {
                log.warn("Company profile for UserDetail ID {} is incomplete. Skipping AI analysis.", savedUserDetail.getId());
            }
        } catch (AppException e) {
            log.error("Failed to perform AI analysis for company profile {}: {}", savedUserDetail.getId(), e.getMessage(), e);
            // Tùy chọn: ném lại lỗi hoặc chỉ log nếu bạn không muốn lỗi AI làm gián đoạn update profile
        }

        if (roleName.equals(ROLE_JOB_SEEKER)) {
            boolean shouldAnalyzeResume = false;

            if (resumeUrlUpdatedOrCleared) {
                shouldAnalyzeResume = true;
                log.info("Resume URL for job seeker ID {} was updated/cleared. Will trigger AI analysis.", user.getId());
            }
            // Điều kiện 2: Resume hiện có và chưa có bản phân tích AI
            else if (savedUserDetail.getResumeUrl() != null && !savedUserDetail.getResumeUrl().isEmpty()) {
                boolean hasExistingAnalysis = jobseekerAnalysisRepository.findByUserDetail(savedUserDetail).isPresent();
                if (!hasExistingAnalysis) {
                    shouldAnalyzeResume = true;
                    log.info("Job seeker ID {} has a resume but no existing analysis. Triggering AI analysis.", user.getId());
                }
            }

            if (shouldAnalyzeResume) {
                if (savedUserDetail.getResumeUrl() != null && !savedUserDetail.getResumeUrl().isEmpty()) {
                    try {
                        jobseekerAnalysisService.analyzeAndSaveJobseekerResume(savedUserDetail);
                        log.info("Successfully performed AI analysis for job seeker resume for user {} (UserDetail ID {}).", user.getId(), savedUserDetail.getId());
                    } catch (AppException e) {
                        log.error("Failed to perform AI analysis for job seeker resume for user {}: {}", user.getId(), e.getMessage(), e);
                    } catch (Exception e) {
                        log.error("An unexpected error occurred during AI analysis for job seeker resume for user {}: {}", user.getId(), e.getMessage(), e);
                    }
                } else {
                    // Nếu resumeUrlUpdatedOrCleared là true và resumeUrl là null/empty (người dùng xóa resume)
                    log.info("Resume URL is empty or null for job seeker ID {} after update. Skipping new AI analysis.", user.getId());
                    // Tùy chọn: Xóa bản phân tích AI cũ nếu resume đã bị xóa
                    jobseekerAnalysisRepository.findByUserDetail(savedUserDetail)
                            .ifPresent(jobseekerAnalysisRepository::delete);
                    log.info("Deleted existing AI analysis for job seeker ID {} as resume was cleared.", user.getId());
                }
            } else {
                log.info("Resume for job seeker ID {} not updated or no new resume to analyze. Skipping AI analysis.", user.getId());
            }
        }
        return mapToProfileResponse(user, userDetail);
    }

    public List<ProfileResponse> listCurrentUserProfiles() throws Exception { // Nên đổi thành throws AppException
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (currentUser.getVerified() == 0) {
            throw new AppException(ErrorCode.USER_NOT_VERIFIED);
        }

        UserDetail userDetail = userDetailsRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        ProfileResponse response = mapToProfileResponse(currentUser, userDetail);
        return Collections.singletonList(response);
    }

    private ProfileResponse mapToProfileResponse(User user, UserDetail userDetail) {
        ProfileResponse response = new ProfileResponse();
        response.setEmail(user.getEmail());
        response.setRoleName(user.getRole().getName());
        response.setLocation(userDetail.getLocation());
        if (userDetail.getEducation() != null) {
            response.setEducationId(userDetail.getEducation().getId());
            response.setEducationName(userDetail.getEducation().getName());
        }
        response.setFullName(userDetail.getFullName());
        response.setPhone(userDetail.getPhone());
        if (userDetail.getExperience() != null) {
            response.setExperienceId(userDetail.getExperience().getId());
            response.setExperienceName(userDetail.getExperience().getName());
        }
        if (userDetail.getCategory() != null) {
            response.setCategoryId(userDetail.getCategory().getId());
            response.setCategoryName(userDetail.getCategory().getName());
        }
        response.setResumeUrl(userDetail.getResumeUrl());
        response.setCompanyName(userDetail.getCompanyName());
        response.setDescription(userDetail.getDescription());
        response.setWebsite(userDetail.getWebsite());
        response.setAvatarUrl(userDetail.getAvatarUrl());
        if (userDetail.getOrganization() != null) {
            response.setOrganizationId(userDetail.getOrganization().getId());
            response.setOrganizationType(userDetail.getOrganization().getName());
        }

        response.setBanner(userDetail.getBanner());
        response.setTeamSize(userDetail.getTeamSize());

        response.setYearOfEstablishment(userDetail.getYearOfEstablishment());
        response.setMapLocation(userDetail.getMapLocation());
        response.setCompanyVision(userDetail.getCompanyVision());

        return response;
    }
}