package com.ongs.intranet.auth.service;

import com.ongs.intranet.auth.domain.RefreshToken;
import com.ongs.intranet.auth.dto.TokenResponse;
import com.ongs.intranet.auth.jwt.JwtProvider;
import com.ongs.intranet.auth.repository.RefreshTokenRepository;
import com.ongs.intranet.common.exception.BusinessException;
import com.ongs.intranet.common.exception.ErrorCode;
import com.ongs.intranet.user.domain.User;
import com.ongs.intranet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Transactional
    public TokenResponse refresh(String rawRefreshToken) {
        String hashed = hashToken(rawRefreshToken);

        RefreshToken refreshToken = refreshTokenRepository.findByToken(hashed)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getRole().name());

        String newRaw = UUID.randomUUID().toString();
        String newHashed = hashToken(newRaw);
        refreshTokenRepository.delete(refreshToken);
        refreshTokenRepository.save(RefreshToken.create(
                user.getId(), newHashed,
                LocalDateTime.now().plusSeconds(refreshTokenExpiry)
        ));

        return new TokenResponse(newAccessToken, accessTokenExpiry);
    }

    @Transactional
    public String logout(Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
        return "refreshToken";
    }

    private String hashToken(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
