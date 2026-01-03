package com.jsoncompare.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoncompare.dto.compare.CompareRequest;
import com.jsoncompare.dto.compare.CompareResponse;
import com.jsoncompare.model.ComparisonDiff;
import com.jsoncompare.model.User;
import com.jsoncompare.model.enums.ComparisonStatus;
import com.jsoncompare.model.enums.UserStatus;
import com.jsoncompare.repository.ComparisonDiffRepository;
import com.jsoncompare.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class CompareServiceTest {

    @Mock
    private ComparisonDiffRepository comparisonDiffRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CompareService compareService;

    private ObjectMapper objectMapper;

    private User testUser;
    private ComparisonDiff testComparison;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // Inject ObjectMapper using reflection since it's final
        try {
            java.lang.reflect.Field field = CompareService.class.getDeclaredField("objectMapper");
            field.setAccessible(true);
            field.set(compareService, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject ObjectMapper", e);
        }

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setDeleted(false);

        testComparison = new ComparisonDiff();
        testComparison.setId(UUID.randomUUID());
        testComparison.setCreatedBy(testUser);
        testComparison.setInputStringA("{\"name\":\"John\"}");
        testComparison.setInputStringB("{\"name\":\"Jane\"}");
        testComparison.setResult("[{\"op\":\"replace\",\"path\":\"/name\",\"value\":\"Jane\"}]");
        testComparison.setIdentical(false);
        testComparison.setComparisonStatus(ComparisonStatus.COMPLETED);
        testComparison.setDeleted(false);
    }

    @Test
    void testCompare_IdenticalJson() {
        // Arrange
        CompareRequest request = new CompareRequest();
        request.setJsonA("{\"name\":\"John\",\"age\":30}");
        request.setJsonB("{\"name\":\"John\",\"age\":30}");
        request.setDescription("Test comparison");

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(comparisonDiffRepository.save(any(ComparisonDiff.class))).thenAnswer(invocation -> {
            ComparisonDiff comp = invocation.getArgument(0);
            comp.setId(UUID.randomUUID());
            return comp;
        });

        // Act
        CompareResponse response = compareService.compare(request, testUser.getId());

        // Assert
        assertNotNull(response);
        assertTrue(response.isIdentical());
        assertEquals(0, response.getDiffCount());
        assertEquals("Test comparison", response.getDescription());
        verify(comparisonDiffRepository, times(1)).save(any(ComparisonDiff.class));
    }

    @Test
    void testCompare_DifferentJson() {
        // Arrange
        CompareRequest request = new CompareRequest();
        request.setJsonA("{\"name\":\"John\"}");
        request.setJsonB("{\"name\":\"Jane\"}");

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(comparisonDiffRepository.save(any(ComparisonDiff.class))).thenAnswer(invocation -> {
            ComparisonDiff comp = invocation.getArgument(0);
            comp.setId(UUID.randomUUID());
            return comp;
        });

        // Act
        CompareResponse response = compareService.compare(request, testUser.getId());

        // Assert
        assertNotNull(response);
        assertFalse(response.isIdentical());
        assertNotNull(response.getDifferences());
        verify(comparisonDiffRepository, times(1)).save(any(ComparisonDiff.class));
    }

    @Test
    void testCompare_InvalidJsonA() {
        // Arrange
        CompareRequest request = new CompareRequest();
        request.setJsonA("invalid json");
        request.setJsonB("{\"name\":\"Jane\"}");

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> compareService.compare(request, testUser.getId())
        );
        assertTrue(exception.getMessage().contains("JSON A"));
        verify(comparisonDiffRepository, never()).save(any(ComparisonDiff.class));
    }

    @Test
    void testCompare_InvalidJsonB() {
        // Arrange
        CompareRequest request = new CompareRequest();
        request.setJsonA("{\"name\":\"John\"}");
        request.setJsonB("invalid json");

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> compareService.compare(request, testUser.getId())
        );
        assertTrue(exception.getMessage().contains("JSON B"));
        verify(comparisonDiffRepository, never()).save(any(ComparisonDiff.class));
    }

    @Test
    void testCompare_UserNotFound() {
        // Arrange
        CompareRequest request = new CompareRequest();
        request.setJsonA("{\"name\":\"John\"}");
        request.setJsonB("{\"name\":\"Jane\"}");

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> compareService.compare(request, UUID.randomUUID())
        );
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testGetComparison_Success() {
        // Arrange
        UUID comparisonId = testComparison.getId();
        when(comparisonDiffRepository.findByIdAndCreatedByIdAndDeletedFalse(comparisonId, testUser.getId()))
                .thenReturn(Optional.of(testComparison));

        // Act
        CompareResponse response = compareService.getComparison(comparisonId, testUser.getId());

        // Assert
        assertNotNull(response);
        assertEquals(comparisonId, response.getId());
        assertEquals(testComparison.getInputStringA(), response.getJsonA());
        assertEquals(testComparison.getInputStringB(), response.getJsonB());
    }

    @Test
    void testGetComparison_NotFound() {
        // Arrange
        UUID comparisonId = UUID.randomUUID();
        when(comparisonDiffRepository.findByIdAndCreatedByIdAndDeletedFalse(comparisonId, testUser.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> compareService.getComparison(comparisonId, testUser.getId())
        );
        assertEquals("Comparison not found", exception.getMessage());
    }

    @Test
    void testRecompare_Success() {
        // Arrange
        UUID comparisonId = testComparison.getId();
        when(comparisonDiffRepository.findByIdAndCreatedByIdAndDeletedFalse(comparisonId, testUser.getId()))
                .thenReturn(Optional.of(testComparison));
        when(comparisonDiffRepository.save(any(ComparisonDiff.class))).thenReturn(testComparison);

        // Act
        CompareResponse response = compareService.recompare(comparisonId, testUser.getId());

        // Assert
        assertNotNull(response);
        assertNotNull(testComparison.getLastComparedAt());
        assertEquals(ComparisonStatus.COMPLETED, testComparison.getComparisonStatus());
        verify(comparisonDiffRepository, times(1)).save(testComparison);
    }

    @Test
    void testUpdateDescription_Success() {
        // Arrange
        UUID comparisonId = testComparison.getId();
        String newDescription = "Updated description";
        when(comparisonDiffRepository.findByIdAndCreatedByIdAndDeletedFalse(comparisonId, testUser.getId()))
                .thenReturn(Optional.of(testComparison));
        when(comparisonDiffRepository.save(any(ComparisonDiff.class))).thenReturn(testComparison);

        // Act
        CompareResponse response = compareService.updateDescription(comparisonId, testUser.getId(), newDescription);

        // Assert
        assertNotNull(response);
        assertEquals(newDescription, testComparison.getDescription());
        verify(comparisonDiffRepository, times(1)).save(testComparison);
    }

    @Test
    void testDeleteComparison_Success() {
        // Arrange
        UUID comparisonId = testComparison.getId();
        when(comparisonDiffRepository.findByIdAndCreatedByIdAndDeletedFalse(comparisonId, testUser.getId()))
                .thenReturn(Optional.of(testComparison));
        when(comparisonDiffRepository.save(any(ComparisonDiff.class))).thenReturn(testComparison);

        // Act
        compareService.deleteComparison(comparisonId, testUser.getId());

        // Assert
        assertTrue(testComparison.isDeleted());
        verify(comparisonDiffRepository, times(1)).save(testComparison);
    }

    @Test
    void testDeleteComparison_NotFound() {
        // Arrange
        UUID comparisonId = UUID.randomUUID();
        when(comparisonDiffRepository.findByIdAndCreatedByIdAndDeletedFalse(comparisonId, testUser.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> compareService.deleteComparison(comparisonId, testUser.getId())
        );
        assertEquals("Comparison not found", exception.getMessage());
        verify(comparisonDiffRepository, never()).save(any(ComparisonDiff.class));
    }
}

