package com.metalbook.assignment.service;

import com.metalbook.assignment.entity.User;
import com.metalbook.assignment.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("customUserDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username)); // User entity implements UserDetails
    }

    /**
     * Load user by ID (useful for JWT token processing)
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {

        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with id: " + id));
    }
}
