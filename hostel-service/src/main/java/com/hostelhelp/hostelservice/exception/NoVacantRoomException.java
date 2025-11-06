package com.hostelhelp.hostelservice.exception;

public class NoVacantRoomException extends RuntimeException {
    public NoVacantRoomException(String message) { super(message); }
}