package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.dto.user.UserResponse;
import com.example.jobfinder.model.UserDocument;
import com.example.jobfinder.repository.EducationRepository;
import com.example.jobfinder.repository.ExperienceRepository;
import com.example.jobfinder.repository.OrganizationRepository;
import com.example.jobfinder.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDocumentMapper {

    private final EducationRepository educationRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final ExperienceRepository experienceRepository;
    private final UserMapper userMapper;

    public UserResponse toUserResponse(UserDocument userDocument) {
        UserResponse response = userMapper.toUserResponse(userDocument);

        response.setEducation(getEducation(userDocument.getEducationId()));
        response.setRole(getRole(userDocument.getRoleId()));
        response.setOrganization(getOrganization(userDocument.getOrganizationId()));
        response.setExperience(getExperience(userDocument.getExperienceId()));
        
        // Set số lượng job đã post (chỉ job active)
        if (userDocument.getJobsPosted() != null) {
            response.setTotalJobsPosted(userDocument.getJobsPosted().longValue());
        }
        
        return response;
    }

    private SimpleNameResponse getRole(Long roleId) {
        if (roleId == null) return null;
        return roleRepository.findById(roleId)
                .map(role -> new SimpleNameResponse(role.getId(), role.getName()))
                .orElse(null);
    }

    private SimpleNameResponse getEducation(Long educationId) {
        if (educationId == null) return null;
        return educationRepository.findById(educationId)
                .map(education -> new SimpleNameResponse(education.getId(), education.getName()))
                .orElse(null);
    }

    private SimpleNameResponse getOrganization(Long organizationId) {
        if (organizationId == null) return null;
        return organizationRepository.findById(organizationId)
                .map(organization -> new SimpleNameResponse(organization.getId(), organization.getName()))
                .orElse(null);
    }

    private SimpleNameResponse getExperience(Long experienceId) {
        if (experienceId == null) return null;
        return experienceRepository.findById(experienceId)
                .map(experience -> new SimpleNameResponse(experience.getId(), experience.getName()))
                .orElse(null);
    }
}
