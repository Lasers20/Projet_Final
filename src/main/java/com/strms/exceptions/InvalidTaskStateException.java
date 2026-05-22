package com.strms.exceptions;

public class InvalidTaskStateException extends Exception {
    public InvalidTaskStateException(String message) {
        super(message);
    }
}
