package com.example.jobfinder.repository;

import com.example.jobfinder.model.Application;
import com.example.jobfinder.model.User;
import com.example.jobfinder.util.QueryConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByJobSeekerIdAndJobId(Long jobSeekerId, Long jobId);

    Optional<Application> findByJobSeekerId(Long jobSeekerId);

    Page<Application> findByJobSeekerId(Long jobSeekerId, Pageable pageable);

    Long countByJobSeekerId(Long jobSeekerId); // <-- THÊM DÒNG NÀY


    List<Application> findByAppliedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Application> findByJob_Id(Long jobId);

    Long countByJob_Id (Long jobId);

    @Query("SELECT COUNT(ja) FROM Application ja WHERE ja.jobSeeker.id = :applicantId")
    long countByApplicantId(Long applicantId);

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
            "JOIN FETCH ja.job j " +
            "JOIN FETCH j.employer e " +
            "JOIN FETCH ja.jobSeeker a " + // Đảm bảo mối quan hệ là jobSeeker
            "LEFT JOIN FETCH a.userDetail ud " +
            "LEFT JOIN FETCH ud.education edu " +
            "LEFT JOIN FETCH j.jobType jt " +
            "LEFT JOIN FETCH j.jobLevel jl " +
            "WHERE e.id = :employerId " +
            "AND j.id = :jobId " +

            // Filters for Applicant (UserDetail)
            "AND (:name IS NULL OR LOWER(ud.fullName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:educationId IS NULL OR edu.id = :educationId) " +

            // Filters for Job (via Job entity, but applied to applications for *this* job)
            "AND (:jobTypeId IS NULL OR jt.id = :jobTypeId) " +
            "AND (:jobLevelId IS NULL OR jl.id = :jobLevelId)")
    Page<Application> getEmployerJobApplicationsForSpecificJob( // Đã đổi tên phương thức ở đây
                                                                   @Param("employerId") Long employerId,
                                                                   @Param("jobId") Long jobId,
                                                                   // Applicant-related filter parameters
                                                                   @Param("name") String name,
                                                                   @Param("minExperience") Integer minExperience,
                                                                   @Param("maxExperience") Integer maxExperience,
                                                                   // Job-related filter parameters
                                                                   @Param("jobTypeId") Long jobTypeId,
                                                                   @Param("educationId") Long educationId,
                                                                   @Param("jobLevelId") Long jobLevelId,
                                                                   Pageable pageable
    );

    @Query("SELECT js FROM Application a " +
            "JOIN a.jobSeeker js " +
            "LEFT JOIN FETCH js.userDetail ud " +
            "LEFT JOIN FETCH ud.education education " + // Alias cho education
            "LEFT JOIN FETCH ud.experience experience " + // Alias cho experience
            "LEFT JOIN FETCH js.role " +
            "WHERE a.job.id = :jobId " +
            "AND js.isActive = TRUE " + // Chỉ lấy ứng viên active
            "AND (:fullName IS NULL OR ud.fullName LIKE CONCAT('%', :fullName, '%')) " +
            "AND (:email IS NULL OR js.email LIKE CONCAT('%', :email, '%')) " +
            "AND (:location IS NULL OR ud.location LIKE CONCAT('%', :location, '%')) " +
            "AND (:experienceName IS NULL OR experience.name LIKE CONCAT('%', :experienceName, '%')) " + // Lọc theo tên kinh nghiệm
            "AND (:educationName IS NULL OR education.name LIKE CONCAT('%', :educationName, '%')) " + // Lọc theo tên học vấn
            "AND (:isPremium IS NULL OR js.isPremium = :isPremium)") // Lọc theo trạng thái premium
    Page<User> findApplicantsWithDetailsByJobIdAndFilters(
            @Param("jobId") Long jobId,
            @Param("fullName") String fullName,
            @Param("email") String email,
            @Param("location") String location,
            @Param("experienceName") String experienceName,
            @Param("educationName") String educationName,
            @Param("isPremium") Boolean isPremium,
            Pageable pageable);

}

