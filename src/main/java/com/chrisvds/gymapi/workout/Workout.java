package com.chrisvds.gymapi.workout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.chrisvds.gymapi.common.OwnedEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

/**
 * A single training session. {@code id} is client-generated so an offline-queued
 * create is idempotent. {@code programDayKey} links it to a
 * {@link com.chrisvds.gymapi.program.ProgramDay} ("push", "pull", …) when the
 * session was started from the program.
 */
@Entity
@Table(name = "workout")
public class Workout extends OwnedEntity {

	@Id
	@Column(length = 64)
	private String id;

	@Column(nullable = false)
	private LocalDate date;

	@Column(columnDefinition = "text", nullable = false)
	private String note = "";

	@Column(length = 32)
	private String programDayKey;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "workout_id")
	@OrderColumn(name = "position")
	private List<WorkoutEntry> entries = new ArrayList<>();

	protected Workout() {
	}

	public Workout(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note == null ? "" : note;
	}

	public String getProgramDayKey() {
		return programDayKey;
	}

	public void setProgramDayKey(String programDayKey) {
		this.programDayKey = programDayKey;
	}

	public List<WorkoutEntry> getEntries() {
		return entries;
	}
}
