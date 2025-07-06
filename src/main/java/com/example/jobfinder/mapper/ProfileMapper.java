// src/main/java/com/example/jobfinder/mapper/ProfileMapper.java
package com.example.jobfinder.mapper;

import com.example.jobfinder.dto.auth.ProfileResponse;
import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.model.Education;
import com.example.jobfinder.model.Experience;
import com.example.jobfinder.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

/**
 * MapStruct Mapper for converting User and UserDetail entities to ProfileResponse DTO.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProfileMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "isPremium", target = "isPremium")
    @Mapping(source = "verified", target = "verified")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "role.name", target = "roleName")

    // Ánh xạ các trường từ UserDetail
    @Mapping(source = "userDetail.fullName", target = "fullName")
    @Mapping(source = "userDetail.location", target = "location")
    @Mapping(source = "userDetail.avatarUrl", target = "avatarUrl")

    // Ánh xạ các trường JobSeeker từ UserDetail
    @Mapping(source = "userDetail.resumeUrl", target = "resumeUrl")
    @Mapping(source = "userDetail.education", target = "education")

    // Ánh xạ các trường Employer từ UserDetail
    @Mapping(source = "userDetail.companyName", target = "companyName")
    @Mapping(source = "userDetail.website", target = "website")
    @Mapping(source = "userDetail.banner", target = "banner")
    @Mapping(source = "userDetail.teamSize", target = "teamSize")
    @Mapping(source = "userDetail.yearOfEstablishment", target = "yearOfEstablishment")
    @Mapping(source = "userDetail.mapLocation", target = "mapLocation")
    @Mapping(source = "userDetail.organizationType", target = "organizationType")

    // Bỏ qua các trường tính toán vì chúng sẽ được gán thủ công trong Service
    @Mapping(target = "totalJobsPosted", ignore = true)
    @Mapping(target = "totalApplications", ignore = true)
    ProfileResponse toProfileResponse(User user);

    // Helper method để ánh xạ Education sang SimpleNameResponse
    @Named("mapEducationToSimpleNameResponse")
    default SimpleNameResponse mapEducationToSimpleNameResponse(Education education) {
        if (education == null) {
            return null;
        }
        return SimpleNameResponse.builder()
                .id(education.getId())
                .name(education.getName())
                .build();
    }

    // Helper method để ánh xạ Experience sang SimpleNameResponse
    @Named("mapExperienceToSimpleNameResponse")
    default SimpleNameResponse mapExperienceToSimpleNameResponse(Experience experience) {
        if (experience == null) {
            return null;
        }
        return SimpleNameResponse.builder()
                .id(experience.getId())
                .name(experience.getName())
                .build();
    }
}