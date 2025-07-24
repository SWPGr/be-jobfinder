package com.example.jobfinder.repository;

import com.example.jobfinder.model.Application;
import com.example.jobfinder.model.ResumeSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeSummaryRepository extends JpaRepository<ResumeSummary, Long> {
    Optional<ResumeSummary> findByApplication(Application application);
    Optional<ResumeSummary> findByApplicationId(Long applicationId);
}