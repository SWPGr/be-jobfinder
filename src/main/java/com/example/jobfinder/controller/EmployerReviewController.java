package com.example.jobfinder.controller;

import com.example.jobfinder.dto.employer_review.EmployerReviewRequest;
import com.example.jobfinder.dto.employer_review.EmployerReviewResponse;
import com.example.jobfinder.service.EmployerReviewService;
import jakarta.validation.Valid; // Để sử dụng validation trên DTO
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employer-reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployerReviewController {

    EmployerReviewService employerReviewService;

    @PostMapping
//    @PreAuthorize("hasRole('JOB_SEEKER')") // Chỉ Job Seeker mới được tạo review
    public ResponseEntity<EmployerReviewResponse> createReview(
            @RequestBody @Valid EmployerReviewRequest request) { // @Valid để kích hoạt validation trong DTO
        EmployerReviewResponse response = employerReviewService.createEmployerReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/by-employer/{employerId}")
    public ResponseEntity<List<EmployerReviewResponse>> getReviewsByEmployerId(@PathVariable Long employerId) {
        List<EmployerReviewResponse> reviews = employerReviewService.getReviewsForEmployer(employerId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/my-review/for-employer/{employerId}")
//    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<EmployerReviewResponse> getMyReviewForSpecificEmployer(@PathVariable Long employerId) {
        EmployerReviewResponse review = employerReviewService.getMyReviewForEmployer(employerId);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/my-reviews")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<List<EmployerReviewResponse>> getMyReviews() {
        List<EmployerReviewResponse> reviews = employerReviewService.getMyReviews();
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/{reviewId}")
//    @PreAuthorize("hasRole('JOB_SEEKER')") // Chỉ Job Seeker (người tạo review) mới được cập nhật
    public ResponseEntity<EmployerReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @RequestBody @Valid EmployerReviewRequest request) {
        EmployerReviewResponse response = employerReviewService.updateEmployerReview(reviewId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('JOB_SEEKER') or hasRole('ADMIN')") // Job Seeker hoặc Admin
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        employerReviewService.deleteEmployerReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}