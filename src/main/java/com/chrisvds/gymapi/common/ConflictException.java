package com.chrisvds.gymapi.common;

/**
 * Thrown when an optimistic-concurrency check fails: the client sent an
 * {@code expectedUpdatedAt} that no longer matches the stored row, meaning
 * another device changed it first. The client is expected to re-fetch and
 * resolve.
 */
public class ConflictException extends RuntimeException {

	public ConflictException(String message) {
		super(message);
	}
}
