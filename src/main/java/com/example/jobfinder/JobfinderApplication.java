package com.example.jobfinder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class JobfinderApplication implements CommandLineRunner { // implements CommandLineRunner

	public static void main(String[] args) {
		SpringApplication.run(JobfinderApplication.class, args);
	}

	// Bean này sẽ được Spring Boot tự động chạy sau khi ApplicationContext được tạo
	@Override
	public void run(String... args) throws Exception {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String hashedPassword = passwordEncoder.encode("1234");
		System.out.println("Hashed password for '1234': " + hashedPassword);
	}
}