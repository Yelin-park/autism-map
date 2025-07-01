package com.yaliny.autismmap.global.exception;

public class PostNotFoundException extends CustomException {

    public PostNotFoundException() {
        super(ErrorCode.POST_NOT_FOUND);
    }

}
