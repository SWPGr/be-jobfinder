package com.example.jobfinder.repository;

import com.example.jobfinder.model.EmployerReview;
import com.example.jobfinder.model.User;
import com.example.jobfinder.util.QueryConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployerReviewRepository extends JpaRepository<EmployerReview, Long> {
    List<EmployerReview> findByEmployerId(Long employerId);

    Optional<EmployerReview> findByJobSeekerIdAndEmployerId(Long jobSeekerId, Long employerId);

    List<EmployerReview> findByJobSeekerId(Long jobSeekerId);

    @Query(QueryConstants.FIND_EMPLOYER_REVIEWS_BY_EMPLOYER_AND_RATING)
    List<EmployerReview> findEmployerReviewsByEmployerAndRating(@Param("employerId") Long employerId, // Sử dụng ID
                                                                @Param("minRating") Integer minRating,
                                                                @Param("maxRating") Integer maxRating);

    List<EmployerReview> findByEmployer(User employer);
}