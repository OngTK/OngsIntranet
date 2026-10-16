package com.ongs.intranet.auth.repository;

import com.ongs.intranet.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String tokenHash);
    void deleteAllByUserId(Long userId);
}
