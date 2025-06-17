package com.example.jobfinder.repository;

import com.example.jobfinder.model.JobView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface JobViewRepository extends JpaRepository<JobView, Long> {
    @Query("SELECT jv FROM JobView jv WHERE jv.jobSeeker.id = :jobSeekerId AND jv.job.id = :jobId AND jv.viewedAt >= :startOfDay")
    Optional<JobView> findByJobSeekerIdAndJobIdAndViewedAtAfter(
            @Param("jobSeekerId") Long jobSeekerId,
            @Param("jobId") Long jobId,
            @Param("startOfDay")LocalDateTime startOfDay
            );
}
