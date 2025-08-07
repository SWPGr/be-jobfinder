package com.example.jobfinder.service;

import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class FilterService {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;

    public FilterService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void enableUserAndRelatedFilters() {
        Session session = entityManager.unwrap(Session.class);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        session.enableFilter("activeUserFilter").setParameter("isActive", true);
        session.enableFilter("activeRelatedUserFilter").setParameter("isActive", true);

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String userEmail = authentication.getName();
            User currentUser = userRepository.findByEmail(userEmail).orElse(null);

            if (currentUser != null && Objects.equals(currentUser.getRole().getName(), "ADMIN")) {
                session.disableFilter("activeUserFilter");
                session.disableFilter("activeRelatedUserFilter");
            }
        }
    }

    public void disableUserAndRelatedFilters() {
        Session session = entityManager.unwrap(Session.class);
        if (session.getEnabledFilter("activeUserFilter") != null) {
            session.disableFilter("activeUserFilter");
        }
        if (session.getEnabledFilter("activeRelatedUserFilter") != null) {
            session.disableFilter("activeRelatedUserFilter");
        }
    }
}