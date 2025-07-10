package com.example.jobfinder.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
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
    private final ElasticsearchClient client;

    public JobSuggestionService(ElasticsearchClient client) {
        this.client = client;
    }

    public List<String> suggestTitles(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return List.of();

        try {
            var response = client.search(s -> s
                            .index("jobs")
                            .query(q -> q
                                    .matchPhrasePrefix(m -> m
                                            .field("title")
                                            .query(keyword)
                                    )
                            )
                            .size(5),
                    JobDocument.class
            );

            return response.hits().hits().stream()
                    .map(hit -> hit.source() != null ? hit.source().getTitle() : null)
                    .filter(title -> title != null && !title.trim().isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error during title suggestion search for keyword: {}", keyword, e);
            return List.of();
        }
    }

}
