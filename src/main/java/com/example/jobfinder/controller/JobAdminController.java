package com.example.jobfinder.controller;

import com.example.jobfinder.service.ElasticsearchSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/admin/jobs")
public class JobAdminController {

    private final ElasticsearchSyncService elasticsearchSyncService;

    public JobAdminController(ElasticsearchSyncService elasticsearchSyncService) {
        this.elasticsearchSyncService = elasticsearchSyncService;
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncJobsToElasticsearch() {
        elasticsearchSyncService.syncAllJobs();
        return ResponseEntity.ok("Jobs sync complete.");
    }
}
