package com.example.jobfinder.repository;

import com.example.jobfinder.model.JobDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobDocumentRepository extends ElasticsearchRepository<JobDocument, Long> {
    List<JobDocument> findByTitleContainingIgnoreCase(String title);
    List<JobDocument> findByCategoryContainingIgnoreCase(String category);
    List<JobDocument> findByLocationContainingIgnoreCase(String location);

    @Query("{\"bool\": {\"should\": [{\"wildcard\": {\"title\": \"*?0*\"}}, {\"wildcard\": {\"category\": \"*?0*\"}}, {\"wildcard\": {\"location\": \"*?0*\"}}]}}")
    List<JobDocument> findByKeywordInTitleCategoryOrLocation(String keyword);
}
