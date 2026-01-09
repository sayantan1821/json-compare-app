package com.jsoncompare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoncompare.dto.auth.LoginRequest;
import com.jsoncompare.dto.auth.RegisterRequest;
import com.jsoncompare.model.User;
import com.jsoncompare.model.UserSession;
import com.jsoncompare.model.enums.UserSessionStatus;
import com.jsoncompare.model.enums.UserStatus;
import com.jsoncompare.repository.UserRepository;
import com.jsoncompare.repository.UserSessionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class AuthControllerIntegrationTest {

    private static boolean schemaInitialized = false;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private User testUser;
    private String testToken;

    @BeforeEach
    void setUp() {
        // Force schema creation on first test method run
        // This ensures tables are created before @Transactional takes effect
        if (!schemaInitialized) {
            EntityManager tempEm = entityManagerFactory.createEntityManager();
            jakarta.persistence.EntityTransaction tx = null;
            try {
                tx = tempEm.getTransaction();
                tx.begin();
                // Create a dummy user to trigger schema creation
                User dummyUser = new User();
                dummyUser.setEmail("schema_init_dummy@example.com");
                dummyUser.setPasswordHash("dummy");
                dummyUser.setStatus(UserStatus.ACTIVE);
                dummyUser.setRoles(List.of("USER"));
                dummyUser.setDeleted(false);
                tempEm.persist(dummyUser);
                tempEm.flush(); // Force flush to trigger schema creation
                tx.commit(); // Commit to ensure schema is created and persisted
                // Now delete the dummy entity
                tx = tempEm.getTransaction();
                tx.begin();
                tempEm.remove(dummyUser);
                tx.commit();
                schemaInitialized = true;
            } catch (Exception e) {
                // If this fails, schema will be created on first entity save
                if (tx != null && tx.isActive()) {
                    try {
                        tx.rollback();
                    } catch (Exception ex) {
                        // Ignore rollback errors
                    }
                }
            } finally {
                if (tempEm != null && tempEm.isOpen()) {
                    tempEm.close();
                }
            }
        }

        // Clean up existing data
        try {
            userSessionRepository.deleteAll();
            userRepository.deleteAll();
        } catch (Exception e) {
            // Ignore if tables don't exist yet - they should exist after schema initialization
        }

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
    }

    @Test
    void testRegister_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.message").value("Registration successful"));
    }

    @Test
    void testRegister_PasswordsDoNotMatch() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("different");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testRegister_InvalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email");
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testGetCurrentUser_Success() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testGetCurrentUser_NoToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogout_Success() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }
}

