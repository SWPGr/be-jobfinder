package com.example.jobfinder.config;

import com.example.jobfinder.dto.auth.LoginResponse;
import com.example.jobfinder.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.messaging.Message;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
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
@EnableWebSocketSecurity
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
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/users",
                                "/api/debug/**",
                                "/api/profiles/**",
                                "/api/profiles/me",
                                "/api/apply/**",
                                "/api/save/",
                                "/api/saved-jobs",
                                "/api/social-types",
                                "/api/job-levels",
                                "/api/employer-reviews",
                                "/api/user-social-links",
                                "/api/notifications",
                                "/api/chat/**",
                                "/api/job/**",
                                "/api/job/list",
                                "/api/job-types",
                                "/api/statistics",
                                "/api/statistics/employer",
                                "/api/chatbot",
                                "/topic/**",
                                "/error",
                                "/ws/**",
                                "/app/**",
                                "api/options/**"
                        ).permitAll()
                        .requestMatchers("/api/chat/**").authenticated()
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
                );
//        add header để tránh lỗi Cross-Origin-Opener-Policy policy would block the window.postMessage call.


          ;
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3030",
                "http://localhost:8080"
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
