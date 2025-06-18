// D:\Code-Window\JobFinderProject\be-jobfinder\trunglecode\src\main\java\com\example\jobfinder\controller\JobTypeController.java
package com.example.jobfinder.controller;

import com.example.jobfinder.service.BaseNameService;
import com.example.jobfinder.service.JobTypeService; // Import JobTypeService
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/job-types") // Đường dẫn gốc cho Job Types
public class JobTypeController extends BaseNameController {

    private final JobTypeService jobTypeService;

    public JobTypeController(JobTypeService jobTypeService) {
        this.jobTypeService = jobTypeService;
    }

    @Override
    protected BaseNameService getService() {
        return jobTypeService;
    }

    @Override
    protected String getBasePath() {
        return "Job Type"; // Hoặc "JobType"
    }
}