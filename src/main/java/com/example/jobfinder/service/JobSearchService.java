package com.example.jobfinder.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.example.jobfinder.dto.job.JobSearchRequest;
import com.example.jobfinder.model.JobDocument;
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

        if (request.getCategory() != null)
            mustQueries.add(matchQuery("category", request.getCategory()));

        if (request.getJobLevel() != null)
            mustQueries.add(matchQuery("jobLevel", request.getJobLevel()));

        if (request.getJobType() != null)
            mustQueries.add(matchQuery("jobType", request.getJobType()));

        if (request.getEducation() != null)
            mustQueries.add(matchQuery("education", request.getEducation()));

        Query finalQuery = mustQueries.isEmpty()
                ? Query.of(q -> q.matchAll(m -> m))
                : Query.of(q -> q.bool(b -> b.must(mustQueries)));

        SearchResponse<JobDocument> response = client.search(s -> s
                        .index("jobs")
                        .query(finalQuery)
                        .size(50),
                JobDocument.class
        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }

    private Query matchQuery(String field, String value) {
        return Query.of(q -> q.match(m -> m.field(field).query(value)));
    }

}
