package com.yaliny.autismmap.global.exception;

import com.yaliny.autismmap.global.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<BaseResponse<Void>> handleCustomException(CustomException ex) {
        log.warn("CustomException - {}: {}", ex.getErrorCode().name(), ex.getMessage());
        return ResponseEntity
            .status(ex.getErrorCode().getStatus())
            .body(BaseResponse.error(ex.getErrorCode().getStatus().value(), ex.getMessage()));
    }

    // 그 외 모든 예외 처리 (catch-all)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleException(Exception ex) {
        log.error("Unhandled Exception ", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(BaseResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 오류가 발생했습니다."));
    }
}
