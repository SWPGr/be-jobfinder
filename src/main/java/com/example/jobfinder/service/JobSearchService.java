package com.example.jobfinder.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.example.jobfinder.dto.job.JobResponse;
import com.example.jobfinder.dto.job.JobSearchRequest;
import com.example.jobfinder.dto.job.JobSearchResponse;
import com.example.jobfinder.mapper.JobDocumentMapper;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import com.example.jobfinder.model.Job;
import com.example.jobfinder.model.JobDocument;
import com.example.jobfinder.model.User;
import com.example.jobfinder.model.SearchHistory;

import com.example.jobfinder.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import co.elastic.clients.elasticsearch.core.search.Hit;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class JobSearchService {
    private static final Logger log = LoggerFactory.getLogger(JobSearchService.class);
    private static final DateTimeFormatter CREATED_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter EXPIRED_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final ElasticsearchClient client;
    private final UserRepository userRepository;
    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final JobDocumentMapper jobDocumentMapper;
    private final SearchHistoryRepository searchHistoryRepository;

    public JobSearchResponse search(JobSearchRequest request) throws IOException {
        List<Query> mustQueries = new ArrayList<>();

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            String keyword = request.getKeyword().trim();

            mustQueries.add(Query.of(q -> q.bool(b -> b.should(
                    List.of(
                            Query.of(q1 -> q1.multiMatch(m -> m
                                    .fields("title", "description")
                                    .query(keyword)
                            )),
                            Query.of(q2 -> q2.matchPhrasePrefix(m -> m
                                    .field("title")
                                    .query(keyword)
                            ))
                    )
            ).minimumShouldMatch("1"))));
        }

        if (request.getSalaryNegotiable() != null && request.getSalaryNegotiable()) {
            Query noSalaryQuery = Query.of(q -> q.bool(b -> b
                .mustNot(
                    Query.of(q1 -> q1.exists(e -> e.field("salaryMin"))),
                    Query.of(q2 -> q2.exists(e -> e.field("salaryMax")))
                )
            ));
            mustQueries.add(noSalaryQuery);
            
        } else if (request.getSalaryMin() != null || request.getSalaryMax() != null) {
            Query hasSalaryMinQuery = Query.of(q -> q.exists(e -> e.field("salaryMin")));
            mustQueries.add(hasSalaryMinQuery);
            
            Query hasSalaryMaxQuery = Query.of(q -> q.exists(e -> e.field("salaryMax")));
            mustQueries.add(hasSalaryMaxQuery);
            
            if (request.getSalaryMin() != null && request.getSalaryMax() != null) {
                Query salaryMinQuery = RangeQuery.of(r -> r
                    .number(n -> n
                        .field("salaryMin")
                        .gte(request.getSalaryMin().doubleValue())
                    )
                )._toQuery();
                mustQueries.add(salaryMinQuery);

                Query salaryMaxQuery = RangeQuery.of(r -> r
                    .number(n -> n
                        .field("salaryMax")
                        .lte(request.getSalaryMax().doubleValue())
                    )
                )._toQuery();
                mustQueries.add(salaryMaxQuery);
                
            } else if (request.getSalaryMin() != null) {
                Query salaryMinQuery = RangeQuery.of(r -> r
                    .number(n -> n
                        .field("salaryMin")
                        .gte(request.getSalaryMin().doubleValue())
                    )
                )._toQuery();
                mustQueries.add(salaryMinQuery);
                
            } else if (request.getSalaryMax() != null) {
                Query salaryMaxQuery = RangeQuery.of(r -> r
                    .number(n -> n
                        .field("salaryMax")
                        .lte(request.getSalaryMax().doubleValue())
                    )
                )._toQuery();
                mustQueries.add(salaryMaxQuery);
            }
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

        if (request.getExperienceId() != null) {
            mustQueries.add(termQuery("experience", request.getExperienceId()));
        }

        mustQueries.add(Query.of(q -> q.term(t -> t
                .field("active")
                .value(true)
        )));

        Query finalQuery = mustQueries.isEmpty()
                ? Query.of(q -> q.matchAll(m -> m))
                : Query.of(q -> q.bool(b -> b.must(mustQueries)));

        int safePage = Math.max(1, request.getPage());
        int size = request.getSize();

        SearchResponse<JobDocument> response = client.search(s -> {
            var searchRequest = s
                    .index("jobs")
                    .query(finalQuery)
                    .from((safePage - 1) * size)
                    .size(size);

            if (request.getSort() != null) {
                searchRequest = searchRequest.sort(srt -> srt
                        .field(f -> f
                                .field("createdAt")
                                .order("asc".equalsIgnoreCase(request.getSort())
                                        ? SortOrder.Asc
                                        : SortOrder.Desc)
                        ));
            }

            return searchRequest;
        }, JobDocument.class);
        log.info("Searching with filters: {}", mustQueries);


        List<JobDocument> jobs = response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();

        setIsSaveStatus(jobs);

        long totalHits = response.hits().total() != null
                ? response.hits().total().value()
                : jobs.size();

        List<JobResponse> jobResponses = jobs.stream()
                .map(jobDocumentMapper::toJobResponse)
                .toList();

        saveSearchHistory(request);

        return JobSearchResponse.builder()
                .data(jobResponses)
                .totalHits(totalHits)
                .page(safePage)
                .size(size)
                .build();
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

    private void setIsSaveStatus(List<JobDocument> jobs) {
        jobs.forEach(job -> job.setIsSave(false));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                List<Long> savedJobIds = savedJobRepository.findSavedJobIdsByUserId(user.getId());
                log.info("User {} has {} saved jobs", email, savedJobIds.size());
                
                for (JobDocument job : jobs) {
                    if (job.getId() != null && savedJobIds.contains(job.getId())) {
                        job.setIsSave(true);
                        log.debug("Job {} marked as saved for user {}", job.getId(), email);
                    }
                }
            }
        }
    }

    public JobSearchResponse searchWithIsSaveStatus(JobSearchRequest request) throws IOException {
        return search(request);
    }

    public List<JobDocument> getAllJobsWithIsSaveStatus() {
        List<Job> allJobs = jobRepository.findAll();
        log.info("Found {} jobs in database", allJobs.size());

        List<JobDocument> jobDocuments = allJobs.stream()
                .map(this::convertToJobDocument)
                .toList();

        log.info("Converted {} jobs to JobDocuments", jobDocuments.size());

        setIsSaveStatus(jobDocuments);

        return jobDocuments;
    }

    private JobDocument convertToJobDocument(Job job) {
        JobDocument doc = new JobDocument();
        doc.setId(job.getId());
        doc.setTitle(job.getTitle());
        doc.setDescription(job.getDescription());
        doc.setLocation(job.getLocation());
        doc.setEmployerId(job.getEmployer().getId());
        doc.setCategoryId(job.getCategory().getId());
        doc.setJobLevelId(job.getJobLevel().getId());
        if (job.getExperience() != null) {
            doc.setExperience(job.getExperience().getId());
        }
        doc.setSalaryMin(job.getSalaryMin());
        doc.setSalaryMax(job.getSalaryMax());
        doc.setJobTypeId(job.getJobType().getId());
        doc.setEducationId(job.getEducation().getId());
        doc.setActive(job.getActive());
        doc.setIsSave(false);
        doc.setExpiredDate(job.getExpiredDate() != null
                ? job.getExpiredDate().format(EXPIRED_DATE_FORMATTER)
                : null);

        doc.setCreatedAt(job.getCreatedAt() != null
                ? job.getCreatedAt().format(CREATED_AT_FORMATTER)
                : null);

        log.debug("Converted Job {} to JobDocument with title: {}", job.getId(), doc.getTitle());
        return doc;
    }

    private void saveSearchHistory(JobSearchRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String email = auth.getName();
                User user = userRepository.findByEmail(email).orElse(null);

                if (user != null) {
                    String searchQuery = buildSearchQueryString(request);

                    if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                        SearchHistory lastSearchHistory = searchHistoryRepository
                                .findFirstByUserAndSearchTypeOrderByCreatedAtDesc(user, SearchHistory.SearchType.JOB);

                        boolean isDuplicate = lastSearchHistory != null &&
                                normalizeForComparison(searchQuery).equals(
                                        normalizeForComparison(lastSearchHistory.getSearchQuery())
                                );

                        if (!isDuplicate) {
                            SearchHistory searchHistory = SearchHistory.builder()
                                    .user(user)
                                    .searchQuery(searchQuery)
                                    .searchType(SearchHistory.SearchType.JOB)
                                    .build();
                            searchHistoryRepository.save(searchHistory);
                            log.debug("Saved new job search history for user {}: {}", email, searchQuery);

                            cleanupOldSearchHistory(user, 50);
                        } else {
                            log.debug("Skipped saving duplicate job search history for user {}: {}", email, searchQuery);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to save job search history: {}", e.getMessage());
        }
    }

    private void cleanupOldSearchHistory(User user, int maxRecords) {
        try {
            long totalRecords = searchHistoryRepository.countByUserAndSearchType(user, SearchHistory.SearchType.JOB);
            if (totalRecords > maxRecords) {
                List<SearchHistory> allHistories = searchHistoryRepository
                        .findByUserAndSearchTypeOrderByCreatedAtDesc(user, SearchHistory.SearchType.JOB);

                int recordsToDelete = (int) (totalRecords - maxRecords);
                List<SearchHistory> historiesToDelete = allHistories.subList(0, recordsToDelete);
                searchHistoryRepository.deleteAll(historiesToDelete);

                log.debug("Cleaned up {} old job search history records for user {}", recordsToDelete, user.getEmail());
            }
        } catch (Exception e) {
            log.error("Failed to cleanup old job search history: {}", e.getMessage());
        }
    }

    private String normalizeForComparison(String text) {
        if (text == null) return null;
        return text.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private String buildSearchQueryString(JobSearchRequest request) {
        List<String> queryParts = new ArrayList<>();
        
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            queryParts.add(request.getKeyword().trim());
        }
        
        return queryParts.isEmpty() ? null : String.join(", ", queryParts);
    }
}
