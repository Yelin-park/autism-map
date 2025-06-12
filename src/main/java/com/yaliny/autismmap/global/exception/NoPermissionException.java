package com.yaliny.autismmap.global.exception;

public class NoPermissionException extends CustomException  {
    public NoPermissionException() {
        super(ErrorCode.ACCESS_DENIED);
    }
}
