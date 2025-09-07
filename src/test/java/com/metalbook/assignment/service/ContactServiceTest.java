package com.metalbook.assignment.service;

import com.metalbook.assignment.dto.ContactRequest;
import com.metalbook.assignment.dto.ContactResponse;
import com.metalbook.assignment.entity.Contact;
import com.metalbook.assignment.exception.DuplicateResourceException;
import com.metalbook.assignment.exception.ResourceNotFoundException;
import com.metalbook.assignment.repository.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactService contactService;

    private ContactRequest contactRequest;
    private Contact contact;
    private Long userId = 1L;

    @BeforeEach
    void setUp() {
        contactRequest = new ContactRequest(
                "Tushar",
                "Panchal",
                "tpanchal484@gmail.com",
                "+1234567890"
        );

//        contact = new Contact(
//                "Tushar",
//                "Panchal",
//                "tpanchal484@gmail.com",
//                "+1234567890",
//                userId
//        );

        contact = Contact.builder()
                .firstName("Tushar")
                .lastName("Panchal")
                .email("tpanchal484@gmail.com")
                        .build();
        contact.setId(1L);
    }

    @Test
    void createContact_ShouldReturnContactResponse() {
        // Given
        when(contactRepository.existsByEmailAndUserId(contactRequest.email(), userId))
                .thenReturn(false);
        when(contactRepository.save(any(Contact.class))).thenReturn(contact);

        // When
        ContactResponse result = contactService.createContact(contactRequest, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.firstName()).isEqualTo("Tushar");
        assertThat(result.lastName()).isEqualTo("Panchal");
        assertThat(result.email()).isEqualTo("tpanchal484@gmail.com");
        verify(contactRepository).save(any(Contact.class));
    }

    @Test
    void createContact_WhenEmailExists_ShouldThrowDuplicateResourceException() {
        // Given
        when(contactRepository.existsByEmailAndUserId(contactRequest.email(), userId))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> contactService.createContact(contactRequest, userId))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    void getContactById_WhenContactExists_ShouldReturnContact() {
        // Given
        when(contactRepository.findByIdAndUserId(1L, userId))
                .thenReturn(Optional.of(contact));

        // When
        ContactResponse result = contactService.getContactById(1L, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.firstName()).isEqualTo("Tushar");
    }

    @Test
    void getContactById_WhenContactNotFound_ShouldThrowResourceNotFoundException() {
        // Given
        when(contactRepository.findByIdAndUserId(1L, userId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> contactService.getContactById(1L, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void deleteContact_WhenContactExists_ShouldDeleteContact() {
        // Given
        when(contactRepository.findByIdAndUserId(1L, userId))
                .thenReturn(Optional.of(contact));

        // When
        contactService.deleteContact(1L, userId);

        // Then
        verify(contactRepository).delete(contact);
    }
}
