package com.example.jobfinder.repository;

import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.Report;
import com.example.jobfinder.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByUserAndJobAndSubjectAndContent(User user, Job job, String subject, String content);
}
