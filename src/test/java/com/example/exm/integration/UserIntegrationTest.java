package com.example.exm.integration;

import com.example.exm.config.JpaAuditingConfig;
import com.example.exm.dto.request.CreateUserRequest;
import com.example.exm.dto.response.UserResponse;
import com.example.exm.entity.UserStatus;
import com.example.exm.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
//@Import(JpaAuditingConfig.class)
class UserIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        userRepository.deleteAll();
    }

    @Test
    void createUser_Success() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPhoneNumber("+1234567890");
        request.setAddress("123 Main St");
        request.setStatus(UserStatus.ACTIVE);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.firstName", is("John")))
                .andExpect(jsonPath("$.data.lastName", is("Doe")))
                .andExpect(jsonPath("$.data.email", is("john.doe@example.com")));
    }

    @Test
    void createUser_DuplicateEmail_ReturnsBadRequest() throws Exception {
        // Create first user
        CreateUserRequest request1 = new CreateUserRequest();
        request1.setFirstName("John");
        request1.setLastName("Doe");
        request1.setEmail("john.doe@example.com");
        request1.setStatus(UserStatus.ACTIVE);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Try to create second user with same email
        CreateUserRequest request2 = new CreateUserRequest();
        request2.setFirstName("Jane");
        request2.setLastName("Smith");
        request2.setEmail("john.doe@example.com");
        request2.setStatus(UserStatus.ACTIVE);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void getUserById_Success() throws Exception {
        // First create a user
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setStatus(UserStatus.ACTIVE);

        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract the ID from response
        UserResponse userResponse = objectMapper.readTree(response).get("data").traverse(objectMapper).readValueAs(UserResponse.class);
        Long userId = userResponse.getId();

        // Get the user by ID
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(userId.intValue())))
                .andExpect(jsonPath("$.data.firstName", is("John")))
                .andExpect(jsonPath("$.data.lastName", is("Doe")));
    }

    @Test
    void getUserById_NotFound_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void getAllUsers_Success() throws Exception {
        // Create multiple users
        for (int i = 1; i <= 3; i++) {
            CreateUserRequest request = new CreateUserRequest();
            request.setFirstName("User" + i);
            request.setLastName("Test");
            request.setEmail("user" + i + "@example.com");
            request.setStatus(UserStatus.ACTIVE);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Get all users
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalElements", is(3)))
                .andExpect(jsonPath("$.data.content.length()", is(3)));
    }

    @Test
    void createUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("invalid-email");
        request.setStatus(UserStatus.ACTIVE);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }
}