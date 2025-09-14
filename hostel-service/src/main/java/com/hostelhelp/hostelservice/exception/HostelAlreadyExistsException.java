package com.hostelhelp.hostelservice.exception;

public class HostelAlreadyExistsException extends RuntimeException {
    public HostelAlreadyExistsException(String message) {
        super(message);
    }
}
