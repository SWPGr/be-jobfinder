package com.example.jobfinder.config;

import com.example.jobfinder.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SecurityConfig {
    JwtRequestFilter jwtRequestFilter;
    OAuth2JwtSuccessHandler oAuth2JwtSuccessHandler;
    OAuth2JwtFailureHandler oAuth2JwtFailureHandler;

//    @Configuration
//    public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
//        @Override
//        protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
//            messages
//                    .simpDestMatchers("/app/**").hasRole("ADMIN") // Yêu cầu ROLE_ADMIN cho tin nhắn gửi đến /app/**
//                    .simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.DISCONNECT).permitAll() // Cho phép CONNECT và DISCONNECT
//                    .simpSubscribeDestMatchers("/topic/**").hasRole("ADMIN") // Yêu cầu ROLE_ADMIN để subscribe /topic/**
//                    .anyMessage().authenticated(); // Tất cả tin nhắn khác yêu cầu xác thực
//        }
//
//        @Override
//        protected boolean sameOriginDisabled() {
//            return true; // Tắt kiểm tra same-origin nếu cần (cho phát triển)
//        }
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthService authService) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/verify-email",
                                "/api/auth/forgot-password",
                                "/api/auth/resend-verification",
                                "/api/auth/verify",
                                "/api/auth/reset-password",
                                "/api/users/**",
                                "/api/debug/**",
                                "/api/profiles/**",
                                "/api/profiles/me",
                                "/api/apply/**",
                                "/api/save/",
                                "/api/saved-jobs/**",
                                "/api/social-types/**",
                                "/api/job-levels/**",
                                "/api/categories/**",
                                "/api/employer-reviews",
                                "/api/user-social-links",
                                "/api/notifications",
                                "/api/chat/**",
                                "/api/job/**",
                                "/api/job/list",
                                "/api/job-types",
                                "/api/auth/google",
                                "/api/categories/**",
                                "/api/statistics",
                                "/api/statistics/employer",
                                "/api/experiences/**",
                                "/api/chatbot",
                                "/topic/**",
                                "/error",
                                "/ws/**",
                                "/app/**",
                                "api/options/**",
                                "api/admin/**",
                                "/api/jobs/**",
                                "/api/categories/**",
                                "/api/analytics/employer/**",
                                "/api/employers/**",
                                "/api/job-seekers/**",
                                "/api/employers/**",
                                "/api/payos/**",
                                "/webhook",
                                "/api/subscription-plans/**",
                                "/api/payments/**",
                                "/api/green-test/**",
                                "api/debug/**",
                                "api/report/**",
                                "/api/admin"
                        ).permitAll()
                        .requestMatchers("/api/auth/change-password").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService()))
                        .successHandler(oAuth2JwtSuccessHandler)
                        .failureHandler(oAuth2JwtFailureHandler))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ).headers(headers -> headers
                        .addHeaderWriter((request, response) ->{
                            response.setHeader("Cross-Origin-Opener-Policy", "same-origin-allow-popups");
                            response.setHeader("Cross-Origin-Embedder-Policy", "require-corp");
                        })
                        
                )
                .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint((request, response, authException) -> {
                        if (request.getRequestURI().startsWith("/api/")) {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + authException.getMessage() + "\"}");
                        } else {
                            // Redirect to OAuth2 for non-API requests
                            response.sendRedirect("/oauth2/authorization/google");
                        }
                    })
            );
//        add header để tránh lỗi Cross-Origin-Opener-Policy policy would block the window.postMessage call.


          ;
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:3030",
                "http://localhost:8080",
                "https://fe-jobfinder.vercel.app/",
                "http://localhost:8081"
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
    public OidcUserService oidcUserService() {
        return new OidcUserService();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
