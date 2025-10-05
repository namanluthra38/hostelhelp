package com.hostelhelp.requestservice.service;

public class StudentNotFoundRemoteException extends RuntimeException {
    public StudentNotFoundRemoteException(String message) {
        super(message);
    }
}

