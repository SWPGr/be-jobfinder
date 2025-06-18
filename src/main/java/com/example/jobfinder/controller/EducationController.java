// D:\Code-Window\JobFinderProject\be-jobfinder\trunglecode\src\main\java\com\example\jobfinder\controller\EducationController.java
package com.example.jobfinder.controller;

import com.example.jobfinder.service.BaseNameService; // Cần import BaseNameService
import com.example.jobfinder.service.EducationService; // Cần import EducationService
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/educations") // Endpoint base cho Education
public class EducationController extends BaseNameController {

    private final EducationService educationService; // Inject service cụ thể

    public EducationController(EducationService educationService) {
        this.educationService = educationService;
    }

    @Override
    protected BaseNameService getService() {
        return educationService; // Trả về instance của EducationService
    }

    @Override
    protected String getBasePath() {
        return "Education"; // Tên để hiển thị trong message/log
    }
}