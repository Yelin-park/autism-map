package com.yaliny.autismmap.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
    @Schema(description = "JWT 토큰 값")
    String token
) {
}
