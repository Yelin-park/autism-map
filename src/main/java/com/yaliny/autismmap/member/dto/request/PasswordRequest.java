package com.yaliny.autismmap.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordRequest(
    @Schema(title = "기존 비밀번호", description = "기존 비밀번호", example = "old1234")
    String oldPassword,
    @Schema(title = "변경할 비밀번호", description = "변경할 비밀번호", example = "test1234")
    String password
) {
}
