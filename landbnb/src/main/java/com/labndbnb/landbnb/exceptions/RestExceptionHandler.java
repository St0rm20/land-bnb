package com.labndbnb.landbnb.exceptions;

import com.labndbnb.landbnb.dto.util_dto.ResponseDTO;
import com.labndbnb.landbnb.dto.util_dto.ValidationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(ExceptionAlert.class)
    public ResponseEntity<ResponseDTO<String>> handleAlert(ExceptionAlert ex) {
        return ResponseEntity.badRequest().body(new ResponseDTO<>(true, ex.getMessage()));
    }

    @ExceptionHandler(EmailAlreadyInUse.class)
    public ResponseEntity<ResponseDTO<String>> handleEmailInUse(EmailAlreadyInUse ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseDTO<>(true, ex.getMessage()));
    }

    @ExceptionHandler({TokenIncorrect.class, MissingToken.class})
    public ResponseEntity<ResponseDTO<String>> handleToken(RuntimeException ex) {
        return ResponseEntity.badRequest().body(new ResponseDTO<>(true, ex.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ResponseDTO<String>> handleBadInput(Exception ex) {
        return ResponseEntity.badRequest().body(new ResponseDTO<>(true, "Solicitud inválida"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseDTO<String>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ResponseDTO<>(true, "No tienes permisos para acceder a este recurso"));
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ResponseDTO<String>> handleBadCredentials(
            org.springframework.security.authentication.BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDTO<>(true, "Credenciales inválidas"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseDTO<String>> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseDTO<>(true, "Debes iniciar sesión para acceder a este recurso"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ResponseDTO<String>> noResourceFoundExceptionHandler(NoResourceFoundException ex) {
        return ResponseEntity.status(404).body(new ResponseDTO<>(true, "El recurso solicitado no existe"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTO<List<ValidationDTO>>> validationExceptionHandler(MethodArgumentNotValidException ex) {
        List<ValidationDTO> errors = new ArrayList<>();
        for (FieldError e : ex.getBindingResult().getFieldErrors()) {
            errors.add(new ValidationDTO(e.getField(), e.getDefaultMessage()));
        }
        return ResponseEntity.badRequest().body(new ResponseDTO<>(true, errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<String>> generalExceptionHandler(Exception e) {
        log.error("Error no controlado", e);
        return ResponseEntity.internalServerError().body(new ResponseDTO<>(true, "Error interno del servidor"));
    }
}
