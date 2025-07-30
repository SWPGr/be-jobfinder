package com.example.jobfinder.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.InfoResponse;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class ElasticsearchConnectionTester {

    ElasticsearchClient elasticsearchClient;

    @PostConstruct
    public void testConnection() {
        try {
            InfoResponse info = elasticsearchClient.info();
            System.out.println("Elasticsearch connected!");
            System.out.println("Cluster name: " + info.clusterName());
            System.out.println("Elasticsearch version: " + info.version().number());
        } catch (Exception e) {
            System.err.println("Failed to connect to Elasticsearch: ");
            e.printStackTrace();
        }
    }
}
