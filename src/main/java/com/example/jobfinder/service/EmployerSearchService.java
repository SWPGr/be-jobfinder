package com.example.jobfinder.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.jobfinder.dto.employer.EmployerSearchRequest;
import com.example.jobfinder.dto.employer.EmployerSearchResponse;
import com.example.jobfinder.dto.user.UserResponse;
import com.example.jobfinder.mapper.UserDocumentMapper;
import com.example.jobfinder.model.UserDocument;
import com.example.jobfinder.model.User;
import com.example.jobfinder.model.SearchHistory;
import com.example.jobfinder.repository.UserRepository;
import com.example.jobfinder.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmployerSearchService {
    
    private static final Logger log = LoggerFactory.getLogger(EmployerSearchService.class);

    private final ElasticsearchClient client;
    private final UserDocumentMapper userDocumentMapper;
    private final UserRepository userRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    public EmployerSearchResponse search(EmployerSearchRequest request) throws IOException {
        List<Query> mustQueries = new ArrayList<>();

        mustQueries.add(Query.of(q -> q.term(t -> t
                .field("roleId")
                .value(2L)
        )));

        if (request.getName() != null && !request.getName().isBlank()) {
            String name = request.getName().trim();
            
            mustQueries.add(Query.of(q -> q.bool(b -> b.should(
                    List.of(
                            Query.of(q1 -> q1.multiMatch(m -> m
                                    .fields( "companyName")
                                    .query(name)
                            )),
                            Query.of(q3 -> q3.matchPhrasePrefix(m -> m
                                    .field("companyName")
                                    .query(name)
                            ))
                    )
            ).minimumShouldMatch("1"))));
        }

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            String keyword = request.getKeyword().trim();
            
            mustQueries.add(Query.of(q -> q.bool(b -> b.should(
                    List.of(
                            Query.of(q1 -> q1.multiMatch(m -> m
                                    .fields("companyName", "description", "location")
                                    .query(keyword)
                            )),
                            Query.of(q2 -> q2.matchPhrasePrefix(m -> m
                                    .field("companyName")
                                    .query(keyword)
                            ))
                    )
            ).minimumShouldMatch("1"))));
        }

        if (request.getLocation() != null && !request.getLocation().isBlank()) {
            mustQueries.add(matchQuery("location", request.getLocation()));
        }

        if (request.getOrganizationId() != null) {
            mustQueries.add(termQuery("organizationId", request.getOrganizationId()));
        }
        

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

        log.info("Employer search with filters: {}", mustQueries);

        List<UserDocument> employers = response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();

        long totalHits = response.hits().total() != null
                ? response.hits().total().value()
                : employers.size();

        List<UserResponse> employerResponses = employers.stream()
                .map(userDocumentMapper::toUserResponse)
                .toList();

        saveEmployerSearchHistory(request);

        return EmployerSearchResponse.builder()
                .data(employerResponses)
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

    private void saveEmployerSearchHistory(EmployerSearchRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String email = auth.getName();
                User user = userRepository.findByEmail(email).orElse(null);
                
                if (user != null) {
                    String searchQuery = buildEmployerSearchQueryString(request);
                    
                    if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                        SearchHistory lastSearchHistory = searchHistoryRepository.findFirstByUserOrderByCreatedAtDesc(user);
                        
                        boolean isDuplicate = lastSearchHistory != null &&
                                normalizeForComparison(searchQuery).equals(
                                    normalizeForComparison(lastSearchHistory.getSearchQuery())
                                );
                        
                        if (!isDuplicate) {
                            SearchHistory searchHistory = SearchHistory.builder()
                                    .user(user)
                                    .searchQuery(searchQuery)
                                    .build();
                            
                            searchHistoryRepository.save(searchHistory);
                            log.debug("Saved new employer search history for user {}: {}", email, searchQuery);
                            
                            cleanupOldSearchHistory(user, 50);
                        } else {
                            log.debug("Skipped saving duplicate employer search history for user {}: {} (normalized comparison)", email, searchQuery);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to save employer search history: {}", e.getMessage());
        }
    }

    private void cleanupOldSearchHistory(User user, int maxRecords) {
        try {
            long totalRecords = searchHistoryRepository.countByUser(user);
            if (totalRecords > maxRecords) {
                List<SearchHistory> allHistories = searchHistoryRepository.findByUserOrderByCreatedAtAsc(user);
                int recordsToDelete = (int) (totalRecords - maxRecords);
                
                List<SearchHistory> historiesToDelete = allHistories.subList(0, recordsToDelete);
                searchHistoryRepository.deleteAll(historiesToDelete);
                
                log.debug("Cleaned up {} old search history records for user {}", recordsToDelete, user.getEmail());
            }
        } catch (Exception e) {
            log.error("Failed to cleanup old search history: {}", e.getMessage());
        }
    }

    private String buildEmployerSearchQueryString(EmployerSearchRequest request) {
        List<String> queryParts = new ArrayList<>();
        
        queryParts.add("[EMPLOYER]");
        
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            queryParts.add("name: " + request.getName().trim());
        }
        
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            queryParts.add("keyword: " + request.getKeyword().trim());
        }
        
        return queryParts.size() > 1 ? String.join(", ", queryParts) : null;
    }


    private String normalizeForComparison(String text) {
        if (text == null) return null;
        return text.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
