package com.labndbnb.landbnb.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ExceptionAlert extends RuntimeException {
    private final Logger logger = LoggerFactory.getLogger(ExceptionAlert.class);
    public ExceptionAlert(String message) {
        super(message);
        logger.info(message);
    }
}