package com.sitescribe.common;

/** The request is valid but the resource is not in a state that allows it. Maps to 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
