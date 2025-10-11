package com.labndbnb.landbnb.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ExceptionAlert extends RuntimeException {
    public ExceptionAlert(String message) {
        super(message);
    }
}