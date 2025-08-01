package com.example.jobfinder.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.Base64;

@Configuration
public class ElasticsearchConfig{

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUrl;

    @Value("${spring.elasticsearch.username}")
    private String userName;

    @Value("${spring.elasticsearch.password}")
    private String password;


    @Bean
    public ElasticsearchClient elasticsearchClient() throws Exception {
        URI uri = new URI(elasticsearchUrl);
        HttpHost host = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());

        String credentials = Base64.getEncoder().encodeToString((userName + ":" + password).getBytes());

        Header[] defaultHeaders = new Header[]{
                new BasicHeader("Authorization", "Basic " + credentials)
        };

        RestClient restClient = RestClient.builder(host)
                .setDefaultHeaders(defaultHeaders)
                .build();

        RestClientTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper(new ObjectMapper()
                        .disable(MapperFeature.AUTO_DETECT_CREATORS,
                                MapperFeature.AUTO_DETECT_FIELDS,
                                MapperFeature.AUTO_DETECT_GETTERS)
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false))
        );

        return new ElasticsearchClient(transport);
    }
}