package com.jsoncompare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoncompare.dto.compare.CompareRequest;
import com.jsoncompare.model.ComparisonDiff;
import com.jsoncompare.model.User;
import com.jsoncompare.model.UserSession;
import com.jsoncompare.model.enums.ComparisonStatus;
import com.jsoncompare.model.enums.UserSessionStatus;
import com.jsoncompare.model.enums.UserStatus;
import com.jsoncompare.repository.ComparisonDiffRepository;
import com.jsoncompare.repository.UserRepository;
import com.jsoncompare.repository.UserSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CompareControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private ComparisonDiffRepository comparisonDiffRepository;

    private User testUser;
    private String testToken;
    private ComparisonDiff testComparison;

    @BeforeEach
    void setUp() {
        // Clean up
        comparisonDiffRepository.deleteAll();
        userSessionRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash(String.valueOf("password123".hashCode()));
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRoles(List.of("USER"));
        testUser.setDeleted(false);
        testUser = userRepository.save(testUser);

        // Create test session
        UserSession session = new UserSession();
        session.setUser(testUser);
        session.setAuthToken("test-token-123");
        session.setStatus(UserSessionStatus.ACTIVE);
        session.setExpiresAt(LocalDateTime.now().plusHours(24));
        session.setDeleted(false);
        userSessionRepository.save(session);
        testToken = "test-token-123";

        // Create test comparison
        testComparison = new ComparisonDiff();
        testComparison.setCreatedBy(testUser);
        testComparison.setInputStringA("{\"name\":\"John\"}");
        testComparison.setInputStringB("{\"name\":\"Jane\"}");
        testComparison.setResult("[{\"op\":\"replace\",\"path\":\"/name\",\"value\":\"Jane\"}]");
        testComparison.setIdentical(false);
        testComparison.setComparisonStatus(ComparisonStatus.COMPLETED);
        testComparison.setDeleted(false);
        testComparison = comparisonDiffRepository.save(testComparison);
    }

    @Test
    void testCompare_Success() throws Exception {
        CompareRequest request = new CompareRequest();
        request.setJsonA("{\"name\":\"John\",\"age\":30}");
        request.setJsonB("{\"name\":\"Jane\",\"age\":30}");
        request.setDescription("Test comparison");

        mockMvc.perform(post("/api/compare")
                        .header("Authorization", testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.identical").value(false))
                .andExpect(jsonPath("$.description").value("Test comparison"));
    }

    @Test
    void testCompare_IdenticalJson() throws Exception {
        CompareRequest request = new CompareRequest();
        request.setJsonA("{\"name\":\"John\"}");
        request.setJsonB("{\"name\":\"John\"}");

        mockMvc.perform(post("/api/compare")
                        .header("Authorization", testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.identical").value(true))
                .andExpect(jsonPath("$.diffCount").value(0));
    }

    @Test
    void testCompare_InvalidJson() throws Exception {
        CompareRequest request = new CompareRequest();
        request.setJsonA("invalid json");
        request.setJsonB("{\"name\":\"Jane\"}");

        mockMvc.perform(post("/api/compare")
                        .header("Authorization", testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCompare_NoAuth() throws Exception {
        CompareRequest request = new CompareRequest();
        request.setJsonA("{\"name\":\"John\"}");
        request.setJsonB("{\"name\":\"Jane\"}");

        mockMvc.perform(post("/api/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetComparison_Success() throws Exception {
        UUID comparisonId = testComparison.getId();

        mockMvc.perform(get("/api/compare/{id}", comparisonId)
                        .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comparisonId.toString()))
                .andExpect(jsonPath("$.jsonA").exists())
                .andExpect(jsonPath("$.jsonB").exists());
    }

    @Test
    void testGetComparison_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/compare/{id}", nonExistentId)
                        .header("Authorization", testToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetUserComparisons_Success() throws Exception {
        mockMvc.perform(get("/api/compare")
                        .header("Authorization", testToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testRecompare_Success() throws Exception {
        UUID comparisonId = testComparison.getId();

        mockMvc.perform(post("/api/compare/{id}/recompare", comparisonId)
                        .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comparisonId.toString()));
    }

    @Test
    void testUpdateDescription_Success() throws Exception {
        UUID comparisonId = testComparison.getId();
        String newDescription = "Updated description";

        mockMvc.perform(patch("/api/compare/{id}", comparisonId)
                        .header("Authorization", testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"" + newDescription + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(newDescription));
    }

    @Test
    void testDeleteComparison_Success() throws Exception {
        UUID comparisonId = testComparison.getId();

        mockMvc.perform(delete("/api/compare/{id}", comparisonId)
                        .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comparison deleted successfully"));
    }
}

