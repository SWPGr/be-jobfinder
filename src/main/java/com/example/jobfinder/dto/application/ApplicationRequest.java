package com.example.jobfinder.dto.application;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationRequest {
    private Long jobId;
    private String email;
    private String phone;
    private String resume;
    private String coverLetter;
}
