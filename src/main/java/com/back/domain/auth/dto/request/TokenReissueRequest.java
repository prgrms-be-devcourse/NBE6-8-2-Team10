package com.back.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenReissueRequest(@NotBlank(message = "RefreshToken은 필수입니다.")
                                  String refreshToken) {
}
