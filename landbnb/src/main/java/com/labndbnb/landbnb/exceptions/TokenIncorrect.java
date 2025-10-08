package com.labndbnb.landbnb.exceptions;

public class TokenIncorrect extends RuntimeException {
    public TokenIncorrect() {
        super("Invalid or expired token");
    }
}
