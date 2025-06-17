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
    /**
             * Retrieves a JobView for a specific job seeker and job, where the view occurred on or after the specified start of day.
             *
             * @param jobSeekerId the ID of the job seeker
             * @param jobId the ID of the job
             * @param startOfDay the earliest viewedAt timestamp to consider
             * @return an Optional containing the matching JobView if found, or empty if not found
             */
            @Query("SELECT jv FROM JobView jv WHERE jv.jobSeeker.id = :jobSeekerId AND jv.job.id = :jobId AND jv.viewedAt >= :startOfDay")
    Optional<JobView> findByJobSeekerIdAndJobIdAndViewedAtAfter(
            @Param("jobSeekerId") Long jobSeekerId,
            @Param("jobId") Long jobId,
            @Param("startOfDay")LocalDateTime startOfDay
            );
}
