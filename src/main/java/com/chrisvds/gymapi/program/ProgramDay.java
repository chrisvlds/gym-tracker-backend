package com.chrisvds.gymapi.program;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

/** A named training day template ("push", "pull", …) and its planned exercises. */
@Entity
@Table(name = "program_day")
public class ProgramDay {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "day_key", nullable = false, length = 32)
	private String key;

	@Column(nullable = false)
	private String name;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "day_id")
	@OrderColumn(name = "position")
	private List<ProgramSlot> slots = new ArrayList<>();

	protected ProgramDay() {
	}

	public ProgramDay(String key, String name) {
		this.key = key;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ProgramSlot> getSlots() {
		return slots;
	}
}
