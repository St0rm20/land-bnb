package com.labndbnb.landbnb.exceptions;

/** If this exception occurs, call God D:
 */
public class IdAlreadyInUse extends RuntimeException {
    public IdAlreadyInUse(String message) {
        super(message);
    }
}
