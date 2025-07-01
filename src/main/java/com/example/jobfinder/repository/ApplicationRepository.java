package com.example.jobfinder.repository;

import com.example.jobfinder.model.Application;
import com.example.jobfinder.model.User;
import com.example.jobfinder.util.QueryConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByJobSeekerIdAndJobId(Long jobSeekerId, Long jobId);

    List<Application> findByJobSeekerId(Long jobSeekerId);

    List<Application> findByAppliedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Application> findByJob_Id(Long jobId);

    Long countByJob_Id (Long jobId);

    boolean existsByJobSeeker_IdAndJob_Employer_Id(Long jobSeekerId, Long employerId);
    @Query(QueryConstants.FIND_APPLICATIONS_BY_CRITERIA) // Sử dụng hằng số
    List<Application> findApplicationsByCriteria(@Param("jobSeekerEmail") String jobSeekerEmail,
                                                 @Param("jobTitle") String jobTitle,
                                                 @Param("status") String status);

    @Query("SELECT ja.jobSeeker FROM Application ja " +
            "JOIN FETCH ja.jobSeeker.userDetail sd " + // Join FETCH SeekerDetail
            "WHERE ja.job.id = :jobId")
    List<User> findApplicantsWithDetailsByJobId(@Param("jobId") Long jobId);

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

    // THÊM PHƯƠNG THỨC NÀY (cho Employer)
    @Query("SELECT a FROM Application a " +
            "LEFT JOIN a.job j " +
            "WHERE j.id IN :jobIds " +
            "AND (:jobTitle IS NULL OR j.title LIKE %:jobTitle%) " +
            "AND (:status IS NULL OR a.status = :status)")
    List<Application> findApplicationsByJobIdsAndJobTitleAndStatus(
            @Param("jobIds") List<Long> jobIds,
            @Param("jobTitle") String jobTitle,
            @Param("status") String status
    );

    Long countByAppliedAt(LocalDateTime date);

    @Query("SELECT FUNCTION('DATE', ja.appliedAt) AS appliedDate, COUNT(ja) AS count " +
            "FROM Application ja " +
            "WHERE ja.appliedAt BETWEEN :startDateTime AND :endDateTime " +
            "GROUP BY FUNCTION('DATE', ja.appliedAt) " +
            "ORDER BY FUNCTION('DATE', ja.appliedAt) ASC")
    List<Object[]> countApplicationsByDateTimeRange(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);


    @Query("SELECT ja FROM Application ja " +
            "JOIN FETCH ja.job j " + // Tham gia Job
            "JOIN FETCH j.employer e " + // Tham gia Employer của Job để lọc
            "JOIN FETCH ja.jobSeeker a " + // Tham gia User (applicant)
            "LEFT JOIN FETCH a.userDetail ud " + // Tham gia UserDetail của applicant (LEFT JOIN để không loại trừ nếu userDetail null)
            "LEFT JOIN FETCH ud.education edu " + // Tham gia Education của userDetail
            "WHERE e.id = :employerId " + // Lọc theo ID của nhà tuyển dụng
            "AND (:fullName IS NULL OR LOWER(ud.fullName) LIKE LOWER(CONCAT('%', :fullName, '%'))) " +
            "AND (:email IS NULL OR LOWER(a.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
            "AND (:location IS NULL OR LOWER(ud.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
            "AND (:minYearsExperience IS NULL OR ud.experience >= :minYearsExperience) " +
            "AND (:educationId IS NULL OR edu.id = :educationId)")
    Page<Application> findApplicationsForEmployerWithFilters(
            @Param("employerId") Long employerId,
            @Param("fullName") String fullName,
            @Param("email") String email,
            @Param("location") String location,
            @Param("minYearsExperience") Integer minYearsExperience,
            @Param("educationId") Long educationId,
            Pageable pageable
    );
}

