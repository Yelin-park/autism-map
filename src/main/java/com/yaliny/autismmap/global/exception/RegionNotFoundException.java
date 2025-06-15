package com.yaliny.autismmap.global.exception;

public class RegionNotFoundException extends CustomException {

    public RegionNotFoundException() {
        super(ErrorCode.REGION_NOT_FOUND);
    }

}
