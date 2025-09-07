package com.metalbook.assignment.service;

import com.metalbook.assignment.dto.AuthResponse;
import com.metalbook.assignment.dto.LoginRequest;
import com.metalbook.assignment.dto.UserInfo;
import com.metalbook.assignment.dto.UserRegistrationRequest;
import com.metalbook.assignment.entity.User;
import com.metalbook.assignment.exception.DuplicateResourceException;
import com.metalbook.assignment.repository.UserRepository;
import com.metalbook.assignment.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        log.info("AuthService initialized successfully");
    }

    /**
     * Register a new user
     */
    public UserInfo registerUser(UserRegistrationRequest request) {
        log.info("Starting user registration process for username: {}", request.username());

        // Check if username already exists
        log.debug("Checking username availability for: {}", request.username());
        if (userRepository.existsByUsername(request.username())) {
            log.warn("Registration failed - Username already exists: {}", request.username());
            throw new DuplicateResourceException("Username '" + request.username() + "' is already taken");
        }

        // Check if email already exists
        log.debug("Checking email availability for: {}", request.email());
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed - Email already exists: {}", request.email());
            throw new DuplicateResourceException("Email '" + request.email() + "' is already registered");
        }

        // Create new user
        log.debug("Creating new user entity for username: {}", request.username());
        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .build();

        try {
            User savedUser = userRepository.save(user);
            log.info("User registered successfully - userId: {}, username: {}",
                    savedUser.getId(), savedUser.getUsername());

            UserInfo userInfo = mapToUserInfo(savedUser);
            log.debug("UserInfo mapped successfully for userId: {}", savedUser.getId());

            return userInfo;
        } catch (Exception e) {
            log.error("Failed to save user during registration for username: {} - {}",
                    request.username(), e.getMessage());
            throw e;
        }
    }

    /**
     * Authenticate user and generate JWT token
     */
    public AuthResponse authenticateUser(LoginRequest request) {
        log.info("Starting authentication process for username: {}", request.username());

        try {
            // Authenticate user credentials
            log.debug("Attempting to authenticate user credentials for: {}", request.username());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );

            log.debug("User credentials authenticated successfully for: {}", request.username());

            // Generate JWT token
            log.debug("Generating JWT token for user: {}", request.username());
            String token = jwtTokenProvider.generateToken(authentication);
            long expirationTime = jwtTokenProvider.getExpirationTime();

            log.debug("JWT token generated successfully for user: {} with expiration: {}",
                    request.username(), expirationTime);

            // Get user information
            User user = userRepository.findByUsername(request.username())
                    .orElseThrow(() -> {
                        log.error("User not found during authentication process: {}", request.username());
                        return new RuntimeException("User not found");
                    });

            UserInfo userInfo = mapToUserInfo(user);

            log.info("Authentication completed successfully for userId: {}, username: {}",
                    user.getId(), user.getUsername());

            return new AuthResponse(token, expirationTime, userInfo);

        } catch (Exception e) {
            log.error("Authentication failed for username: {} - {}", request.username(), e.getMessage());
            throw e;
        }
    }

    /**
     * Get current user information
     */
    @Transactional(readOnly = true)
    public UserInfo getCurrentUserInfo(String username) {
        log.info("Fetching current user info for username: {}", username);

        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.error("User not found when fetching current user info: {}", username);
                        return new RuntimeException("User not found");
                    });

            UserInfo userInfo = mapToUserInfo(user);
            log.debug("Current user info retrieved successfully for userId: {}", user.getId());

            return userInfo;
        } catch (Exception e) {
            log.error("Failed to fetch current user info for username: {} - {}", username, e.getMessage());
            throw e;
        }
    }

    /**
     * Check if username is available
     */
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        log.debug("Checking username availability for: {}", username);

        boolean available = !userRepository.existsByUsername(username);

        log.debug("Username availability check result for '{}': {}", username, available);
        return available;
    }

    /**
     * Check if email is available
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        log.debug("Checking email availability for: {}", email);

        boolean available = !userRepository.existsByEmail(email);

        log.debug("Email availability check result for '{}': {}", email, available);
        return available;
    }

    // Private helper methods

    private UserInfo mapToUserInfo(User user) {
        log.debug("Mapping User entity to UserInfo for userId: {}", user.getId());

        try {
            return new UserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole().name(),
                    user.getCreatedAt()
            );
        } catch (Exception e) {
            log.error("Failed to map User entity to UserInfo for userId: {} - {}",
                    user.getId(), e.getMessage());
            throw e;
        }
    }
}