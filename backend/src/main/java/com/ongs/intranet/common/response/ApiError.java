package com.ongs.intranet.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApiError {
    private final String code;
    private final String message;
}
