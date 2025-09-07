package com.metalbook.assignment.dto;

/**
 * Search Parameters DTO
 */
public record ContactSearchParams(
        String firstName,
        String lastName,
        String email,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
    public ContactSearchParams {
        // Validation and default values
        page = Math.max(0, page);
        size = Math.min(Math.max(1, size), 100); // Max 100 per page
        sortBy = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        sortDirection = (sortDirection == null ||
                (!sortDirection.equalsIgnoreCase("desc") &&
                        !sortDirection.equalsIgnoreCase("asc"))) ? "asc" : sortDirection;
    }

    // Convenience constructor for search without pagination
    public ContactSearchParams(String firstName, String lastName, String email) {
        this(firstName, lastName, email, 0, 20, "id", "asc");
    }
}
