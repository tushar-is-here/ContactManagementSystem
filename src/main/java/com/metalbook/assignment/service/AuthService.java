package com.metalbook.assignment.service;

import com.metalbook.assignment.dto.AuthResponse;
import com.metalbook.assignment.dto.LoginRequest;
import com.metalbook.assignment.dto.UserInfo;
import com.metalbook.assignment.dto.UserRegistrationRequest;
import com.metalbook.assignment.entity.User;
import com.metalbook.assignment.exception.DuplicateResourceException;
import com.metalbook.assignment.repository.UserRepository;
import com.metalbook.assignment.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
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
    }

    /**
     * Register a new user
     */
    public UserInfo registerUser(UserRegistrationRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username '" + request.username() + "' is already taken");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email '" + request.email() + "' is already registered");
        }

        // Create new user
        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.email()
        );

        User savedUser = userRepository.save(user);
        return mapToUserInfo(savedUser);
    }

    /**
     * Authenticate user and generate JWT token
     */
    public AuthResponse authenticateUser(LoginRequest request) {
        // Authenticate user credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(authentication);
        long expirationTime = jwtTokenProvider.getExpirationTime();

        // Get user information
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserInfo userInfo = mapToUserInfo(user);

        return new AuthResponse(token, expirationTime, userInfo);
    }

    /**
     * Get current user information
     */
    @Transactional(readOnly = true)
    public UserInfo getCurrentUserInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserInfo(user);
    }

    /**
     * Check if username is available
     */
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * Check if email is available
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    // Private helper methods

    private UserInfo mapToUserInfo(User user) {
        return new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
