package com.example.appointment.controller;

import static org.mockito.Mockito.*;
import static java.util.Arrays.asList;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.appointment.model.Appointment;
import com.example.appointment.repository.AppointmentRepository;
import com.example.appointment.view.AppointmentView;

public class AppointmentControllerTest {

	@Mock
	private AppointmentRepository appointmentRepository;

	@Mock
	private AppointmentView appointmentView;

	@InjectMocks
	private AppointmentController appointmentController;

	private AutoCloseable closeable;

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
	}

	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}

	@Test
	public void testAllAppointments() {
		List<Appointment> appointments = asList(new Appointment());
		when(appointmentRepository.findAll())
			.thenReturn(appointments);
		appointmentController.allAppointments();
		verify(appointmentView)
			.showAllAppointments(appointments);
	}

	@Test
	public void testNewAppointmentWhenAppointmentDoesNotAlreadyExist() {
		Appointment appointment = new Appointment("1", "test");
		when(appointmentRepository.findById("1")).
			thenReturn(null);
		appointmentController.newAppointment(appointment);
		InOrder inOrder = inOrder(appointmentRepository, appointmentView);
		inOrder.verify(appointmentRepository).save(appointment);
		inOrder.verify(appointmentView).appointmentAdded(appointment);
	}

	@Test
	public void testNewAppointmentWhenAppointmentAlreadyExists() {
		Appointment appointmentToAdd = new Appointment("1", "test");
		Appointment existingAppointment = new Appointment("1", "existing");
		when(appointmentRepository.findById("1")).
			thenReturn(existingAppointment);
		appointmentController.newAppointment(appointmentToAdd);
		verify(appointmentView)
			.showError("Already existing appointment with id 1", existingAppointment);
		verifyNoMoreInteractions(ignoreStubs(appointmentRepository));
	}

	@Test
	public void testDeleteAppointmentWhenAppointmentExists() {
		Appointment appointmentToDelete = new Appointment("1", "test");
		when(appointmentRepository.findById("1")).
			thenReturn(appointmentToDelete);
		appointmentController.deleteAppointment(appointmentToDelete);
		InOrder inOrder = inOrder(appointmentRepository, appointmentView);
		inOrder.verify(appointmentRepository).delete("1");
		inOrder.verify(appointmentView).appointmentRemoved(appointmentToDelete);
	}

	@Test
	public void testDeleteAppointmentWhenAppointmentDoesNotExist() {
		Appointment appointment = new Appointment("1", "test");
		when(appointmentRepository.findById("1")).
			thenReturn(null);
		appointmentController.deleteAppointment(appointment);
		verify(appointmentView)
			.showErrorAppointmentNotFound("No existing appointment with id 1", appointment);
		verifyNoMoreInteractions(ignoreStubs(appointmentRepository));
	}
}
