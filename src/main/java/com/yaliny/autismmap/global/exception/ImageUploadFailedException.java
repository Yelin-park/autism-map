package com.yaliny.autismmap.global.exception;

public class ImageUploadFailedException extends CustomException {
    public ImageUploadFailedException() {
        super(ErrorCode.IMAGE_UPLOAD_FAIL);
    }
}
