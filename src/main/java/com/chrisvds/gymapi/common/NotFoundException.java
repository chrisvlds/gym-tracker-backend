package com.chrisvds.gymapi.common;

/** Thrown when a requested resource doesn't exist (or isn't owned by the caller). */
public class NotFoundException extends RuntimeException {

	public NotFoundException(String message) {
		super(message);
	}
}
