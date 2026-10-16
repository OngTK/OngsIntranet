package com.ongs.intranet.user.dto;

import com.ongs.intranet.user.domain.User;

import java.time.LocalDateTime;

public record UserDetailResponse(
        Long id,
        String name,
        String email,
        String department,
        String position,
        String profileImageUrl,
        String phone,
        String role,
        boolean isActive,
        LocalDateTime createdAt
) {
    public static UserDetailResponse from(User user, String departmentName) {
        return new UserDetailResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                departmentName,
                user.getPosition(),
                user.getProfileImageUrl(),
                user.getPhone(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
