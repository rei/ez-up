package com.rei.ezup;

public class TemplatingException extends RuntimeException {
    private static final long serialVersionUID = -6796346721375079688L;
    
    public TemplatingException(String message, Exception e) {
        super(message, e);
    }
}