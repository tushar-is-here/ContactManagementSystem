package com.metalbook.assignment.controller;

import com.metalbook.assignment.dto.*;
import com.metalbook.assignment.entity.User;
import com.metalbook.assignment.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/api/v1/contacts")
@Tag(name = "Contacts", description = "Contact management endpoints")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class ContactController {

    private final ContactService contactService;

    @Autowired
    public ContactController(ContactService contactService) {
        this.contactService = contactService;
        log.info("ContactController initialized");
    }

    @Operation(summary = "Create a new contact", description = "Create a new contact for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Contact created successfully",
                    content = @Content(schema = @Schema(implementation = ContactResponse.class))),
            @ApiResponse(responseCode = "409", description = "Contact with email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponseCustom<ContactResponse>> createContact(
            @Valid @RequestBody ContactRequest request) {

        Long userId = getCurrentUserId();
        log.info("Creating new contact for userId: {} with email: {}", userId, request.email());
        log.debug("Contact creation details - firstName: {}, lastName: {}, phoneNumber: {}",
                request.firstName(), request.lastName(), request.phoneNumber());

        try {
            ContactResponse contact = contactService.createContact(request, userId);
            ApiResponseCustom<ContactResponse> response = ApiResponseCustom.success("Contact created successfully", contact);

            log.info("Contact created successfully with id: {} for userId: {}", contact.id(), userId);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Failed to create contact for userId: {} with email: {} - {}",
                    userId, request.email(), e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Get contact by ID", description = "Retrieve a specific contact by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contact retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ContactResponse.class))),
            @ApiResponse(responseCode = "404", description = "Contact not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseCustom<ContactResponse>> getContactById(
            @Parameter(description = "Contact ID") @PathVariable Long id) {

        Long userId = getCurrentUserId();
        log.info("Fetching contact with id: {} for userId: {}", id, userId);

        try {
            ContactResponse contact = contactService.getContactById(id, userId);
            ApiResponseCustom<ContactResponse> response = ApiResponseCustom.success(contact);

            log.debug("Contact retrieved successfully - id: {}, email: {}", contact.id(), contact.email());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to retrieve contact with id: {} for userId: {} - {}",
                    id, userId, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Get all contacts", description = "Retrieve all contacts for the authenticated user with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contacts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponseCustom<PagedResponse<ContactResponse>>> getAllContacts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection) {

        Long userId = getCurrentUserId();
        log.info("Fetching all contacts for userId: {} - page: {}, size: {}, sortBy: {}, sortDirection: {}",
                userId, page, size, sortBy, sortDirection);

        try {
            PagedResponse<ContactResponse> contacts = contactService.getAllContacts(userId, page, size, sortBy, sortDirection);
            ApiResponseCustom<PagedResponse<ContactResponse>> response = ApiResponseCustom.success(contacts);

            log.info("Retrieved {} contacts (page {}/{}) for userId: {}",
                    contacts.content().size(), contacts.page() + 1, contacts.totalPages(), userId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to retrieve contacts for userId: {} - {}", userId, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Update contact", description = "Update an existing contact")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contact updated successfully",
                    content = @Content(schema = @Schema(implementation = ContactResponse.class))),
            @ApiResponse(responseCode = "404", description = "Contact not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Contact with email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseCustom<ContactResponse>> updateContact(
            @Parameter(description = "Contact ID") @PathVariable Long id,
            @Valid @RequestBody ContactRequest request) {

        Long userId = getCurrentUserId();
        log.info("Updating contact with id: {} for userId: {}", id, userId);
        log.debug("Update request details - firstName: {}, lastName: {}, email: {}, phoneNumber: {}",
                request.firstName(), request.lastName(), request.email(), request.phoneNumber());

        try {
            ContactResponse contact = contactService.updateContact(id, request, userId);
            ApiResponseCustom<ContactResponse> response = ApiResponseCustom.success("Contact updated successfully", contact);

            log.info("Contact updated successfully - id: {}, email: {} for userId: {}",
                    contact.id(), contact.email(), userId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to update contact with id: {} for userId: {} - {}",
                    id, userId, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Delete contact", description = "Delete a contact by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contact deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Contact not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseCustom<String>> deleteContact(
            @Parameter(description = "Contact ID") @PathVariable Long id) {

        Long userId = getCurrentUserId();
        log.info("Deleting contact with id: {} for userId: {}", id, userId);

        try {
            contactService.deleteContact(id, userId);
            ApiResponseCustom<String> response = ApiResponseCustom.success("Contact deleted successfully", "OK");

            log.info("Contact deleted successfully - id: {} for userId: {}", id, userId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to delete contact with id: {} for userId: {} - {}",
                    id, userId, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Search contacts", description = "Search contacts by first name, last name, or email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponseCustom<PagedResponse<ContactResponse>>> searchContacts(
            @Parameter(description = "First name to search") @RequestParam(required = false) String firstName,
            @Parameter(description = "Last name to search") @RequestParam(required = false) String lastName,
            @Parameter(description = "Email to search") @RequestParam(required = false) String email,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection) {

        Long userId = getCurrentUserId();
        log.info("Searching contacts for userId: {} with criteria - firstName: {}, lastName: {}, email: {}",
                userId, firstName, lastName, email);
        log.debug("Search pagination - page: {}, size: {}, sortBy: {}, sortDirection: {}",
                page, size, sortBy, sortDirection);

        ContactSearchParams searchParams = new ContactSearchParams(
                firstName, lastName, email, page, size, sortBy, sortDirection
        );

        try {
            PagedResponse<ContactResponse> contacts = contactService.searchContacts(searchParams, userId);

            String searchMessage = buildSearchMessage(firstName, lastName, email);
            ApiResponseCustom<PagedResponse<ContactResponse>> response = ApiResponseCustom.success(searchMessage, contacts);

            log.info("Contact search completed for userId: {} - found {} results (page {}/{})",
                    userId, contacts.content().size(), contacts.page() + 1, contacts.totalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Contact search failed for userId: {} - {}", userId, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Get contact statistics", description = "Get statistics about user's contacts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/stats")
    public ResponseEntity<ApiResponseCustom<ContactService.ContactStats>> getContactStats() {

        Long userId = getCurrentUserId();
        log.info("Fetching contact statistics for userId: {}", userId);

        try {
            ContactService.ContactStats stats = contactService.getContactStats(userId);
            ApiResponseCustom<ContactService.ContactStats> response = ApiResponseCustom.success(stats);

            log.info("Contact statistics retrieved for userId: {} - total contacts: {}",
                    userId, stats.totalContacts());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch contact statistics for userId: {} - {}", userId, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Delete all contacts", description = "Delete all contacts for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All contacts deleted successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping
    public ResponseEntity<ApiResponseCustom<String>> deleteAllContacts() {

        Long userId = getCurrentUserId();
        log.warn("Deleting ALL contacts for userId: {} - This is a destructive operation!", userId);

        try {
            contactService.deleteAllContacts(userId);
            ApiResponseCustom<String> response = ApiResponseCustom.success("All contacts deleted successfully", "OK");

            log.warn("All contacts deleted successfully for userId: {}", userId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to delete all contacts for userId: {} - {}", userId, e.getMessage());
            throw e;
        }
    }

    // Helper methods

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }

    private String buildSearchMessage(String firstName, String lastName, String email) {
        StringBuilder message = new StringBuilder("Search completed");

        if (firstName != null && !firstName.isBlank()) {
            message.append(" (firstName: ").append(firstName).append(")");
        }
        if (lastName != null && !lastName.isBlank()) {
            message.append(" (lastName: ").append(lastName).append(")");
        }
        if (email != null && !email.isBlank()) {
            message.append(" (email: ").append(email).append(")");
        }

        return message.toString();
    }
}