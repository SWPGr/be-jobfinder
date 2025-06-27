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
    OAuth2JwtFailureHandler oAuth2JwtFailureHandler;;

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
                                "/api/job",
                                "/api/job/list",
                                "/api/job-types",
                                "/api/statistics",
                                "/api/statistics/employer",
                                "/api/chatbot",
                                "/topic/**",
                                "/error",
                                "/ws/**",
                                "/app/**"
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
                )

          ;
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Cho phép frontend của bạn. TRONG MÔI TRƯỜNG PRODUCTION, CHỈ ĐỊNH RÕ RÀNG DOMAIN CỦA BẠN.
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:3001", "http://localhost:3030")); // Thêm port React của bạn
        // Cho phép tất cả các phương thức HTTP
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Cho phép tất cả các header (bao gồm Authorization header cho JWT)
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // Cho phép gửi credentials (ví dụ: Authorization header)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Áp dụng cấu hình CORS này cho TẤT CẢ các đường dẫn API của bạn.
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
