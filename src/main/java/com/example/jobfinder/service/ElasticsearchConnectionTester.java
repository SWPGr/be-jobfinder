package com.example.jobfinder.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.InfoResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchConnectionTester {

    private final ElasticsearchClient elasticsearchClient;

    public ElasticsearchConnectionTester(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

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
