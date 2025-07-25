package com.yaliny.autismmap.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "계정이 존재하지 않습니다."),
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "장소가 존재하지 않습니다."),
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 지역입니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다.."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    DUPLICATE_SOCIAL_EMAIL(HttpStatus.CONFLICT, "해당 이메일은 다른 소셜 플랫폼으로 가입되어 있습니다."),
    S3_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "S3에 파일 업로드를 실패했습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "권한이 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

}
