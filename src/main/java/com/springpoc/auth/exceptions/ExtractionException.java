package com.springpoc.auth.exceptions;

public class ExtractionException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ExtractionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExtractionException(String message) {
        super(message);
    }
    
    
}
