package com.example.jobfinder.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.example.jobfinder.model.JobDocument;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class JobSuggestionService {
    Logger log = LoggerFactory.getLogger(JobSuggestionService.class);
    ElasticsearchClient client;

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
