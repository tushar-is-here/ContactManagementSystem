package com.metalbook.assignment;

import com.metalbook.assignment.dto.ContactRequest;
import com.metalbook.assignment.dto.LoginRequest;
import com.metalbook.assignment.dto.UserRegistrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
		import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AssignmentApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void contextLoads() {
		// Test that the application context loads successfully
	}

	@Test
	void fullWorkflow_RegisterLoginCreateContact() throws Exception {
		// 1. Register a user
		UserRegistrationRequest registrationRequest = new UserRegistrationRequest(
				"testuser", "password123", "test@example.com"
		);

		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(registrationRequest)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true));

		// 2. Login and get JWT token
		LoginRequest loginRequest = new LoginRequest("testuser", "password123");

		MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.token").exists())
				.andReturn();

		String responseBody = loginResult.getResponse().getContentAsString();
		// Extract token from response (simplified)
		String token = "Bearer " + extractTokenFromResponse(responseBody);

		// 3. Create a contact using the JWT token
		ContactRequest contactRequest = new ContactRequest(
				"John", "Doe", "john.doe@example.com", "+1234567890"
		);

		mockMvc.perform(post("/api/v1/contacts")
						.header("Authorization", token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(contactRequest)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.firstName").value("John"))
				.andExpect(jsonPath("$.data.email").value("john.doe@example.com"));

		// 4. Get all contacts
		mockMvc.perform(get("/api/v1/contacts")
						.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.content").isArray())
				.andExpect(jsonPath("$.data.content[0].firstName").value("John"));
	}

	private String extractTokenFromResponse(String responseBody) {
		// Simple extraction - in real tests, use proper JSON parsing
		try {
			var jsonNode = objectMapper.readTree(responseBody);
			return jsonNode.get("data").get("token").asText();
		} catch (Exception e) {
			throw new RuntimeException("Failed to extract token", e);
		}
	}
}