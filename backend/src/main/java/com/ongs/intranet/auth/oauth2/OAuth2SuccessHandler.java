package com.ongs.intranet.auth.oauth2;

import com.ongs.intranet.auth.domain.RefreshToken;
import com.ongs.intranet.auth.jwt.JwtProvider;
import com.ongs.intranet.auth.repository.RefreshTokenRepository;
import com.ongs.intranet.user.domain.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    @Value("${app.frontend-redirect-uri}")
    private String frontendRedirectUri;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getRole().name());

        String rawRefreshToken = UUID.randomUUID().toString();
        String hashedRefreshToken = hashToken(rawRefreshToken);

        refreshTokenRepository.deleteAllByUserId(user.getId());
        refreshTokenRepository.save(RefreshToken.create(
                user.getId(),
                hashedRefreshToken,
                LocalDateTime.now().plusSeconds(refreshTokenExpiry)
        ));

        Cookie cookie = new Cookie("refreshToken", rawRefreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) refreshTokenExpiry);
        response.addCookie(cookie);

        String redirectUrl = frontendRedirectUri + "?token=" + accessToken;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
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
