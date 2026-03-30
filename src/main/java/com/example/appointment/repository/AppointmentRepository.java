package com.example.appointment.repository;

import java.util.List;

import com.example.appointment.model.Appointment;

public interface AppointmentRepository {
	public List<Appointment> findAll();

	public Appointment findById(String id);

	public void save(Appointment appointment);

	public void delete(String id);
}
