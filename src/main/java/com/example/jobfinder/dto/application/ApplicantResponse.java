package com.example.jobfinder.dto.application;

import com.example.jobfinder.dto.simple.SimpleNameResponse;
import com.example.jobfinder.model.Experience;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApplicantResponse {
    private Long id; // ID của User
    private String email;
    private String fullName; // Từ UserDetail
    private String location; // Từ UserDetail
    private Experience experience; // Từ UserDetail
    private String phone; // Từ UserDetail
    private SimpleNameResponse education; // Nếu có entity Education
    private String resumeUrl;
    // Thêm các trường khác từ UserDetail nếu cần
}