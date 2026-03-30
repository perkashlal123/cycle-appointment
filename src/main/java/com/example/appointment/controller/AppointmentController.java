package com.example.appointment.controller;

import com.example.appointment.model.Appointment;
import com.example.appointment.repository.AppointmentRepository;
import com.example.appointment.view.AppointmentView;

public class AppointmentController {
	private AppointmentView appointmentView;
	private AppointmentRepository appointmentRepository;

	public AppointmentController(AppointmentView appointmentView, AppointmentRepository appointmentRepository) {
		this.appointmentView = appointmentView;
		this.appointmentRepository = appointmentRepository;
	}

	public void allAppointments() {
		appointmentView.showAllAppointments(appointmentRepository.findAll());
	}

	public void newAppointment(Appointment appointment) {
		Appointment existingAppointment = appointmentRepository.findById(appointment.getId());
		if (existingAppointment != null) {
			appointmentView.showError("Already existing appointment with id " + appointment.getId(),
					existingAppointment);
			return;
		}

		appointmentRepository.save(appointment);
		appointmentView.appointmentAdded(appointment);
	}

	public void deleteAppointment(Appointment appointment) {
		if (appointmentRepository.findById(appointment.getId()) == null) {
			appointmentView.showErrorAppointmentNotFound("No existing appointment with id " + appointment.getId(),
					appointment);
			return;
		}

		appointmentRepository.delete(appointment.getId());
		appointmentView.appointmentRemoved(appointment);
	}

}
