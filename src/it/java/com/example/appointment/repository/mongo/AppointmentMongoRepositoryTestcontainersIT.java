package com.example.appointment.repository.mongo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.MongoDBContainer;

import com.example.appointment.model.Appointment;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * Some integration tests for the
 * {@link com.example.appointment.repository.mongo.AppointmentMongoRepository},
 * relying on Testcontainers.
 *
 * These tests do not necessarily make sense: they are meant to be a
 * demonstration of Testcontainers.
 */
public class AppointmentMongoRepositoryTestcontainersIT {

	@ClassRule
	public static final MongoDBContainer mongo =
		new MongoDBContainer("mongo:5");

	private MongoClient client;
	private AppointmentMongoRepository appointmentRepository;
	private MongoCollection<Document> appointmentCollection;

	private static final String APPOINTMENT_DB_NAME = "appointment";
	private static final String APPOINTMENT_COLLECTION_NAME = "appointment";

	@Before
	public void setup() {
		client = new MongoClient(
			new ServerAddress(
				mongo.getHost(),
				mongo.getFirstMappedPort()));
		appointmentRepository =
			new AppointmentMongoRepository(client, APPOINTMENT_DB_NAME, APPOINTMENT_COLLECTION_NAME);
		MongoDatabase database = client.getDatabase(APPOINTMENT_DB_NAME);
		// make sure we always start with a clean database
		database.drop();
		appointmentCollection = database.getCollection(APPOINTMENT_COLLECTION_NAME);
	}

	@After
	public void tearDown() {
		client.close();
	}

	@Test
	public void testFindAll() {
		addTestAppointmentToDatabase("1", "test1");
		addTestAppointmentToDatabase("2", "test2");
		assertThat(appointmentRepository.findAll())
			.containsExactly(
				new Appointment("1", "test1"),
				new Appointment("2", "test2"));
	}

	@Test
	public void testFindById() {
		addTestAppointmentToDatabase("1", "test1");
		addTestAppointmentToDatabase("2", "test2");
		assertThat(appointmentRepository.findById("2"))
			.isEqualTo(new Appointment("2", "test2"));
	}

	@Test
	public void testSave() {
		Appointment appointment = new Appointment("1", "added appointment");
		appointmentRepository.save(appointment);
		assertThat(readAllAppointmentsFromDatabase())
			.containsExactly(appointment);
	}

	@Test
	public void testDelete() {
		addTestAppointmentToDatabase("1", "test1");
		appointmentRepository.delete("1");
		assertThat(readAllAppointmentsFromDatabase())
			.isEmpty();
	}

	private void addTestAppointmentToDatabase(String id, String description) {
		appointmentCollection.insertOne(
				new Document()
					.append("id", id)
					.append("description", description));
	}

	private List<Appointment> readAllAppointmentsFromDatabase() {
		return StreamSupport.
			stream(appointmentCollection.find().spliterator(), false)
				.map(d -> new Appointment(""+d.get("id"), ""+d.get("description")))
				.collect(Collectors.toList());
	}
}
