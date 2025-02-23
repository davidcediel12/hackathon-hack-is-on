package com.hackathon.blockchain.controller;


import com.hackathon.blockchain.dto.GenericResponse;
import com.hackathon.blockchain.exception.ApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApplicationControllerAdvice {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException apiException) {

        if (apiException.getResponseBody() == null) {

            if(apiException.getMessage() != null) {
                return new ResponseEntity<>(
                        new GenericResponse(apiException.getMessage()), apiException.getStatus());
            }
            return new ResponseEntity<>(apiException.getStatus());
        }

        return new ResponseEntity<>(apiException.getResponseBody(), apiException.getStatus());

    }
}
