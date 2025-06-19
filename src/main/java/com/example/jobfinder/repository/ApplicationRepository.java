package com.example.jobfinder.repository;

import com.example.jobfinder.model.Application;
import com.example.jobfinder.util.QueryConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    @Query("SELECT COUNT(a) FROM Application a")
    long countAllApplications();

    @Query("SELECT COUNT(DISTINCT a.job.id) FROM Application a WHERE a.appliedAt <= :endDate")
    long countUniqueAppliedJobsBeforeOrEquals(@Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Application a " +
            "WHERE a.jobSeeker.id = :jobSeekerId " +
            "AND (:jobTitle IS NULL OR LOWER(a.job.title) LIKE LOWER(CONCAT('%', :jobTitle, '%'))) " +
            "AND (:status IS NULL OR LOWER(a.status) = LOWER(:status))")
    List<Application> findApplicationsByJobSeekerAndJobTitleAndStatus(
            @Param("jobSeekerId") Long jobSeekerId,
            @Param("jobTitle") String jobTitle,
            @Param("status") String status
    );

    // 2. Dành cho EMPLOYER: Tìm ứng tuyển vào các công việc của một danh sách các ID công việc
    // Employer chỉ được xem đơn ứng tuyển vào CÁC CÔNG VIỆC mà họ đã đăng.
    @Query("SELECT a FROM Application a " +
            "WHERE a.job.id IN :employerJobIds " +
            "AND (:jobSeekerEmail IS NULL OR a.jobSeeker.email = :jobSeekerEmail) " + // Có thể lọc theo email ứng viên
            "AND (:jobTitle IS NULL OR LOWER(a.job.title) LIKE LOWER(CONCAT('%', :jobTitle, '%'))) " +
            "AND (:status IS NULL OR LOWER(a.status) = LOWER(:status))")
    List<Application> findApplicationsByEmployerJobsAndCriteria(
            @Param("employerJobIds") List<Long> employerJobIds,
            @Param("jobSeekerEmail") String jobSeekerEmail,
            @Param("jobTitle") String jobTitle,
            @Param("status") String status
    );
}
