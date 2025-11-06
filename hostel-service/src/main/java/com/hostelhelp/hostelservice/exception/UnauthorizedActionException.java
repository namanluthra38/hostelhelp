package com.hostelhelp.hostelservice.exception;


public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String message) { super(message); }
}