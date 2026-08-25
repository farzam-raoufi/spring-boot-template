package com.github.farzam_raoufi.template.repository;

import com.github.farzam_raoufi.template.model.RefreshToken;
import com.github.farzam_raoufi.template.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteAllByUser(User user);

    // used by the optional scheduled cleanup job
    void deleteAllByExpiryDateBefore(Instant time);
}
