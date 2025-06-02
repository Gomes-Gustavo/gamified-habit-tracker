package com.habitracker.serviceapi.exceptions;

public class HabitNotFoundException extends Exception {
    public HabitNotFoundException(String message) {
        super(message);
    }
    public HabitNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}