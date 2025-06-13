package com.yaliny.autismmap.global.exception;

public class PlaceNotFoundException extends CustomException {

    public PlaceNotFoundException() {
        super(ErrorCode.PLACE_NOT_FOUND);
    }

}
