package com.amor.api.exception;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.BindException;

@ControllerAdvice
@Slf4j
public class GlobalErrorHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest webRequest) {
        log.error("서버오류가 발생하였습니다.", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder().code("500").message("서버오류가 발생하였습니다.").build());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, TypeMismatchException.class, BindException.class, HttpMessageConversionException.class})
    public ResponseEntity<Object> handleBadRequest(Exception ex, WebRequest webRequest) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder().code("400").message("요청값이 잘못되었습니다.").build());
    }

    @ExceptionHandler({NoResourceFoundException.class})
    public ResponseEntity<Object> handleNoResourceFoundException(Exception ex, WebRequest webRequest) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.builder().code("404").message("해당 요청 페이지가 없습니다.").build());
    }

    @ExceptionHandler({BizException.class})
    public ResponseEntity<Object> handleBizException(Exception ex, WebRequest webRequest) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder().code("400").message(ex.getMessage()).build());
    }


    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<Object> handleHttpRequestMethodNotSupportedException(Exception ex, WebRequest webRequest) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder().code("405").message("허용되지 않은 메소드입니다.").build());
    }

}
