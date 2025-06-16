// D:\Code-Window\JobFinderProject\be-jobfinder\trunglecode\src\main\java\com\example\jobfinder\controller\JobLevelController.java
package com.example.jobfinder.controller;

import com.example.jobfinder.service.BaseNameService;
import com.example.jobfinder.service.JobLevelService; // Import JobLevelService
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/job-levels") // Đường dẫn gốc cho Job Levels
public class JobLevelController extends BaseNameController {

    private final JobLevelService jobLevelService;

    public JobLevelController(JobLevelService jobLevelService) {
        this.jobLevelService = jobLevelService;
    }

    @Override
    protected BaseNameService getService() {
        return jobLevelService;
    }

    @Override
    protected String getBasePath() {
        return "Job Level";
    }
}