package com.yaliny.autismmap.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record SignUpRequest(
    @Schema(description = "이메일", example = "test@test.com")
    String email,
    @Schema(description = "비밀번호", example = "test1234")
    String password,
    @Schema(description = "닉네임", example = "테스터")
    String nickname) {

}
