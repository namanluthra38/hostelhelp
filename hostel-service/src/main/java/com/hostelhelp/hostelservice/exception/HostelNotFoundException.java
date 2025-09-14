package com.hostelhelp.hostelservice.exception;

public class HostelNotFoundException extends RuntimeException {
    public HostelNotFoundException(String message) {
        super(message);
    }
}
