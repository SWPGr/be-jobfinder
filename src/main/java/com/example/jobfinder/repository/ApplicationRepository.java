package com.example.jobfinder.repository;

import com.example.jobfinder.model.Application;
import com.example.jobfinder.util.QueryConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByJobSeekerIdAndJobId(Long jobSeekerId, Long jobId);

    List<Application> findByJobSeekerId(Long jobSeekerId);

    List<Application> findByJob_Id(Long jobId);

    boolean existsByJobSeeker_IdAndJob_Employer_Id(Long jobSeekerId, Long employerId);
    @Query(QueryConstants.FIND_APPLICATIONS_BY_CRITERIA) // Sử dụng hằng số
    List<Application> findApplicationsByCriteria(@Param("jobSeekerEmail") String jobSeekerEmail,
                                                 @Param("jobTitle") String jobTitle,
                                                 @Param("status") String status);
}
