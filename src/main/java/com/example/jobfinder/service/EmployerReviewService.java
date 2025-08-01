package com.example.jobfinder.service;

import com.example.jobfinder.dto.employer_review.EmployerReviewRequest;
import com.example.jobfinder.dto.employer_review.EmployerReviewResponse;
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.example.jobfinder.mapper.EmployerReviewMapper;
import com.example.jobfinder.model.EmployerReview;
import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.ApplicationRepository;
import com.example.jobfinder.repository.EmployerReviewRepository;
import com.example.jobfinder.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployerReviewService {

    EmployerReviewRepository employerReviewRepository;
    UserRepository userRepository;
    EmployerReviewMapper employerReviewMapper;
    AuthService authService;
    ApplicationRepository applicationRepository;
    @Transactional
    public EmployerReviewResponse createEmployerReview(EmployerReviewRequest request) {
        User jobSeeker = authService.getAuthenticatedUser();
        Long jobSeekerId = jobSeeker.getId();

        if (!jobSeeker.getRole().getName().equals("JOB_SEEKER")) {
            throw new AppException(ErrorCode.UNAUTHORIZED_REVIEW_ACTION);
        }
        User employer = userRepository.findById(request.getEmployerId())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYER_NOT_FOUND));
        if (!employer.getRole().getName().equals("EMPLOYER")) {
            throw new AppException(ErrorCode.EMPLOYER_NOT_FOUND);
        }

        if (jobSeekerId.equals(request.getEmployerId())) {
            throw new AppException(ErrorCode.CANNOT_REVIEW_SELF);
        }

        if (employerReviewRepository.findByJobSeekerIdAndEmployerId(jobSeekerId, request.getEmployerId()).isPresent()) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        boolean hasAppliedToEmployer = applicationRepository.existsByJobSeeker_IdAndJob_Employer_Id(
                jobSeekerId, request.getEmployerId());
        if (!hasAppliedToEmployer) {
            throw new AppException(ErrorCode.REVIEW_UNAUTHORIZED_NO_RELATION);
        }
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new AppException(ErrorCode.INVALID_RATING_VALUE);
        }
        EmployerReview employerReview = employerReviewMapper.toEmployerReview(request);
        employerReview.setJobSeeker(jobSeeker);
        employerReview.setEmployer(employer);

        EmployerReview savedReview = employerReviewRepository.save(employerReview);
        return employerReviewMapper.toEmployerReviewResponse(savedReview);
    }

    public List<EmployerReviewResponse> getReviewsForEmployer(Long employerId) {
        List<EmployerReview> reviews = employerReviewRepository.findByEmployerId(employerId);
        return employerReviewMapper.toEmployerReviewResponseList(reviews);
    }

    public EmployerReviewResponse getMyReviewForEmployer(Long employerId) {
        Long jobSeekerId = authService.getAuthenticatedUserId();
        EmployerReview review = employerReviewRepository.findByJobSeekerIdAndEmployerId(jobSeekerId, employerId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
        if (!review.getJobSeeker().getId().equals(jobSeekerId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_REVIEW_ACTION);
        }

        return employerReviewMapper.toEmployerReviewResponse(review);
    }

    public List<EmployerReviewResponse> getMyReviews() {
        Long jobSeekerId = authService.getAuthenticatedUserId();

        List<EmployerReview> reviews = employerReviewRepository.findByJobSeekerId(jobSeekerId);
        return employerReviewMapper.toEmployerReviewResponseList(reviews);
    }

    @Transactional
    public EmployerReviewResponse updateEmployerReview(Long reviewId, EmployerReviewRequest request) {
        Long jobSeekerId = authService.getAuthenticatedUserId();

        EmployerReview existingReview = employerReviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
        if (!existingReview.getJobSeeker().getId().equals(jobSeekerId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_REVIEW_ACTION);
        }


        if (!existingReview.getEmployer().getId().equals(request.getEmployerId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_REVIEW_ACTION); // Không cho phép thay đổi employer được review
        }
        existingReview.setRating(request.getRating());
        existingReview.setComment(request.getComment());

        EmployerReview updatedReview = employerReviewRepository.save(existingReview);
        return employerReviewMapper.toEmployerReviewResponse(updatedReview);
    }

    @Transactional
    public void deleteEmployerReview(Long reviewId) {
        User currentUser = authService.getAuthenticatedUser();
        Long currentUserId = currentUser.getId();

        EmployerReview existingReview = employerReviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (!existingReview.getJobSeeker().getId().equals(currentUserId) &&
                !currentUser.getRole().getName().equals("ADMIN")) {
            throw new AppException(ErrorCode.UNAUTHORIZED_REVIEW_ACTION);
        }

        employerReviewRepository.delete(existingReview);
    }
}