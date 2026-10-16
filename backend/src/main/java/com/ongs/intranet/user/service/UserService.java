package com.ongs.intranet.user.service;

import com.ongs.intranet.common.exception.BusinessException;
import com.ongs.intranet.common.exception.ErrorCode;
import com.ongs.intranet.user.domain.User;
import com.ongs.intranet.user.dto.UserDetailResponse;
import com.ongs.intranet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserDetailResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserDetailResponse.from(user, null);
    }
}
