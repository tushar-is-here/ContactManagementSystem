package com.metalbook.assignment.service;

import com.metalbook.assignment.dto.ContactRequest;
import com.metalbook.assignment.dto.ContactResponse;
import com.metalbook.assignment.dto.ContactSearchParams;
import com.metalbook.assignment.dto.PagedResponse;
import com.metalbook.assignment.entity.Contact;
import com.metalbook.assignment.exception.DuplicateResourceException;
import com.metalbook.assignment.exception.ResourceNotFoundException;
import com.metalbook.assignment.repository.ContactRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class ContactService {

    private final ContactRepository contactRepository;

    @Autowired
    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
        log.info("ContactService initialized successfully");
    }

    /**
     * Create a new contact for the specified user
     */
    public ContactResponse createContact(ContactRequest request, Long userId) {
        log.info("Creating new contact for userId: {} with email: {}", userId, request.email());
        log.debug("Contact creation details - firstName: {}, lastName: {}, phoneNumber: {}",
                request.firstName(), request.lastName(), request.phoneNumber());

        // Check if email already exists for this user
        log.debug("Checking if contact with email already exists for userId: {}", userId);
        if (contactRepository.existsByEmailAndUserId(request.email(), userId)) {
            log.warn("Contact creation failed - Email already exists: {} for userId: {}",
                    request.email(), userId);
            throw new DuplicateResourceException("Contact with email '" + request.email() + "' already exists");
        }

        try {
            Contact contact = Contact.builder()
                    .firstName(request.firstName())
                    .lastName(request.lastName())
                    .email(request.email())
                    .phoneNumber(request.phoneNumber())
                    .userId(userId)
                    .build();

            Contact savedContact = contactRepository.save(contact);
            log.info("Contact created successfully - contactId: {}, email: {} for userId: {}",
                    savedContact.getId(), savedContact.getEmail(), userId);

            ContactResponse response = mapToContactResponse(savedContact);
            log.debug("Contact entity mapped to ContactResponse successfully");

            return response;
        } catch (Exception e) {
            log.error("Failed to create contact for userId: {} with email: {} - {}",
                    userId, request.email(), e.getMessage());
            throw e;
        }
    }

    /**
     * Get contact by ID for specific user
     */
    @Transactional(readOnly = true)
    public ContactResponse getContactById(Long id, Long userId) {
        log.info("Fetching contact by id: {} for userId: {}", id, userId);

        try {
            Contact contact = contactRepository.findByIdAndUserId(id, userId)
                    .orElseThrow(() -> {
                        log.warn("Contact not found with id: {} for userId: {}", id, userId);
                        return new ResourceNotFoundException("Contact not found with id: " + id);
                    });

            log.debug("Contact found - id: {}, email: {}", contact.getId(), contact.getEmail());
            return mapToContactResponse(contact);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch contact by id: {} for userId: {} - {}",
                    id, userId, e.getMessage());
            throw e;
        }
    }

    /**
     * Get all contacts for a user with pagination
     */
    @Transactional(readOnly = true)
    public PagedResponse<ContactResponse> getAllContacts(Long userId, int page, int size, String sortBy, String sortDirection) {
        log.info("Fetching all contacts for userId: {} with pagination - page: {}, size: {}, sortBy: {}, sortDirection: {}",
                userId, page, size, sortBy, sortDirection);

        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Contact> contactPage = contactRepository.findByUserId(userId, pageable);
            Page<ContactResponse> responsePage = contactPage.map(this::mapToContactResponse);

            PagedResponse<ContactResponse> result = PagedResponse.of(responsePage);

            log.info("Retrieved {} contacts (page {}/{}) for userId: {} - total elements: {}",
                    result.content().size(), result.page() + 1, result.totalPages(),
                    userId, result.totalElements());

            return result;
        } catch (Exception e) {
            log.error("Failed to fetch all contacts for userId: {} - {}", userId, e.getMessage());
            throw e;
        }
    }

    /**
     * Update contact by ID for specific user
     */
    public ContactResponse updateContact(Long id, ContactRequest request, Long userId) {
        log.info("Updating contact with id: {} for userId: {}", id, userId);
        log.debug("Update request details - firstName: {}, lastName: {}, email: {}, phoneNumber: {}",
                request.firstName(), request.lastName(), request.email(), request.phoneNumber());

        try {
            Contact existingContact = contactRepository.findByIdAndUserId(id, userId)
                    .orElseThrow(() -> {
                        log.warn("Contact not found for update - id: {} for userId: {}", id, userId);
                        return new ResourceNotFoundException("Contact not found with id: " + id);
                    });

            String oldEmail = existingContact.getEmail();
            log.debug("Found existing contact - id: {}, current email: {}", id, oldEmail);

            // Check if email is being changed and if new email already exists
            if (!existingContact.getEmail().equals(request.email())) {
                log.debug("Email is being changed from '{}' to '{}' for contactId: {}",
                        oldEmail, request.email(), id);

                if (contactRepository.existsByEmailAndUserIdAndIdNot(request.email(), userId, id)) {
                    log.warn("Contact update failed - New email already exists: {} for userId: {} (contactId: {})",
                            request.email(), userId, id);
                    throw new DuplicateResourceException("Contact with email '" + request.email() + "' already exists");
                }
            }

            // Update contact fields
            updateContactFields(existingContact, request);

            Contact updatedContact = contactRepository.save(existingContact);
            log.info("Contact updated successfully - id: {}, email: {} for userId: {}",
                    updatedContact.getId(), updatedContact.getEmail(), userId);

            return mapToContactResponse(updatedContact);
        } catch (ResourceNotFoundException | DuplicateResourceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update contact with id: {} for userId: {} - {}",
                    id, userId, e.getMessage());
            throw e;
        }
    }

    /**
     * Delete contact by ID for specific user
     */
    public void deleteContact(Long id, Long userId) {
        log.info("Deleting contact with id: {} for userId: {}", id, userId);

        try {
            Contact contact = contactRepository.findByIdAndUserId(id, userId)
                    .orElseThrow(() -> {
                        log.warn("Contact not found for deletion - id: {} for userId: {}", id, userId);
                        return new ResourceNotFoundException("Contact not found with id: " + id);
                    });

            String email = contact.getEmail();
            contactRepository.delete(contact);

            log.info("Contact deleted successfully - id: {}, email: {} for userId: {}",
                    id, email, userId);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete contact with id: {} for userId: {} - {}",
                    id, userId, e.getMessage());
            throw e;
        }
    }

    /**
     * Search contacts with various criteria
     */
    @Transactional(readOnly = true)
    public PagedResponse<ContactResponse> searchContacts(ContactSearchParams searchParams, Long userId) {
        log.info("Searching contacts for userId: {} with criteria - firstName: {}, lastName: {}, email: {}",
                userId, searchParams.firstName(), searchParams.lastName(), searchParams.email());
        log.debug("Search pagination - page: {}, size: {}, sortBy: {}, sortDirection: {}",
                searchParams.page(), searchParams.size(), searchParams.sortBy(), searchParams.sortDirection());

        try {
            Sort sort = Sort.by(Sort.Direction.fromString(searchParams.sortDirection()), searchParams.sortBy());
            Pageable pageable = PageRequest.of(searchParams.page(), searchParams.size(), sort);

            SearchType searchType = determineSearchType(searchParams);
            log.debug("Determined search type: {}", searchType);

            Page<Contact> contactPage = switch (searchType) {
                case FIRST_NAME_ONLY -> {
                    log.debug("Executing firstName search for: {}", searchParams.firstName());
                    yield contactRepository.findByUserIdAndFirstNameContainingIgnoreCase(
                            userId, searchParams.firstName(), pageable);
                }
                case LAST_NAME_ONLY -> {
                    log.debug("Executing lastName search for: {}", searchParams.lastName());
                    yield contactRepository.findByUserIdAndLastNameContainingIgnoreCase(
                            userId, searchParams.lastName(), pageable);
                }
                case EMAIL_ONLY -> {
                    log.debug("Executing email search for: {}", searchParams.email());
                    yield contactRepository.findByUserIdAndEmailContainingIgnoreCase(
                            userId, searchParams.email(), pageable);
                }
                case MULTIPLE_CRITERIA -> {
                    log.debug("Executing multi-criteria search");
                    yield contactRepository.findByUserIdAndSearchCriteria(
                            userId, searchParams.firstName(), searchParams.lastName(), searchParams.email(), pageable);
                }
                case ALL_CONTACTS -> {
                    log.debug("Executing fetch all contacts (no search criteria)");
                    yield contactRepository.findByUserId(userId, pageable);
                }
            };

            Page<ContactResponse> responsePage = contactPage.map(this::mapToContactResponse);
            PagedResponse<ContactResponse> result = PagedResponse.of(responsePage);

            log.info("Contact search completed for userId: {} - found {} results (page {}/{}) - total elements: {}",
                    userId, result.content().size(), result.page() + 1,
                    result.totalPages(), result.totalElements());

            return result;
        } catch (Exception e) {
            log.error("Contact search failed for userId: {} - {}", userId, e.getMessage());
            throw e;
        }
    }

    /**
     * Get contact statistics for a user
     */
    @Transactional(readOnly = true)
    public ContactStats getContactStats(Long userId) {
        log.info("Fetching contact statistics for userId: {}", userId);

        try {
            long totalContacts = contactRepository.countByUserId(userId);

            log.info("Contact statistics retrieved for userId: {} - total contacts: {}",
                    userId, totalContacts);

            return new ContactStats(totalContacts);
        } catch (Exception e) {
            log.error("Failed to fetch contact statistics for userId: {} - {}", userId, e.getMessage());
            throw e;
        }
    }

    /**
     * Delete all contacts for a user
     */
    public void deleteAllContacts(Long userId) {
        log.warn("Deleting ALL contacts for userId: {} - This is a destructive operation!", userId);

        try {
            long countBefore = contactRepository.countByUserId(userId);
            log.info("Found {} contacts to delete for userId: {}", countBefore, userId);

            contactRepository.deleteByUserId(userId);

            log.warn("All contacts deleted successfully for userId: {} - {} contacts were removed",
                    userId, countBefore);
        } catch (Exception e) {
            log.error("Failed to delete all contacts for userId: {} - {}", userId, e.getMessage());
            throw e;
        }
    }

    // Private helper methods

    private void updateContactFields(Contact contact, ContactRequest request) {
        log.debug("Updating contact fields for contactId: {}", contact.getId());

        contact.setFirstName(request.firstName());
        contact.setLastName(request.lastName());
        contact.setEmail(request.email());
        contact.setPhoneNumber(request.phoneNumber());

        log.debug("Contact fields updated successfully for contactId: {}", contact.getId());
    }

    private ContactResponse mapToContactResponse(Contact contact) {
        log.trace("Mapping Contact entity to ContactResponse for contactId: {}", contact.getId());

        try {
            return new ContactResponse(
                    contact.getId(),
                    contact.getFirstName(),
                    contact.getLastName(),
                    contact.getEmail(),
                    contact.getPhoneNumber(),
                    contact.getCreatedAt(),
                    contact.getUpdatedAt()
            );
        } catch (Exception e) {
            log.error("Failed to map Contact entity to ContactResponse for contactId: {} - {}",
                    contact.getId(), e.getMessage());
            throw e;
        }
    }

    private SearchType determineSearchType(ContactSearchParams params) {
        boolean hasFirstName = params.firstName() != null && !params.firstName().isBlank();
        boolean hasLastName = params.lastName() != null && !params.lastName().isBlank();
        boolean hasEmail = params.email() != null && !params.email().isBlank();

        int criteriaCount = (hasFirstName ? 1 : 0) + (hasLastName ? 2 : 0) + (hasEmail ? 4 : 0);

        SearchType searchType = switch (criteriaCount) {
            case 1 -> SearchType.FIRST_NAME_ONLY;
            case 2 -> SearchType.LAST_NAME_ONLY;
            case 4 -> SearchType.EMAIL_ONLY;
            case 0 -> SearchType.ALL_CONTACTS;
            default -> SearchType.MULTIPLE_CRITERIA;
        };

        log.debug("Search criteria analysis - firstName: {}, lastName: {}, email: {}, criteriaCount: {}, searchType: {}",
                hasFirstName, hasLastName, hasEmail, criteriaCount, searchType);

        return searchType;
    }

    private enum SearchType {
        ALL_CONTACTS,
        FIRST_NAME_ONLY,
        LAST_NAME_ONLY,
        EMAIL_ONLY,
        MULTIPLE_CRITERIA
    }

    /**
     * Contact Statistics record for returning stats information
     */
    public record ContactStats(long totalContacts) {}
}