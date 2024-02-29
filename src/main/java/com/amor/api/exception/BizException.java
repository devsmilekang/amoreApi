package com.amor.api.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException{


    private String code = "-1";
    public BizException(String message){
        super(message);
    }

    public BizException(String code, String message) {
        super(message);
        this.code = code;
    }
}
