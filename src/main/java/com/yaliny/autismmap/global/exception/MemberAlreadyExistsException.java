package com.yaliny.autismmap.global.exception;

public class MemberAlreadyExistsException extends CustomException {
    public MemberAlreadyExistsException() {
        super(ErrorCode.MEMBER_ALREADY_EXISTS);
    }
}
