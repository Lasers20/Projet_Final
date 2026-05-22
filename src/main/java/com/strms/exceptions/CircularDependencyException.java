package com.strms.exceptions;

public class CircularDependencyException extends Exception {
    public CircularDependencyException(String message) {
        super(message);
    }
}
