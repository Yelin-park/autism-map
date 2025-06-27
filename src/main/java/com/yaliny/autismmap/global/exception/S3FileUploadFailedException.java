package com.yaliny.autismmap.global.exception;

public class S3FileUploadFailedException extends CustomException {
    public S3FileUploadFailedException() {
        super(ErrorCode.S3_UPLOAD_FAIL);
    }
}
