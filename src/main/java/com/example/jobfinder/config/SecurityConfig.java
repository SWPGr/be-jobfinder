package com.example.jobfinder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",      // Các API liên quan đến xác thực (đăng ký, đăng nhập, quên mật khẩu...)
                                "/api/debug/**",     // Các endpoint debug (nếu có và bạn muốn cho phép truy cập public)
                                "/api/profiles/me",  // Có thể bạn muốn /api/profiles/me yêu cầu xác thực, cân nhắc lại.
                                // Nếu /api/profiles/** là public, thì /me cũng public.
                                "/api/profiles/**",  // Các API liên quan đến hồ sơ người dùng (nếu có public profile)

                                "/api/job/**",       // Các API liên quan đến Job (ví dụ: xem danh sách jobs)
                                "/api/categories/**", // Các API liên quan đến Category (ví dụ: xem danh sách categories)

                                // THÊM CÁC ĐƯỜNG DẪN MỚI TẠI ĐÂY:
                                "/api/job-levels/**", // API cho Job Levels (ví dụ: xem danh sách job levels)
                                "/api/job-types/**",  // API cho Job Types (ví dụ: xem danh sách job types)
                                "/api/educations/**", // API cho Education (ví dụ: xem danh sách educations)
                                "/swagger-ui/**",     // Swagger UI documentation
                                "/v3/api-docs/**",    // OpenAPI 3 documentation
                                "/error"              // Đường dẫn mặc định của Spring Boot cho lỗi
                        ).permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3030",
                "http://localhost:8080",
                "http://localhost:3000"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
