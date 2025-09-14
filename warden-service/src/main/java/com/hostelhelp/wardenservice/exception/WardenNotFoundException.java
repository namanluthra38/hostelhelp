package com.hostelhelp.wardenservice.exception;

public class WardenNotFoundException extends RuntimeException {
    public WardenNotFoundException(String message) {
        super(message);
    }
}
