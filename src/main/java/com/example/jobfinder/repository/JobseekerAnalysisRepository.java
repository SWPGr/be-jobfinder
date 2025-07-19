package com.example.jobfinder.repository;

import com.example.jobfinder.model.Application;
import com.example.jobfinder.model.JobseekerAnalysis;
import com.example.jobfinder.model.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobseekerAnalysisRepository extends JpaRepository<JobseekerAnalysis, Long> {
    Optional<JobseekerAnalysis> findByUserDetail(UserDetail userDetail);
}