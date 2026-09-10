package com.chrisvds.gymapi.exercise;

import com.chrisvds.gymapi.common.OwnedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A movement in the catalog. Built-ins are seeded per owner with stable ids
 * ({@code chest-press-machine}, …); user-created ones get a generated id and
 * {@code custom = true}. Deleting a referenced exercise archives it instead of
 * removing the row, so old workouts keep their names.
 */
@Entity
@Table(name = "exercise")
public class Exercise extends OwnedEntity {

	@Id
	@Column(length = 64)
	private String id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, length = 32)
	private String muscle;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private Equipment equipment = Equipment.OTHER;

	@Column(nullable = false)
	private boolean custom = false;

	@Column(nullable = false)
	private boolean archived = false;

	protected Exercise() {
	}

	public Exercise(String id) {
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

	public String getMuscle() {
		return muscle;
	}

	public void setMuscle(String muscle) {
		this.muscle = muscle;
	}

	public Equipment getEquipment() {
		return equipment;
	}

	public void setEquipment(Equipment equipment) {
		this.equipment = equipment;
	}

	public boolean isCustom() {
		return custom;
	}

	public void setCustom(boolean custom) {
		this.custom = custom;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}
}
