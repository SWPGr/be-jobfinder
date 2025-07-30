package com.example.jobfinder.repository;

import com.example.jobfinder.model.Job;
import com.example.jobfinder.util.QueryConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    boolean existsByTitleAndEmployerId(String title, Long employerId);

    boolean existsByIdAndEmployerId(Long jobId, Long employerId);

    List<Job> findByActiveTrue();

    List<Job> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(j) FROM Job j WHERE j.employer.id = :employerId")
    long countByEmployerId(@Param("employerId") Long employerId);

    @Query("SELECT COUNT(j) FROM Job j WHERE j.employer.id = :employerId AND j.active = true")
    long countByEmployerIdAndActiveTrue(@Param("employerId") Long employerId);

    List<Job> findByEmployerId(Long employerId);

    Page<Job> findByEmployerId(Long employerId, Pageable pageable);

    @Query("SELECT COUNT(j) FROM Job j WHERE j.createdAt <= :endDate")
    long countTotalJobsPostedBeforeOrEquals(@Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(j) FROM Job j")
    long countAllJobs();

    // Hàm tìm kiếm linh hoạt hơn
    @Query(QueryConstants.FIND_JOBS_BY_CRITERIA)
    List<Job> findJobsByCriteria(@Param("title") String title,
                                 @Param("location") String location,
                                 @Param("minSalary") Float minSalary,
                                 @Param("maxSalary") Float maxSalary,
                                 @Param("categoryName") String categoryName,
                                 @Param("jobLevelName") String jobLevelName,
                                 @Param("jobTypeName") String jobTypeName,
                                 @Param("employerName") String employerName);

    @Query("SELECT j.category.name, COUNT(j.id) " +
            "FROM Job j " +
            "GROUP BY j.category.name " +
            "ORDER BY COUNT(j.id) DESC") // Sắp xếp theo số lượng giảm dần
    List<Object[]> countTotalJobsByCategory();

    @Query("SELECT j FROM Job j ORDER BY j.createdAt DESC")
    List<Job> findTopNJobs(Pageable pageable);

    @Query("""
    SELECT j FROM Job j
    WHERE NOT EXISTS (
        SELECT 1 FROM SavedJob s
        WHERE s.job.id = j.id AND s.jobSeeker.id = :userId
    )
""")
    Page<Job> findAllJobsNotSavedByUser(@Param("userId") Long userId, Pageable pageable);

    @Query("""
    SELECT j FROM Job j
    WHERE NOT EXISTS (
        SELECT 1 FROM SavedJob s
        WHERE s.job.id = j.id AND s.jobSeeker.id = :userId
    )
""")
    List<Long> findAllJobsNotSavedByJobSeeker(@Param("userId") Long userId);

    @Query("""
    SELECT j FROM Job j
    WHERE j.active = true
    AND j.id NOT IN (
        SELECT sj.job.id FROM SavedJob sj WHERE sj.jobSeeker.id = :userId
    )
""")
    Page<Job> findAllActiveJobsNotSavedByUser(@Param("userId") Long userId, Pageable pageable);


    @Query("SELECT j FROM Job j WHERE j.active = true")
    Page<Job> findAllActive(Pageable pageable);

    @Query("""
    SELECT j FROM Job j
    WHERE j.active = true
    AND j.employer.id IN (
        SELECT sj.job.employer.id
        FROM SavedJob sj
        WHERE sj.jobSeeker.id = :userId
    )
    ORDER BY j.createdAt DESC
""")
    List<Job> findLatestJobsFromSavedEmployers(@Param("userId") Long userId, Pageable pageable);

    @Query("""
    SELECT j FROM Job j
    WHERE j.active = true
    AND j.employer.id IN (
        SELECT sj.job.employer.id
        FROM SavedJob sj
        WHERE sj.jobSeeker.id = :userId
    )
    ORDER BY j.createdAt DESC
""")
    List<Job> findRecentJobsFromSavedEmployers(
            @Param("userId") Long userId,
            @Param("fromTime") LocalDateTime fromTime,
            Pageable pageable);

}
