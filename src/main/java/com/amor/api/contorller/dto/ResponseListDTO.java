package com.amor.api.contorller.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ResponseListDTO<T> {

    private String code = "200";
    private String message = "OK";
    private List<T> data;

    public ResponseListDTO(List<T> data) {
        this.data = data;
    }

    public ResponseListDTO(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
