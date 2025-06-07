package com.example.jobfinder.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ProfileRequest {
    private String location;
    private String fullName;
    private String phone;
    private Long education;
    private Integer yearsExperience;
    private String resumeUrl;
    private String companyName;
    private String description;
    private String website;
}
