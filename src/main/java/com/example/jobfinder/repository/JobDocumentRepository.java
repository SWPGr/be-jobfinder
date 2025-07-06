package com.example.jobfinder.repository;

import com.example.jobfinder.model.JobDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobDocumentRepository extends ElasticsearchRepository<JobDocument, Long> {
    List<JobDocument> findByTitleContainingIgnoreCase(String title);

    List<JobDocument> findByCategoryId(Long categoryId);

    List<JobDocument> findByLocationContainingIgnoreCase(String location);

    List<JobDocument> findByEducationId(Long educationId);

}
