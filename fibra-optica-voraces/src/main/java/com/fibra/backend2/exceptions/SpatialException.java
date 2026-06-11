package com.fibra.backend2.exceptions;

public class SpatialException extends RuntimeException {

    public SpatialException(String message) {
        super(message);
    }

    public SpatialException(String message, Throwable cause) {
        super(message, cause);
    }
}
