package com.labndbnb.landbnb.exceptions;

/** Error de negocio esperado: su mensaje es seguro de mostrar al cliente. */
public class ExceptionAlert extends RuntimeException {
    public ExceptionAlert(String message) {
        super(message);
    }
}
