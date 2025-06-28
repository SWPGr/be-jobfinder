package com.example.jobfinder.config;

import com.example.jobfinder.dto.auth.LoginResponse;
import com.example.jobfinder.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2JwtSuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger log = LoggerFactory.getLogger(OAuth2JwtSuccessHandler.class);

    private final ApplicationContext applicationContext;

    public OAuth2JwtSuccessHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Value("${app.frontend.url:http://localhost:3030}")
    private String frontEndUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
//        try {
//            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
//
//            AuthService authService = applicationContext.getBean(AuthService.class);
//            LoginResponse loginResponse = authService.handleGoogleLogin(oidcUser);
//
//            String redirectUrl = String.format("%s/oauth2/success?token=%s&role=%s",
//                    frontEndUrl,
//                    loginResponse.getToken(),
//                    loginResponse.getRole());
//
//            log.debug("Redirecting to: {} " + frontEndUrl + "/oauth2/success");
//            response.sendRedirect(redirectUrl);
//        } catch (Exception e) {
//            log.error("OAuth2 success handler error", e);
//            response.sendRedirect(frontEndUrl + "/login?error=oauth2_failed");
//        }
    }
}
