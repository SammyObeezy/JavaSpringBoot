package org.example.escrow.integration;

import org.example.escrow.model.User;
import org.example.escrow.model.enums.UserRole;
import org.example.escrow.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AdminControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void getAllUsers_ShouldReturnList_WhenAdmin() throws Exception {
        // Arrange: Save a few users
        userRepository.save(User.builder().firstName("U1").lastName("L1").email("u1@test.com").phoneNumber("071").passwordHash("x").role(UserRole.ROLE_USER).build());
        userRepository.save(User.builder().firstName("U2").lastName("L2").email("u2@test.com").phoneNumber("072").passwordHash("x").role(UserRole.ROLE_USER).build());

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    void getAllUsers_ShouldReturnForbidden_WhenNotAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    void getAllUsers_ShouldReturnForbidden_WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // 403
    }
}