package com.amor.api.contorller.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ResponseDTO<T> {

    private String code = "200";
    private String message = "OK";
    private T data;

    public ResponseDTO(T data) {
        this.data = data;
    }

    public ResponseDTO(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
