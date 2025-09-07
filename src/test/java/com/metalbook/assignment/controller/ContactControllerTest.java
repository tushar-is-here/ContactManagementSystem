package com.metalbook.assignment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metalbook.assignment.dto.ContactRequest;
import com.metalbook.assignment.dto.ContactResponse;
import com.metalbook.assignment.entity.User;
import com.metalbook.assignment.service.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @Autowired
    private ObjectMapper objectMapper;

    private ContactRequest contactRequest;
    private ContactResponse contactResponse;
    private User mockUser;

    @BeforeEach
    void setUp() {
        contactRequest = new ContactRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "+1234567890"
        );

        contactResponse = new ContactResponse(
                1L,
                "John",
                "Doe",
                "john.doe@example.com",
                "+1234567890",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

//        mockUser = new User("testuser", "password", "test@example.com");
        mockUser = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com").build();
        mockUser.setId(1L);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createContact_ShouldReturnCreatedContact() throws Exception {
        // Set up authentication context
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities())
        );

        when(contactService.createContact(any(ContactRequest.class), eq(1L)))
                .thenReturn(contactResponse);

        mockMvc.perform(post("/api/v1/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contactRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getContactById_ShouldReturnContact() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities())
        );

        when(contactService.getContactById(1L, 1L)).thenReturn(contactResponse);

        mockMvc.perform(get("/api/v1/contacts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("John"));
    }

    @Test
    void createContact_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contactRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createContact_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        ContactRequest invalidRequest = new ContactRequest(
                "", // empty first name
                "",
                "invalid-email",
                "123" // invalid phone
        );

        mockMvc.perform(post("/api/v1/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}