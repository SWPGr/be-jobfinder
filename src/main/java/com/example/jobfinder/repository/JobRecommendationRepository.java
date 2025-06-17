package com.example.jobfinder.repository;

import com.example.jobfinder.model.JobRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRecommendationRepository extends JpaRepository<JobRecommendation, Long> {
    /****
 * Retrieves all job recommendations for the specified job seeker, ordered by recommendation score in descending order.
 *
 * @param jobSeekerId the unique identifier of the job seeker
 * @return a list of job recommendations sorted by score from highest to lowest
 */
List<JobRecommendation> findByJobSeekerIdOrderByScoreDesc(Long jobSeekerId);
    /****
 * Retrieves a job recommendation for a specific job seeker and job.
 *
 * @param jobSeekerId the ID of the job seeker
 * @param jobId the ID of the job
 * @return an Optional containing the matching JobRecommendation if found, or empty if not found
 */
Optional<JobRecommendation> findByJobSeekerIdAndJobId(Long jobSeekerId, Long jobId);
    /****
 * Deletes all job recommendations associated with the specified job seeker ID.
 *
 * @param jobSeekerId the ID of the job seeker whose recommendations will be removed
 */
void deleteByJobSeekerId(Long jobSeekerId);
}
