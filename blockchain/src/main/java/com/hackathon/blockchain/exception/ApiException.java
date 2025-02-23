package com.hackathon.blockchain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public class ApiException extends RuntimeException {

    public static final ApiException USER_NOT_FOUND = new ApiException("User not found", HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;
    private final Object responseBody;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.responseBody = null;
    }

    public ApiException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
        this.responseBody = null;
    }

    public ApiException(String message, Object responseBody, HttpStatus status) {
        super(message);
        this.responseBody = responseBody;
        this.status = status;
    }

}
