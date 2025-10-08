package com.labndbnb.landbnb.exceptions;

public class MissingToken extends RuntimeException {
    public MissingToken() {
        super("Missing token");
    }
}
