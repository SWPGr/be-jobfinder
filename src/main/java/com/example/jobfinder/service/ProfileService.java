package com.example.jobfinder.service;

import com.example.jobfinder.dto.auth.ProfileRequest;
import com.example.jobfinder.dto.auth.ProfileResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.model.Education;
import com.example.jobfinder.model.Experience;
import com.example.jobfinder.model.User;
import com.example.jobfinder.model.UserDetail;
import com.example.jobfinder.repository.EducationRepository;
import com.example.jobfinder.repository.ExperienceRepository;
import com.example.jobfinder.repository.UserDetailsRepository;
import com.example.jobfinder.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final EducationRepository educationRepository;
    private final CloudinaryService cloudinaryService;
    private final ExperienceRepository experienceRepository;

    public ProfileService(UserRepository userRepository, UserDetailsRepository userDetailsRepository,
                          EducationRepository educationRepository, CloudinaryService cloudinaryService,
                          ExperienceRepository experienceRepository) {
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.educationRepository = educationRepository;
        this.cloudinaryService = cloudinaryService;
        this.experienceRepository = experienceRepository;
    }

    public ProfileResponse updateProfile(ProfileRequest request) throws Exception{
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->  new UsernameNotFoundException(email));
        if (user == null) {
            throw new Exception("User not found");
        }

        UserDetail userDetail = userDetailsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND)); // UserDetail phải tồn tại.;
        if (userDetail == null) {
            throw new Exception("Profile not found.");
        }

        String roleName = user.getRole().getName();
        if (roleName.equals("JOB_SEEKER")) {
            if (request.getEducation() != null) {
                Education education = educationRepository.findById(request.getEducation().getId())
                        .orElseThrow(() -> new Exception("invalid education id"));
                userDetail.setEducation(education);
            }
            userDetail.setLocation(request.getLocation());
            userDetail.setFullName(request.getFullName());
            userDetail.setPhone(request.getPhone());
            if (request.getUserExperience() != null) {
                Experience experience = experienceRepository.findById(request.getUserExperience().getId())
                        .orElseThrow(() -> new AppException(ErrorCode.EXPERIENCE_NOT_FOUND));
                userDetail.setExperience(experience);
            }
            userDetail.setResumeUrl(request.getResumeUrl());
        }else if(roleName.equals("EMPLOYER")) {
            if(request.getCompanyName() == null || request.getCompanyName().isEmpty()) {
                throw new Exception("Company name is required for employer");
            }
            userDetail.setCompanyName(request.getCompanyName());
            userDetail.setLocation(request.getLocation());
            userDetail.setDescription(request.getDescription());
            userDetail.setWebsite(request.getWebsite());
            userDetail.setOrganizationType(request.getOrganizationType());
        }else {
            throw new Exception("Invalid role");
        }
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            String avatarUrl = cloudinaryService.uploadFile(request.getAvatar());
            userDetail.setAvatarUrl(avatarUrl);
        }
        userDetailsRepository.save(userDetail);
        return mapToProfileResponse(user, userDetail);
    }

    public List<ProfileResponse> listCurrentUserProfiles() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() ->  new UsernameNotFoundException(email));
        if (currentUser == null) {
            throw new Exception("User not found");
        }

        if (currentUser.getVerified() == 0) {
            throw new Exception("Please verify your email first");
        }

        UserDetail userDetail = userDetailsRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND)); // UserDetail phải tồn tại.;
        if (userDetail == null) {
            return Collections.emptyList();
        }

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
            response.setExperienceName(userDetail.getExperience().getName()); // hoặc getExperienceName()
        }
        response.setResumeUrl(userDetail.getResumeUrl());
        response.setCompanyName(userDetail.getCompanyName());
        response.setDescription(userDetail.getDescription());
        response.setWebsite(userDetail.getWebsite());
        response.setAvatarUrl(userDetail.getAvatarUrl());
        response.setOrganizationType(userDetail.getOrganizationType());
        return response;
    }
}
