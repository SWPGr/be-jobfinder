package com.example.jobfinder.dto.application;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ApplicationRequest {
    private Long jobId;
    private String email;
    private String phone;
    private MultipartFile resume;
    private String coverLetter;
}
