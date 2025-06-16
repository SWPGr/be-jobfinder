package com.example.jobfinder.repository;

import com.example.jobfinder.model.JobRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRecommendationRepository extends JpaRepository<JobRecommendation, Long> {
    List<JobRecommendation> findByJobSeekerIdOrderByScoreDesc(Long jobSeekerId);
    void deleteByJobSeekerId(Long jobSeekerId);
}
