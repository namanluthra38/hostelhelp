package com.hostelhelp.requestservice.exception;

public class NoVacantRoomException extends RuntimeException {
    public NoVacantRoomException(String message) { super(message); }
}