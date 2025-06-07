package com.example.jobfinder.repository;

import com.example.jobfinder.model.JobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobTypeRepository extends JpaRepository<JobType, Long> {
    Optional<JobType> findByName(String name);
}
