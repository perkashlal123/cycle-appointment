package com.example.appointment.model;

import java.util.Objects;

public class Appointment {
	private String id;
	private String description;

	public Appointment() {

	}

	public Appointment(String id, String description) {
		this.id = id;
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, description);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Appointment other = (Appointment) obj;
		return Objects.equals(id, other.id) && Objects.equals(description, other.description);
	}

	@Override
	public String toString() {
		return "Appointment [id=" + id + ", description=" + description + "]";
	}

}
