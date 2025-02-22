package com.hackathon.blockchain.controller;


import com.hackathon.blockchain.dto.GenericResponse;
import com.hackathon.blockchain.exception.ApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApplicationControllerAdvice {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<GenericResponse> handleApiException(ApiException apiException) {
        return new ResponseEntity<>(
                new GenericResponse(apiException.getMessage()),
                apiException.getStatus());
    }
}
