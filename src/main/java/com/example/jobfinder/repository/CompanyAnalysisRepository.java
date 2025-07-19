package com.example.jobfinder.repository;

import com.example.jobfinder.model.CompanyAnalysis;
import com.example.jobfinder.model.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyAnalysisRepository extends JpaRepository<CompanyAnalysis, Long> {
    Optional<CompanyAnalysis> findByUserDetail(UserDetail userDetail);
}