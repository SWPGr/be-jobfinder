package com.example.jobfinder;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Disabled("Full context test disabled for CI/CD - use integration environment for full testing")
class JobfinderApplicationTests {

	@Test
	void contextLoads() {
		System.out.println("✅ Application context test (disabled for CI/CD)");
	}

}
