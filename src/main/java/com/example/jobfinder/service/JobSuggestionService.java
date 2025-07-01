package com.example.jobfinder.service;

import com.example.jobfinder.model.JobDocument;
import com.example.jobfinder.repository.JobDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class JobSuggestionService {
    private static final Logger log = LoggerFactory.getLogger(JobSuggestionService.class);
    private final JobDocumentRepository jobDocumentRepository;

    public JobSuggestionService(JobDocumentRepository jobDocumentRepository) {
        this.jobDocumentRepository = jobDocumentRepository;
    }

    public List<String> suggestTitles(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        try {
            return jobDocumentRepository.findByTitleContainingIgnoreCase(keyword)
                    .stream()
                    .map(JobDocument::getTitle)
                    .filter(title -> title != null && !title.trim().isEmpty())
                    .distinct()
                    .limit(5)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error during title suggestion search for keyword: {}", keyword, e);
            return List.of();
        }
    }

}
