package com.habitracker.serviceapi.exceptions;

// Em UserNotFoundException.java
public class UserNotFoundException extends Exception {
    public UserNotFoundException(String message) {
        super(message);
    }
    // Construtor opcional com causa
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
