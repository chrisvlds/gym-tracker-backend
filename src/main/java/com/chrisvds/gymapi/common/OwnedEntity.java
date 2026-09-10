package com.chrisvds.gymapi.common;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

/**
 * Shared columns for every top-level resource: the {@code owner} it belongs to
 * (see {@link com.chrisvds.gymapi.identity.CurrentUser}) and audit timestamps.
 * {@code updatedAt} doubles as the optimistic-concurrency token.
 */
@MappedSuperclass
public abstract class OwnedEntity {

	@Column(nullable = false, length = 190)
	private String owner;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	/** Millisecond precision so the value survives a DB round-trip and stays comparable. */
	private static Instant now() {
		return Instant.now().truncatedTo(ChronoUnit.MILLIS);
	}

	@PrePersist
	void onCreate() {
		Instant now = now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = now();
	}

	/** Call before an explicit save when child collections changed but no column did. */
	public void touch() {
		this.updatedAt = now();
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
