package com.chrisvds.gymapi.state;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * The whole client-side state blob, stored opaquely. One row for now
 * ({@code id = "default"}); becomes per-user once there's auth.
 */
@Entity
@Table(name = "workout_state")
public class WorkoutState {

	@Id
	@Column(length = 64)
	private String id;

	@Column(columnDefinition = "text", nullable = false)
	private String payload;

	@Column(nullable = false)
	private Instant updatedAt;

	protected WorkoutState() {
	}

	public WorkoutState(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
