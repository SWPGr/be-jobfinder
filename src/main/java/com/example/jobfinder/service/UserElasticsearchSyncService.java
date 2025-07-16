package com.example.jobfinder.service;

import com.example.jobfinder.model.UserDetail;
import com.example.jobfinder.model.UserDocument;
import com.example.jobfinder.repository.UserDetailsRepository;
import com.example.jobfinder.repository.UserDocumentRepository;
import com.example.jobfinder.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserElasticsearchSyncService {
    private static final Logger log = LoggerFactory.getLogger(ElasticsearchSyncService.class);


    private final UserDetailsRepository userDetailsRepository;
    private final UserDocumentRepository userDocumentRepository;
    private final JobRepository jobRepository;

    public UserElasticsearchSyncService(UserDetailsRepository userDetailsRepository, 
                                       UserDocumentRepository userDocumentRepository,
                                       JobRepository jobRepository) {
        this.userDetailsRepository = userDetailsRepository;
        this.userDocumentRepository = userDocumentRepository;
        this.jobRepository = jobRepository;
    }

    @Scheduled(cron = "0 5 0 * * *")
    public void syncAllUser() {
        List<UserDetail> userDetails = userDetailsRepository.findAll();
        List<UserDocument> userDocuments = userDetails.stream().map(this::mapToDocument).toList();

        userDocumentRepository.saveAll(userDocuments);
        log.info("All jobs have been synced, indexed {} user", userDetails.size());
    }

    // Method để sync manual cho testing
    public void syncAllUserManual() {
        syncAllUser();
    }

    private UserDocument mapToDocument(UserDetail userDetail) {
        UserDocument userDocument = new UserDocument();
        userDocument.setId(userDetail.getId());
        userDocument.setEmail(userDetail.getUser().getEmail());
        userDocument.setFullName(userDetail.getFullName());
        userDocument.setCompanyName(userDetail.getCompanyName());
        userDocument.setLocation(userDetail.getLocation());
        userDocument.setDescription(userDetail.getDescription());
        userDocument.setWebsite(userDetail.getWebsite());
        userDocument.setAvatarUrl(userDetail.getAvatarUrl());
        userDocument.setPhone(userDetail.getPhone());
        userDocument.setTeamSize(userDetail.getTeamSize());
        userDocument.setYearOfEstablishment(userDetail.getYearOfEstablishment());
        userDocument.setMapLocation(userDetail.getMapLocation());
        if (userDetail.getUser() != null && userDetail.getUser().getRole() != null) {
            userDocument.setRoleId(userDetail.getUser().getRole().getId());
        }

        if (userDetail.getEducation() != null) {
            userDocument.setEducationId(userDetail.getEducation().getId());
        }

        if (userDetail.getOrganization() != null) {
            userDocument.setOrganizationId(userDetail.getOrganization().getId());
        }
        if (userDetail.getExperience() != null) {
            userDocument.setExperienceId(userDetail.getExperience().getId());
        }

        // Set số lượng job active của employer
        if (userDetail.getUser() != null && userDetail.getUser().getRole() != null 
            && "EMPLOYER".equals(userDetail.getUser().getRole().getName())) {
            long activeJobsCount = jobRepository.countByEmployerIdAndActiveTrue(userDetail.getUser().getId());
            userDocument.setJobsPosted((int) activeJobsCount);
            log.info("Employer {} (userId: {}) has {} active jobs", 
                    userDetail.getCompanyName(), userDetail.getUser().getId(), activeJobsCount);
        } else {
            userDocument.setJobsPosted(0);
        }

        return userDocument;
    }
}
