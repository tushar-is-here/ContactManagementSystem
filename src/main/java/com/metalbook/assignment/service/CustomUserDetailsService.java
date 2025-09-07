package com.metalbook.assignment.service;

import com.metalbook.assignment.entity.User;
import com.metalbook.assignment.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("customUserDetailsService")
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
        log.info("CustomUserDetailsService initialized successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);

        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.warn("User not found with username: {}", username);
                        return new UsernameNotFoundException("User not found with username: " + username);
                    });

            log.info("User loaded successfully - userId: {}, username: {}, role: {}",
                    user.getId(), user.getUsername(), user.getRole());
            log.debug("User authorities: {}", user.getAuthorities());

            return user; // User entity implements UserDetails
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to load user by username: {} - {}", username, e.getMessage());
            throw new UsernameNotFoundException("Failed to load user: " + username, e);
        }
    }

    /**
     * Load user by ID (useful for JWT token processing)
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        log.info("Loading user by id: {}", id);

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("User not found with id: {}", id);
                        return new UsernameNotFoundException("User not found with id: " + id);
                    });

            log.info("User loaded successfully by id - userId: {}, username: {}, role: {}",
                    user.getId(), user.getUsername(), user.getRole());
            log.debug("User authorities: {}", user.getAuthorities());

            return user;
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to load user by id: {} - {}", id, e.getMessage());
            throw new UsernameNotFoundException("Failed to load user with id: " + id, e);
        }
    }
}