package com.labndbnb.landbnb.exceptions;


import com.labndbnb.landbnb.dto.util_dto.ResponseDTO;
import com.labndbnb.landbnb.dto.util_dto.ValidationDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ResponseDTO<String>> noResourceFoundExceptionHandler(NoResourceFoundException ex){
        return ResponseEntity.status(404).body(new ResponseDTO<>(true, "El recurso solicitado no existe"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<String>> generalExceptionHandler(Exception e){
        return ResponseEntity.internalServerError().body(new ResponseDTO<>(true, "Error interno del servidor"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTO<List<ValidationDTO>>> validationExceptionHandler(MethodArgumentNotValidException ex) {
        List<ValidationDTO> errors = new ArrayList<>();
        BindingResult results = ex.getBindingResult();

        for (FieldError e: results.getFieldErrors()) {
            errors.add(new ValidationDTO(e.getField(), e.getDefaultMessage()));
        }

        return ResponseEntity.badRequest().body(new ResponseDTO<>(true, errors));
    }
}
