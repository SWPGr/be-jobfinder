package com.example.jobfinder.repository;

import com.example.jobfinder.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByJobSeekerIdAndJobId(Long jobSeekerId, Long jobId);

    List<Application> findByJobSeekerId(Long jobSeekerId);

    List<Application> findByJob_Id(Long jobId);

    boolean existsByJobSeeker_IdAndJob_Employer_Id(Long jobSeekerId, Long employerId);

}
