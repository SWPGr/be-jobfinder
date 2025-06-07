package com.example.jobfinder.repository;

import com.example.jobfinder.model.JobLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobLevelRepository extends JpaRepository<JobLevel, Long> {
    Optional<JobLevel> findByName(String name);
}
