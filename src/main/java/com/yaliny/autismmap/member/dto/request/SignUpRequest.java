package com.yaliny.autismmap.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record SignUpRequest(
    @Schema(title = "이메일", description = "이메일", example = "test@test.com")
    String email,
    @Schema(title = "비밀번호", description = "비밀번호", example = "test1234")
    String password,
    @Schema(title = "닉네임", description = "닉네임", example = "테스터")
    String nickname) {

}
