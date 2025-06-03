package com.example.jobfinder.repository;

import com.example.jobfinder.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    boolean existsByTitleAndEmployerId(String title, Long employerId);
    List<Job> findByTitleContainingIgnoreCase(String title);
    List<Job> findByLocation(String location);
}
