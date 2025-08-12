package com.yaliny.autismmap.global.exception;

import com.yaliny.autismmap.global.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static com.yaliny.autismmap.global.exception.ErrorCode.VALIDATION_FAILED;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("ValidationException: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity
            .status(VALIDATION_FAILED.getStatus())
            .body(BaseResponse.error(VALIDATION_FAILED.getStatus().value(), VALIDATION_FAILED.getMessage(), errors));
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<BaseResponse<Void>> handleAuthServiceException(InternalAuthenticationServiceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(BaseResponse.error(HttpStatus.CONFLICT.value(), ex.getMessage()));
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
