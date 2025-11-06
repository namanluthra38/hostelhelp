package com.hostelhelp.hostelservice.exception;

public class RemoteServiceException extends RuntimeException {
    public RemoteServiceException(String message, Throwable cause) { super(message, cause); }
    public RemoteServiceException(String message) { super(message); }
}