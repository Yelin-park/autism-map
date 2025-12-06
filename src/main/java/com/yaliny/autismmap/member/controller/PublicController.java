package com.yaliny.autismmap.member.controller;

import com.yaliny.autismmap.global.response.BaseResponse;
import com.yaliny.autismmap.member.dto.request.LoginRequest;
import com.yaliny.autismmap.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "외부에서 사용할 수 있는 기능")
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicController {

    private final MemberService memberService;

    @Operation(summary = "회원탈퇴")
    @DeleteMapping("/member/delete")
    public ResponseEntity<BaseResponse<String>> withdraw(
        @RequestBody LoginRequest request
    ) {
        memberService.memberDelete(request);
        return ResponseEntity.ok(BaseResponse.success("회원탈퇴 성공"));
    }
}
