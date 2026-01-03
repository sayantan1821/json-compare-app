package com.jsoncompare.repository;

import com.jsoncompare.model.UserSession;
import com.jsoncompare.model.enums.UserSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByAuthToken(String authToken);

    Optional<UserSession> findByAuthTokenAndDeletedFalse(String authToken);

    Optional<UserSession> findByAuthTokenAndStatus(String authToken, UserSessionStatus status);

    List<UserSession> findByUserIdAndDeletedFalse(UUID userId);

    List<UserSession> findByUserIdAndStatus(UUID userId, UserSessionStatus status);
}

