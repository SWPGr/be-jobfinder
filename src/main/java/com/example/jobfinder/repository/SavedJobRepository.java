package com.example.jobfinder.repository;

import com.example.jobfinder.model.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    Optional<SavedJob> findByJobSeekerIdAndJobId(Long jobSeekerId, Long jobId);

    List<SavedJob> findByJobSeeker_Id(Long jobSeekerId);
}
