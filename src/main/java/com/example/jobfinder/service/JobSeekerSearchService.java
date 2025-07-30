package com.example.jobfinder.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.jobfinder.dto.job_seeker.JobSeekerSearchRequest;
import com.example.jobfinder.dto.job_seeker.JobSeekerSearchResponse;
import com.example.jobfinder.dto.user.UserResponse;
import com.example.jobfinder.mapper.UserDocumentMapper;
import com.example.jobfinder.model.UserDocument;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobSeekerSearchService {

    static Logger log = LoggerFactory.getLogger(JobSeekerSearchService.class);

    ElasticsearchClient client;
    UserDocumentMapper userDocumentMapper;

    public JobSeekerSearchResponse search(JobSeekerSearchRequest request) throws IOException {
        List<Query> mustQueries = new ArrayList<>();
        mustQueries.add(Query.of(q -> q.term(t -> t
                .field("roleId")
                .value(1L) // Assuming JOB_SEEKER role has ID = 1
        )));


        if (request.getEducationId() != null) {
            mustQueries.add(termQuery("educationId", request.getEducationId()));
        }

        // Experience filter
        if (request.getExperienceId() != null) {
            mustQueries.add(termQuery("experienceId", request.getExperienceId()));
        }

        // Location filter
        if (request.getLocation() != null && !request.getLocation().isBlank()) {
            mustQueries.add(matchQuery("location", request.getLocation()));
        }


        // Build final query
        Query finalQuery = mustQueries.isEmpty()
                ? Query.of(q -> q.matchAll(m -> m))
                : Query.of(q -> q.bool(b -> b.must(mustQueries)));

        int safePage = Math.max(1, request.getPage());
        int size = request.getSize();

        SearchResponse<UserDocument> response = client.search(s -> {
            var searchRequest = s
                    .index("users")
                    .query(finalQuery)
                    .from((safePage - 1) * size)
                    .size(size);

            return searchRequest;
        }, UserDocument.class);

        log.info("Job seeker search with filters: {}", mustQueries);

        List<UserDocument> jobSeekers = response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();

        long totalHits = response.hits().total() != null
                ? response.hits().total().value()
                : jobSeekers.size();

        List<UserResponse> jobSeekerResponses = jobSeekers.stream()
                .map(userDocumentMapper::toUserResponse)
                .toList();

        return JobSeekerSearchResponse.builder()
                .data(jobSeekerResponses)
                .totalHits(totalHits)
                .page(safePage)
                .size(size)
                .build();
    }

    private Query termQuery(String field, Object value) {
        return Query.of(q -> q.term(t -> t
                .field(field)
                .value(v -> v.stringValue(value.toString()))
        ));
    }
    private Query matchQuery(String field, String value) {
        return Query.of(q -> q.match(m -> m
                .field(field)
                .query(value)
        ));
    }
}
