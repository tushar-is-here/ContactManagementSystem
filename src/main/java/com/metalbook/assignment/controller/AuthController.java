package com.metalbook.assignment.controller;

import com.metalbook.assignment.dto.*;
import com.metalbook.assignment.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
        log.info("AuthController initialized");
    }

    @Operation(summary = "Register a new user", description = "Create a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = UserInfo.class))),
            @ApiResponse(responseCode = "409", description = "Username or email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponseCustom<UserInfo>> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {

        log.info("Attempting to register new user with username: {}", request.username());
        log.debug("Registration request details - username: {}, email: {}",
                request.username(), request.email());

        try {
            UserInfo userInfo = authService.registerUser(request);
            ApiResponseCustom<UserInfo> response = ApiResponseCustom.success("User registered successfully", userInfo);

            log.info("User registration successful for username: {}, userId: {}",
                    request.username(), userInfo.id());

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("User registration failed for username: {} - {}", request.username(), e.getMessage());
            throw e; // Re-throw to be handled by global exception handler
        }
    }

    @Operation(summary = "Authenticate user", description = "Login with username and password to get JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponseCustom<AuthResponse>> authenticateUser(
            @Valid @RequestBody LoginRequest request) {

        log.info("Authentication attempt for username: {}", request.username());

        try {
            AuthResponse authResponse = authService.authenticateUser(request);
            ApiResponseCustom<AuthResponse> response = ApiResponseCustom.success("Login successful", authResponse);

            log.info("Authentication successful for username: {}, userId: {}",
                    request.username(), authResponse.user().id());
            log.debug("JWT token generated with expiration: {}", authResponse.expiresIn());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("Authentication failed for username: {} - {}", request.username(), e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Get current user info", description = "Get information about the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User info retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserInfo.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponseCustom<UserInfo>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("Fetching current user info for username: {}", username);

        try {
            UserInfo userInfo = authService.getCurrentUserInfo(username);
            ApiResponseCustom<UserInfo> response = ApiResponseCustom.success(userInfo);

            log.debug("Current user info retrieved successfully for userId: {}", userInfo.id());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch current user info for username: {} - {}", username, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Check username availability", description = "Check if a username is available for registration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Username availability checked",
                    content = @Content(schema = @Schema(implementation = Boolean.class)))
    })
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponseCustom<Boolean>> checkUsernameAvailability(
            @RequestParam String username) {

        log.info("Checking username availability for: {}", username);

        boolean available = authService.isUsernameAvailable(username);
        String message = available ? "Username is available" : "Username is already taken";
        ApiResponseCustom<Boolean> response = ApiResponseCustom.success(message, available);

        log.debug("Username availability check result for '{}': {}", username, available);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Check email availability", description = "Check if an email is available for registration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email availability checked",
                    content = @Content(schema = @Schema(implementation = Boolean.class)))
    })
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponseCustom<Boolean>> checkEmailAvailability(
            @RequestParam String email) {

        log.info("Checking email availability for: {}", email);

        boolean available = authService.isEmailAvailable(email);
        String message = available ? "Email is available" : "Email is already registered";
        ApiResponseCustom<Boolean> response = ApiResponseCustom.success(message, available);

        log.debug("Email availability check result for '{}': {}", email, available);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout", description = "Logout the current user (client-side token removal)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseCustom<String>> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "unknown";

        log.info("Logout request from user: {}", username);

        // Since we're using stateless JWT tokens, logout is handled client-side
        // by removing the token from storage. This endpoint is mainly for completeness
        // and could be used for token blacklisting in production systems.

        ApiResponseCustom<String> response = ApiResponseCustom.success(
                "Logout successful. Please remove the token from client storage.",
                "OK"
        );

        log.debug("Logout response sent to user: {}", username);

        return ResponseEntity.ok(response);
    }
}