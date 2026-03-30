package com.example.appointment.view;

import java.util.List;

import com.example.appointment.model.Appointment;

public interface AppointmentView {

	void showAllAppointments(List<Appointment> appointments);

	void showError(String message, Appointment appointment);

	void appointmentAdded(Appointment appointment);

	void appointmentRemoved(Appointment appointment);

	void showErrorAppointmentNotFound(String message, Appointment appointment);

}
