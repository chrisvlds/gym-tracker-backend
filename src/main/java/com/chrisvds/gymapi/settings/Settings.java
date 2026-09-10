package com.chrisvds.gymapi.settings;

import com.chrisvds.gymapi.common.OwnedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Per-owner preferences. One row per owner (id == owner). */
@Entity
@Table(name = "settings")
public class Settings extends OwnedEntity {

	@Id
	@Column(length = 190)
	private String id;

	@Column(nullable = false, length = 4)
	private String unit = "lb";

	/** Default rest between sets, seconds — used by the UI timer. */
	@Column(nullable = false)
	private int restSeconds = 120;

	protected Settings() {
	}

	public Settings(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = "kg".equalsIgnoreCase(unit) ? "kg" : "lb";
	}

	public int getRestSeconds() {
		return restSeconds;
	}

	public void setRestSeconds(int restSeconds) {
		this.restSeconds = Math.max(0, restSeconds);
	}
}
