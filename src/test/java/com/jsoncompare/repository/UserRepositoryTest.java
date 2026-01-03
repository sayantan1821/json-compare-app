package com.jsoncompare.repository;

import com.jsoncompare.model.User;
import com.jsoncompare.model.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedpassword");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRoles(List.of("USER"));
        testUser.setDeleted(false);
        testUser = userRepository.save(testUser);
    }

    @Test
    void testFindByEmail() {
        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testFindByEmail_NotFound() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        assertFalse(found.isPresent());
    }

    @Test
    void testFindByEmailAndDeletedFalse() {
        Optional<User> found = userRepository.findByEmailAndDeletedFalse("test@example.com");

        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testFindByEmailAndDeletedFalse_DeletedUser() {
        // Soft delete the user
        testUser.setDeleted(true);
        userRepository.save(testUser);

        Optional<User> found = userRepository.findByEmailAndDeletedFalse("test@example.com");

        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByEmail() {
        boolean exists = userRepository.existsByEmail("test@example.com");

        assertTrue(exists);
    }

    @Test
    void testExistsByEmail_NotFound() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertFalse(exists);
    }

    @Test
    void testExistsByEmailAndDeletedFalse() {
        boolean exists = userRepository.existsByEmailAndDeletedFalse("test@example.com");

        assertTrue(exists);
    }

    @Test
    void testExistsByEmailAndDeletedFalse_DeletedUser() {
        // Soft delete the user
        testUser.setDeleted(true);
        userRepository.save(testUser);

        boolean exists = userRepository.existsByEmailAndDeletedFalse("test@example.com");

        assertFalse(exists);
    }

    @Test
    void testSaveUser() {
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setPasswordHash("hashedpassword");
        newUser.setStatus(UserStatus.ACTIVE);
        newUser.setRoles(List.of("USER"));
        newUser.setDeleted(false);

        User saved = userRepository.save(newUser);

        assertNotNull(saved.getId());
        assertEquals("newuser@example.com", saved.getEmail());
    }
}

