package com.example.jobfinder.controller;

import com.example.jobfinder.service.BaseNameService;
import com.example.jobfinder.service.SocialTypeService; // Import SocialTypeService
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/social-types")
public class SocialTypeController extends BaseNameController {

    private final SocialTypeService socialTypeService;

    public SocialTypeController(SocialTypeService socialTypeService) {
        this.socialTypeService = socialTypeService;
    }

    @Override
    protected BaseNameService getService() {
        return socialTypeService;
    }

    @Override
    protected String getBasePath() {
        return "Social Type";
    }
}