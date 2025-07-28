package com.example.jobfinder.dto.job;

import com.example.jobfinder.model.Organization;

public record OrganizationDTO(Long id, String name) {
    public static OrganizationDTO fromEntity(Organization org) {
        return new OrganizationDTO(org.getId(), org.getName());
    }
}