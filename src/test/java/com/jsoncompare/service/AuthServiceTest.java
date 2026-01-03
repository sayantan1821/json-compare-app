package com.jsoncompare.service;

import com.jsoncompare.dto.auth.AuthResponse;
import com.jsoncompare.dto.auth.LoginRequest;
import com.jsoncompare.dto.auth.RegisterRequest;
import com.jsoncompare.dto.auth.UserResponse;
import com.jsoncompare.model.User;
import com.jsoncompare.model.UserSession;
import com.jsoncompare.model.enums.UserSessionStatus;
import com.jsoncompare.model.enums.UserStatus;
import com.jsoncompare.repository.UserRepository;
import com.jsoncompare.repository.UserSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UserSession testSession;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash(String.valueOf("password123".hashCode()));
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRoles(List.of("USER"));
        testUser.setDeleted(false);

        testSession = new UserSession();
        testSession.setId(UUID.randomUUID());
        testSession.setUser(testUser);
        testSession.setAuthToken("test-token");
        testSession.setStatus(UserSessionStatus.ACTIVE);
        testSession.setExpiresAt(LocalDateTime.now().plusHours(24));
        testSession.setDeleted(false);
    }

    @Test
    void testRegister_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        when(userRepository.existsByEmailAndDeletedFalse(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(userSessionRepository.save(any(UserSession.class))).thenAnswer(invocation -> {
            UserSession session = invocation.getArgument(0);
            session.setId(UUID.randomUUID());
            return session;
        });

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("newuser@example.com", response.getEmail());
        assertNotNull(response.getToken());
        assertNotNull(response.getExpiresAt());
        assertEquals("Registration successful", response.getMessage());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userSessionRepository, times(1)).save(any(UserSession.class));
    }

    @Test
    void testRegister_PasswordsDoNotMatch() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("different");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );
        assertEquals("Passwords do not match", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        when(userRepository.existsByEmailAndDeletedFalse(anyString())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );
        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmailAndDeletedFalse(anyString())).thenReturn(Optional.of(testUser));
        when(userSessionRepository.save(any(UserSession.class))).thenAnswer(invocation -> {
            UserSession session = invocation.getArgument(0);
            session.setId(UUID.randomUUID());
            return session;
        });

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getEmail(), response.getEmail());
        assertNotNull(response.getToken());
        assertEquals("Login successful", response.getMessage());
        verify(userSessionRepository, times(1)).save(any(UserSession.class));
    }

    @Test
    void testLogin_InvalidEmail() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmailAndDeletedFalse(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testLogin_InvalidPassword() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        when(userRepository.findByEmailAndDeletedFalse(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testLogout_Success() {
        // Arrange
        String token = "test-token";
        when(userSessionRepository.findByAuthTokenAndDeletedFalse(token))
                .thenReturn(Optional.of(testSession));
        when(userSessionRepository.save(any(UserSession.class))).thenReturn(testSession);

        // Act
        authService.logout(token);

        // Assert
        assertEquals(UserSessionStatus.LOGGED_OUT, testSession.getStatus());
        verify(userSessionRepository, times(1)).save(testSession);
    }

    @Test
    void testGetCurrentUser_Success() {
        // Arrange
        String token = "test-token";
        when(userSessionRepository.findByAuthTokenAndStatus(token, UserSessionStatus.ACTIVE))
                .thenReturn(Optional.of(testSession));

        // Act
        UserResponse response = authService.getCurrentUser(token);

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getEmail(), response.getEmail());
    }

    @Test
    void testGetCurrentUser_ExpiredSession() {
        // Arrange
        String token = "expired-token";
        testSession.setExpiresAt(LocalDateTime.now().minusHours(1));
        when(userSessionRepository.findByAuthTokenAndStatus(token, UserSessionStatus.ACTIVE))
                .thenReturn(Optional.of(testSession));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.getCurrentUser(token)
        );
        assertEquals("Session expired", exception.getMessage());
    }

    @Test
    void testRefreshToken_Success() {
        // Arrange
        String token = "old-token";
        when(userSessionRepository.findByAuthTokenAndDeletedFalse(token))
                .thenReturn(Optional.of(testSession));
        when(userSessionRepository.save(any(UserSession.class))).thenAnswer(invocation -> {
            UserSession session = invocation.getArgument(0);
            session.setId(UUID.randomUUID());
            return session;
        });

        // Act
        AuthResponse response = authService.refreshToken(token);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals(UserSessionStatus.EXPIRED, testSession.getStatus());
        verify(userSessionRepository, times(2)).save(any(UserSession.class));
    }
}

