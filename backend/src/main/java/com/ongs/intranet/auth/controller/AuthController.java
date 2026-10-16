package com.ongs.intranet.auth.controller;

import com.ongs.intranet.auth.dto.TokenResponse;
import com.ongs.intranet.auth.service.AuthService;
import com.ongs.intranet.common.response.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(HttpServletRequest request,
                                                               HttpServletResponse response) {
        String rawToken = extractRefreshTokenCookie(request);
        TokenResponse tokenResponse = authService.refresh(rawToken);
        return ResponseEntity.ok(ApiResponse.ok(tokenResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Long userId,
                                                     HttpServletResponse response) {
        authService.logout(userId);

        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok(ApiResponse.ok());
    }

    private String extractRefreshTokenCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new com.ongs.intranet.common.exception.BusinessException(
                    com.ongs.intranet.common.exception.ErrorCode.REFRESH_TOKEN_INVALID);
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> "refreshToken".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new com.ongs.intranet.common.exception.BusinessException(
                        com.ongs.intranet.common.exception.ErrorCode.REFRESH_TOKEN_INVALID));
    }
}
