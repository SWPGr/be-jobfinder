package com.example.jobfinder.repository;

import com.example.jobfinder.model.SavedJob;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    Optional<SavedJob> findByJobSeekerIdAndJobId(Long jobSeekerId, Long jobId);

    Boolean existsByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId);

    List<SavedJob> findByJobSeeker_Id(Long jobSeekerId);

    @Query("SELECT s.job.id FROM SavedJob s WHERE s.jobSeeker.id = :userId")
    List<Long> findSavedJobIdsByUserId(@Param("userId") Long userId);

    Page<SavedJob> findByJobSeeker_Id(Long jobSeekerId, Pageable pageable);

    Long countByJobSeeker_Id(Long jobSeekerId);
}
