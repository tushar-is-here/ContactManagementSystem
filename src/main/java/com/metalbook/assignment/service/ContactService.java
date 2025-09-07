package com.metalbook.assignment.service;

import com.metalbook.assignment.dto.ContactRequest;
import com.metalbook.assignment.dto.ContactResponse;
import com.metalbook.assignment.dto.ContactSearchParams;
import com.metalbook.assignment.dto.PagedResponse;
import com.metalbook.assignment.entity.Contact;
import com.metalbook.assignment.exception.DuplicateResourceException;
import com.metalbook.assignment.exception.ResourceNotFoundException;
import com.metalbook.assignment.repository.ContactRepository;
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
public class ContactService {

    private final ContactRepository contactRepository;

    @Autowired
    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    /**
     * Create a new contact for the specified user
     */
    public ContactResponse createContact(ContactRequest request, Long userId) {
        // Check if email already exists for this user
        if (contactRepository.existsByEmailAndUserId(request.email(), userId)) {
            throw new DuplicateResourceException("Contact with email '" + request.email() + "' already exists");
        }

//        Contact contact = new Contact(
//                request.firstName(),
//                request.lastName(),
//                request.email(),
//                request.phoneNumber(),
//                userId
//        );
        Contact contact = Contact.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .userId(userId)
                .build();

        Contact savedContact = contactRepository.save(contact);
        return mapToContactResponse(savedContact);
    }

    /**
     * Get contact by ID for specific user
     */
    @Transactional(readOnly = true)
    public ContactResponse getContactById(Long id, Long userId) {
        Contact contact = contactRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
        return mapToContactResponse(contact);
    }

    /**
     * Get all contacts for a user with pagination
     */
    @Transactional(readOnly = true)
    public PagedResponse<ContactResponse> getAllContacts(Long userId, int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Contact> contactPage = contactRepository.findByUserId(userId, pageable);
        Page<ContactResponse> responsePage = contactPage.map(this::mapToContactResponse);

        return PagedResponse.of(responsePage);
    }

    /**
     * Update contact by ID for specific user
     */
    public ContactResponse updateContact(Long id, ContactRequest request, Long userId) {
        Contact existingContact = contactRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));

        // Check if email is being changed and if new email already exists
        if (!existingContact.getEmail().equals(request.email()) &&
                contactRepository.existsByEmailAndUserIdAndIdNot(request.email(), userId, id)) {
            throw new DuplicateResourceException("Contact with email '" + request.email() + "' already exists");
        }

        // Update contact fields using Java 21 pattern matching (if available) or traditional approach
        updateContactFields(existingContact, request);

        Contact updatedContact = contactRepository.save(existingContact);
        return mapToContactResponse(updatedContact);
    }

    /**
     * Delete contact by ID for specific user
     */
    public void deleteContact(Long id, Long userId) {
        Contact contact = contactRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));

        contactRepository.delete(contact);
    }

    /**
     * Search contacts with various criteria
     */
    @Transactional(readOnly = true)
    public PagedResponse<ContactResponse> searchContacts(ContactSearchParams searchParams, Long userId) {
        Sort sort = Sort.by(Sort.Direction.fromString(searchParams.sortDirection()), searchParams.sortBy());
        Pageable pageable = PageRequest.of(searchParams.page(), searchParams.size(), sort);

        Page<Contact> contactPage = switch (determineSearchType(searchParams)) {
            case FIRST_NAME_ONLY -> contactRepository.findByUserIdAndFirstNameContainingIgnoreCase(
                    userId, searchParams.firstName(), pageable);
            case LAST_NAME_ONLY -> contactRepository.findByUserIdAndLastNameContainingIgnoreCase(
                    userId, searchParams.lastName(), pageable);
            case EMAIL_ONLY -> contactRepository.findByUserIdAndEmailContainingIgnoreCase(
                    userId, searchParams.email(), pageable);
            case MULTIPLE_CRITERIA -> contactRepository.findByUserIdAndSearchCriteria(
                    userId, searchParams.firstName(), searchParams.lastName(), searchParams.email(), pageable);
            case ALL_CONTACTS -> contactRepository.findByUserId(userId, pageable);
        };

        Page<ContactResponse> responsePage = contactPage.map(this::mapToContactResponse);
        return PagedResponse.of(responsePage);
    }

    /**
     * Get contact statistics for a user
     */
    @Transactional(readOnly = true)
    public ContactStats getContactStats(Long userId) {
        long totalContacts = contactRepository.countByUserId(userId);
        return new ContactStats(totalContacts);
    }

    /**
     * Delete all contacts for a user
     */
    public void deleteAllContacts(Long userId) {
        contactRepository.deleteByUserId(userId);
    }

    // Private helper methods

    private void updateContactFields(Contact contact, ContactRequest request) {
        contact.setFirstName(request.firstName());
        contact.setLastName(request.lastName());
        contact.setEmail(request.email());
        contact.setPhoneNumber(request.phoneNumber());
    }

    private ContactResponse mapToContactResponse(Contact contact) {
        return new ContactResponse(
                contact.getId(),
                contact.getFirstName(),
                contact.getLastName(),
                contact.getEmail(),
                contact.getPhoneNumber(),
                contact.getCreatedAt(),
                contact.getUpdatedAt()
        );
    }

    private SearchType determineSearchType(ContactSearchParams params) {
        boolean hasFirstName = params.firstName() != null && !params.firstName().isBlank();
        boolean hasLastName = params.lastName() != null && !params.lastName().isBlank();
        boolean hasEmail = params.email() != null && !params.email().isBlank();

        return switch ((hasFirstName ? 1 : 0) + (hasLastName ? 2 : 0) + (hasEmail ? 4 : 0)) {
            case 1 -> SearchType.FIRST_NAME_ONLY;
            case 2 -> SearchType.LAST_NAME_ONLY;
            case 4 -> SearchType.EMAIL_ONLY;
            case 0 -> SearchType.ALL_CONTACTS;
            default -> SearchType.MULTIPLE_CRITERIA;
        };
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