package com.example.jobfinder.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileResponse {
    private String email;
    private String roleName;
    private String location;
    private String fullName;
    private Long educationId;
    private String educationName;
    private String phone;
    private Long experienceId;
    private String experienceName;
    private String resumeUrl;
    private String companyName;
    private String description;
    private String website;
    private String avatarUrl;
    private Long organizationId;
    private String organizationType;
}
