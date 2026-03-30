package com.example.appointment.controller;

import static org.mockito.Mockito.*;
import static java.util.Arrays.asList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.appointment.model.Appointment;
import com.example.appointment.repository.AppointmentRepository;
import com.example.appointment.repository.mongo.AppointmentMongoRepository;
import com.example.appointment.view.AppointmentView;
import com.mongodb.MongoClient;

/**
 * Communicates with a MongoDB server on localhost; start MongoDB with Docker with
 *
 * <pre>
 * docker run -p 27017:27017 --rm mongo:5
 * </pre>
 */
public class AppointmentControllerIT {

	@Mock
	private AppointmentView appointmentView;

	private AppointmentRepository appointmentRepository;

	private AppointmentController appointmentController;

	private static final String APPOINTMENT_DB_NAME = "appointment";
	private static final String APPOINTMENT_COLLECTION_NAME = "appointment";

	private AutoCloseable closeable;

	private static int mongoPort =
		Integer.parseInt(System.getProperty("mongo.port", "27017"));

	@Before
	public void setUp() {
		closeable = MockitoAnnotations.openMocks(this);
		appointmentRepository =
			new AppointmentMongoRepository(new MongoClient("localhost", mongoPort),
					APPOINTMENT_DB_NAME, APPOINTMENT_COLLECTION_NAME);
		// explicit empty the database through the repository
		for (Appointment appointment : appointmentRepository.findAll()) {
			appointmentRepository.delete(appointment.getId());
		}
		appointmentController = new AppointmentController(appointmentView, appointmentRepository);
	}

	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}

	@Test
	public void testAllAppointments() {
		Appointment appointment = new Appointment("1", "test");
		appointmentRepository.save(appointment);
		appointmentController.allAppointments();
		verify(appointmentView)
			.showAllAppointments(asList(appointment));
	}

	@Test
	public void testNewAppointment() {
		Appointment appointment = new Appointment("1", "test");
		appointmentController.newAppointment(appointment);
		verify(appointmentView).appointmentAdded(appointment);
	}

	@Test
	public void testDeleteAppointment() {
		Appointment appointmentToDelete = new Appointment("1", "test");
		appointmentRepository.save(appointmentToDelete);
		appointmentController.deleteAppointment(appointmentToDelete);
		verify(appointmentView).appointmentRemoved(appointmentToDelete);
	}

}
