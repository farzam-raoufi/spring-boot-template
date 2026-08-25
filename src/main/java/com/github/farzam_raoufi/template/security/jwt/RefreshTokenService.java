package com.github.farzam_raoufi.template.security.jwt;

import com.github.farzam_raoufi.template.dto.auth.RefreshTokenDTO;
import com.github.farzam_raoufi.template.exception.BusinessException;
import com.github.farzam_raoufi.template.exception.ErrorMessage;
import com.github.farzam_raoufi.template.model.RefreshToken;
import com.github.farzam_raoufi.template.model.User;
import com.github.farzam_raoufi.template.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${app.jwt.refresh-rotate-ms}")
    private long refreshRotateMs;

    /**
     * Issues a new refresh token for the user and persists its hash.
     * Returns the RAW token — this is the only time the raw value exists; only the hash is stored.
     */
    @Transactional
    public String createToken(User user) {
        String rawToken = generateRawToken();

        RefreshToken entity = RefreshToken.builder()
                .tokenHash(hash(rawToken))
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .revoked(false)
                .build();

        refreshTokenRepository.save(entity);
        return rawToken;
    }

    /**
     * Validates the raw token, then rotates it: the old record is deleted and a new one issued.
     * Rotation means a refresh token can only ever be used until refreshRotateMs after issue.
     */
    @Transactional
    public RefreshTokenDTO rotate(String rawToken) {
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new BusinessException(ErrorMessage.REFRESH_TOKEN_INVALID));

        if (existing.isRevoked() || existing.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(existing);
            throw new BusinessException(ErrorMessage.REFRESH_TOKEN_EXPIRED);
        }

        if (existing.getExpiryDate().minusMillis(refreshExpirationMs - refreshRotateMs).isBefore(Instant.now())) {

            User user = existing.getUser();
            refreshTokenRepository.delete(existing);
            return new RefreshTokenDTO(user, createToken(user));

        } else {

            User user = existing.getUser();
            return new RefreshTokenDTO(user, rawToken);

        }
    }

    /**
     * Used on logout to invalidate a single refresh token.
     */
    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken))
                .ifPresent(refreshTokenRepository::delete);
    }

    /**
     * Optional: invalidate every refresh token for a user, e.g. "log out of all devices".
     */
    @Transactional
    public void revokeAllForUser(User user) {
        refreshTokenRepository.deleteAllByUser(user);
    }

    private String generateRawToken() {
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes());
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
