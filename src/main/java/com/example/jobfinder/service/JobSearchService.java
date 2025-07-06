package com.example.jobfinder.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.example.jobfinder.dto.job.JobSearchRequest;
import com.example.jobfinder.model.Category;
import com.example.jobfinder.model.JobDocument;
import com.example.jobfinder.repository.CategoryRepository;
import com.example.jobfinder.repository.EducationRepository;
import com.example.jobfinder.repository.JobLevelRepository;
import com.example.jobfinder.repository.JobTypeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import co.elastic.clients.elasticsearch.core.search.Hit;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class JobSearchService {
    private static final Logger log = LoggerFactory.getLogger(JobSearchService.class);

    private final ElasticsearchClient client;

    public List<JobDocument> search(JobSearchRequest request) throws IOException {
        List<Query> mustQueries = new ArrayList<>();

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            mustQueries.add(Query.of(q -> q
                    .multiMatch(m -> m
                            .fields("title", "description")
                            .query(request.getKeyword())
                    )));
        }

        if (request.getLocation() != null)
            mustQueries.add(matchQuery("location", request.getLocation()));

        if (request.getCategoryId() != null)
                mustQueries.add(termQuery("categoryId", request.getCategoryId()));

        if (request.getJobLevelId() != null)
            mustQueries.add(termQuery("jobLevelId", request.getJobLevelId()));

        if (request.getJobTypeId() != null)
            mustQueries.add(termQuery("jobTypeId", request.getJobTypeId()));

        if (request.getEducationId() != null)
            mustQueries.add(termQuery("educationId", request.getEducationId()));

        Query finalQuery = mustQueries.isEmpty()
                ? Query.of(q -> q.matchAll(m -> m))
                : Query.of(q -> q.bool(b -> b.must(mustQueries)));

        SearchResponse<JobDocument> response = client.search(s -> s
                        .index("jobs")
                        .query(finalQuery)
                        .size(50),
                JobDocument.class
        );
        log.info("Searching with filters: {}", mustQueries);


        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }

    private Query termQuery(String field, Long value) {
        return Query.of(q -> q.term(t -> t
                .field(field)
                .value(value)
        ));
    }

    private Query matchQuery(String field, String value) {
        return Query.of(q -> q.term(t -> t.field(field + ".keyword").value(value)));
    }

}
