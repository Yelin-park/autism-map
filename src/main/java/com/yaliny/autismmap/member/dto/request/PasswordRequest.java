package com.yaliny.autismmap.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordRequest(
        @Schema(title = "비밀번호", description = "비밀번호", example = "test1234")
        String password
) {
}
