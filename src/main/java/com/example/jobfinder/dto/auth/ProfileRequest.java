package com.example.jobfinder.dto.auth;

import com.example.jobfinder.model.Education;
import com.example.jobfinder.model.Experience;
import com.example.jobfinder.model.Organization;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ProfileRequest {
    private String location;
    private String fullName;
    private String phone;
    private Education education;
    private Experience userExperience;
    private MultipartFile resumeUrl;
    private String companyName;
    private String description;
    private String website;
    private MultipartFile avatar;
    private Organization organization;

    private MultipartFile banner; // Để nhận URL banner nếu không gửi file mới
    private String teamSize;
    private Integer yearOfEstablishment; // Năm thành lập
    private String mapLocation;
    private String organizationType;

}
