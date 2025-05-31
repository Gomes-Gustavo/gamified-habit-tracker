package com.habitracker.serviceapi.exceptions;

public class PersistenceException extends Exception {
    public PersistenceException(String message) {
        super(message);
    }
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}