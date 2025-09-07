package com.metalbook.assignment.repository;

import com.metalbook.assignment.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    // Find contacts by user ID
    Page<Contact> findByUserId(Long userId, Pageable pageable);
    List<Contact> findByUserId(Long userId);

    // Find contact by ID and user ID (for security)
    Optional<Contact> findByIdAndUserId(Long id, Long userId);

    // Check if contact exists by email and user ID
    boolean existsByEmailAndUserId(String email, Long userId);

    // Check if contact exists by email and user ID, excluding specific ID (for updates)
    boolean existsByEmailAndUserIdAndIdNot(String email, Long userId, Long id);

    // Search by first name (case-insensitive)
    @Query("SELECT c FROM Contact c WHERE c.userId = :userId AND " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))")
    Page<Contact> findByUserIdAndFirstNameContainingIgnoreCase(
            @Param("userId") Long userId,
            @Param("firstName") String firstName,
            Pageable pageable);

    // Search by last name (case-insensitive)
    @Query("SELECT c FROM Contact c WHERE c.userId = :userId AND " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    Page<Contact> findByUserIdAndLastNameContainingIgnoreCase(
            @Param("userId") Long userId,
            @Param("lastName") String lastName,
            Pageable pageable);

    // Search by email (case-insensitive)
    @Query("SELECT c FROM Contact c WHERE c.userId = :userId AND " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    Page<Contact> findByUserIdAndEmailContainingIgnoreCase(
            @Param("userId") Long userId,
            @Param("email") String email,
            Pageable pageable);

    // Advanced search with multiple parameters
    @Query("SELECT c FROM Contact c WHERE c.userId = :userId AND " +
            "(:firstName IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
            "(:lastName IS NULL OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
            "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%')))")
    Page<Contact> findByUserIdAndSearchCriteria(
            @Param("userId") Long userId,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("email") String email,
            Pageable pageable);

    // Search contacts by full name (first + last name)
    @Query("SELECT c FROM Contact c WHERE c.userId = :userId AND " +
            "(LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :fullName, '%')) OR " +
            "LOWER(CONCAT(c.lastName, ' ', c.firstName)) LIKE LOWER(CONCAT('%', :fullName, '%')))")
    Page<Contact> findByUserIdAndFullNameContainingIgnoreCase(
            @Param("userId") Long userId,
            @Param("fullName") String fullName,
            Pageable pageable);

    // Count contacts by user ID
    long countByUserId(Long userId);

    // Delete all contacts for a user
    void deleteByUserId(Long userId);
}
