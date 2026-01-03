package com.jsoncompare.service;

import com.jsoncompare.dto.auth.*;
import com.jsoncompare.model.User;
import com.jsoncompare.model.UserSession;
import com.jsoncompare.model.enums.UserSessionStatus;
import com.jsoncompare.model.enums.UserStatus;
import com.jsoncompare.repository.UserRepository;
import com.jsoncompare.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;

    // Token expiration: 24 hours
    private static final long TOKEN_EXPIRATION_HOURS = 24;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Check if email already exists
        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPasswordHash(hashPassword(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(List.of("USER"));
        user.setDeleted(false);

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());

        // Create session and return auth response
        return createSessionAndResponse(user, "Registration successful");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmailAndDeletedFalse(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        // Verify password
        if (!verifyPassword(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Check if user is active
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("Account is not active");
        }

        log.info("User logged in successfully: {}", user.getEmail());

        // Create session and return auth response
        return createSessionAndResponse(user, "Login successful");
    }

    @Transactional
    public void logout(String token) {
        UserSession session = userSessionRepository.findByAuthTokenAndDeletedFalse(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session"));

        session.logout();
        userSessionRepository.save(session);

        log.info("User logged out successfully");
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String token) {
        UserSession session = userSessionRepository.findByAuthTokenAndStatus(token, UserSessionStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired session"));

        if (session.isExpired()) {
            throw new IllegalArgumentException("Session expired");
        }

        User user = session.getUser();

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .status(user.getStatus())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(String token) {
        UserSession oldSession = userSessionRepository.findByAuthTokenAndDeletedFalse(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session"));

        User user = oldSession.getUser();

        // Invalidate old session
        oldSession.invalidate();
        userSessionRepository.save(oldSession);

        // Create new session
        return createSessionAndResponse(user, "Token refreshed successfully");
    }

    // ==================== Helper Methods ====================

    private AuthResponse createSessionAndResponse(User user, String message) {
        // Generate token
        String token = generateToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS);

        // Create session
        UserSession session = new UserSession();
        session.setUser(user);
        session.setAuthToken(token);
        session.setExpiresAt(expiresAt);
        session.setAdminLogin(user.hasRole("ADMIN"));
        session.setStatus(UserSessionStatus.ACTIVE);
        session.setDeleted(false);

        userSessionRepository.save(session);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .roles(user.getRoles())
                .token(token)
                .expiresAt(expiresAt)
                .message(message)
                .build();
    }

    private String generateToken() {
        // Simple UUID-based token for now
        // TODO: Replace with JWT when Spring Security is enabled
        return UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
    }

    private String hashPassword(String password) {
        // Simple hash for now - NOT SECURE FOR PRODUCTION
        // TODO: Replace with BCrypt when Spring Security is enabled
        return String.valueOf(password.hashCode());
    }

    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        // Simple verification for now - NOT SECURE FOR PRODUCTION
        // TODO: Replace with BCrypt when Spring Security is enabled
        return String.valueOf(rawPassword.hashCode()).equals(hashedPassword);
    }
}

