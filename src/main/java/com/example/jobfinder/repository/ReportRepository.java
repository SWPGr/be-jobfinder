package com.example.jobfinder.repository;

import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.Report;
import com.example.jobfinder.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByUserAndJobAndSubjectAndContent(User user, Job job, String subject, String content);

    Page<Report> findAllByCreatedAtBetween(LocalDate from, LocalDate to, Pageable pageable);

    Page<Report> findByReportTypeIdAndCreatedAtBetween(Long reportTypeId, LocalDate from, LocalDate to, Pageable pageable);

    Page<Report> findByReportTypeId(Long id, Pageable pageable);

}
