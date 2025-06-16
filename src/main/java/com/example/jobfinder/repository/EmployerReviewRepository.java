package com.example.jobfinder.repository;

import com.example.jobfinder.model.EmployerReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployerReviewRepository extends JpaRepository<EmployerReview, Long> {
    List<EmployerReview> findByEmployerId(Long employerId);
    Optional<EmployerReview> findByJobSeekerIdAndEmployerId(Long jobSeekerId, Long employerId);
    List<EmployerReview> findByJobSeekerId(Long jobSeekerId);
}