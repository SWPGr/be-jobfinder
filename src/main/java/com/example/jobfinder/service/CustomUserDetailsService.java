package com.example.jobfinder.service;

import com.example.jobfinder.model.User;
import com.example.jobfinder.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Session session = entityManager.unwrap(Session.class);
        Filter activeUserFilter = null;

        try {
            activeUserFilter = session.getEnabledFilter("activeUserFilter");
            if (activeUserFilter != null) {
                session.disableFilter("activeUserFilter");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException(email));
            if (user == null) {
                throw new UsernameNotFoundException("User not found with email: " + email);
            }

            if (!user.getIsActive()) {
                throw new UsernameNotFoundException("User account is disabled: " + email);
            }

            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().getName().toLowerCase());
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    Collections.singletonList(authority)
            );
        } finally {
            if (activeUserFilter != null) {
                session.enableFilter("activeUserFilter").setParameter("isActive", true);
            }
        }


    }
}
