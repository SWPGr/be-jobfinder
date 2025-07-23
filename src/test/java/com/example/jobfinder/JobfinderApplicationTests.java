package com.example.jobfinder;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    // Force H2 database
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    
    // JPA configuration
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.show-sql=false",
    "spring.jpa.defer-datasource-initialization=true",
    
    // Completely disable external services
    "spring.autoconfigure.exclude=" +
        "org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration," +
        "org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration," +
        "org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration",
    
    // Disable scheduler
    "app.scheduler.enabled=false",
    "spring.task.scheduling.pool.size=0",
    
    // Mock all external service properties
    "spring.mail.host=localhost",
    "spring.mail.port=25",
    "google.gemini.api-key=test-key",
    "spring.elasticsearch.uris=http://localhost:9200",
    "cloudinary.cloud-name=test-cloud",
    "cloudinary.api-key=test-key",
    "cloudinary.api-secret=test-secret",
    "payos.client-id=test-client",
    "payos.api-key=test-key",
    "payos.checksum-key=test-checksum",
    "spring.security.oauth2.client.registration.google.client-id=test-id",
    "spring.security.oauth2.client.registration.google.client-secret=test-secret",
    "jwt.secret=test-jwt-secret-key-for-testing-purposes-only-must-be-long-enough",
    "jwt.expiration=3600000",
    "app.frontend.url=http://localhost:3030",
    
    // Suppress all logging
    "logging.level.root=ERROR",
    "logging.level.org.apache.http=OFF",
    "logging.level.org.elasticsearch=OFF",
    "logging.level.co.elastic=OFF"
})
class JobfinderApplicationTests {

	@Test
	void contextLoads() {
	}

}
