package com.example.jobfinder.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.example.jobfinder.model.UserDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployerSuggestionService {
    private static final Logger log = LoggerFactory.getLogger(EmployerSuggestionService.class);
    private final ElasticsearchClient client;

    public EmployerSuggestionService(ElasticsearchClient client) {
        this.client = client;
    }

    /**
     * Suggest company names based on keyword input
     */
    public List<String> suggestCompanyNames(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return List.of();

        try {
            var response = client.search(s -> s
                            .index("users")
                            .query(q -> q
                                    .bool(b -> b
                                            .must(
                                                    // Filter by EMPLOYER role
                                                    Query.of(query -> query.term(t -> t
                                                            .field("roleId")
                                                            .value(2L) // EMPLOYER role ID
                                                    )),
                                                    // Match phrase prefix for company name
                                                    Query.of(query -> query.matchPhrasePrefix(m -> m
                                                            .field("companyName")
                                                            .query(keyword)
                                                    ))
                                            )
                                    )
                            )
                            .size(10), // Limit to 10 suggestions
                    UserDocument.class
            );

            return response.hits().hits().stream()
                    .map(hit -> hit.source() != null ? hit.source().getCompanyName() : null)
                    .filter(companyName -> companyName != null && !companyName.trim().isEmpty())
                    .distinct()
                    .limit(10)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error during company name suggestion search for keyword: {}", keyword, e);
            return List.of();
        }
    }
}
