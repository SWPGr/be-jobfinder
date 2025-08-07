package com.example.jobfinder.dto.employer;

public interface TopEmployerProjection {
    Long getUserId();
    String getUserEmail();
    String getCompanyName();
    String getUserLocation();
    Long getTotalApplications();
}
