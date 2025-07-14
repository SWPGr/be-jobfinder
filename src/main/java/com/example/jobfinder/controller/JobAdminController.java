package com.example.jobfinder.controller;

import com.example.jobfinder.service.ElasticsearchSyncService;
import com.example.jobfinder.service.UserElasticsearchSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/admin")
@RequiredArgsConstructor
public class JobAdminController {

    private final ElasticsearchSyncService elasticsearchSyncService;
    private final UserElasticsearchSyncService userElasticsearchSyncService;


    @PostMapping("/sync")
    public ResponseEntity<String> syncJobsToElasticsearch() {
        elasticsearchSyncService.syncAllJobs();
        return ResponseEntity.ok("Jobs sync complete.");
    }

    @PostMapping("/syncUser")
    public ResponseEntity<String> syncUserToElasticsearch() {
        userElasticsearchSyncService.syncAllUser();
        return ResponseEntity.ok("User sync complete.");
    }
}
