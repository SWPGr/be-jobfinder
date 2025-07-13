package com.example.jobfinder.repository;

import com.example.jobfinder.model.JobRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRecommendationRepository extends JpaRepository<JobRecommendation, Long> {
    List<JobRecommendation> findByJobSeekerIdOrderByScoreDesc(Long jobSeekerId);
    Optional<JobRecommendation> findByJobSeekerIdAndJobId(Long jobSeekerId, Long jobId);
    void deleteByJobSeekerId(Long jobSeekerId);
    Long countByJobSeekerId(Long jobSeekerId);
}
