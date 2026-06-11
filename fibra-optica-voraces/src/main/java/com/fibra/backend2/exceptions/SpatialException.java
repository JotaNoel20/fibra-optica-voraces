package com.fibra.backend2.exceptions;

public class SpatialException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SpatialException() {
        super();
    }

    public SpatialException(String message) {
        super(message);
    }

    public SpatialException(Throwable cause) {
        super(cause);
    }

    public SpatialException(String message, Throwable cause) {
        super(message, cause);
    }
}
