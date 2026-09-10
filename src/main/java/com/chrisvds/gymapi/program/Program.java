package com.chrisvds.gymapi.program;

import java.util.ArrayList;
import java.util.List;

import com.chrisvds.gymapi.common.OwnedEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

/**
 * The training program: a set of day templates plus a repeating {@code cycle} of
 * day keys (e.g. {@code push, pull, legs-quad, push, pull, legs-ham}). One per
 * owner. Which day is "next" is derived from the owner's last logged workout,
 * not stored here.
 */
@Entity
@Table(name = "program")
public class Program extends OwnedEntity {

	@Id
	@Column(length = 64)
	private String id;

	@Column(nullable = false)
	private String name = "My Program";

	@ElementCollection
	@CollectionTable(name = "program_cycle", joinColumns = @JoinColumn(name = "program_id"))
	@OrderColumn(name = "position")
	@Column(name = "day_key", length = 32, nullable = false)
	private List<String> cycle = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "program_id")
	@OrderColumn(name = "position")
	private List<ProgramDay> days = new ArrayList<>();

	protected Program() {
	}

	public Program(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getCycle() {
		return cycle;
	}

	public List<ProgramDay> getDays() {
		return days;
	}

	public ProgramDay day(String key) {
		return days.stream().filter(d -> d.getKey().equals(key)).findFirst().orElse(null);
	}
}
