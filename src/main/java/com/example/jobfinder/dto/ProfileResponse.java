package com.example.jobfinder.dto;

import com.example.jobfinder.model.Education;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ProfileResponse {
    private String email;
    private String roleName;
    private String location;
    private String fullName;
    private Long education;
    private String phone;
    private Integer yearsExperience;
    private String resumeUrl;
    private String companyName;
    private String description;
    private String website;
}
