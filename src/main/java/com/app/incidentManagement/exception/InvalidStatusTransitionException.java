package com.app.incidentManagement.exception;

public class InvalidStatusTransitionException
extends RuntimeException {

public InvalidStatusTransitionException(String message) {
super(message);
}
}
