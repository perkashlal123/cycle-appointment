package com.example.appointment.repository.mongo;

import static org.assertj.core.api.Assertions.*;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.example.appointment.model.Appointment;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

/**
 * Some unit tests for the
 * {@link com.example.appointment.repository.mongo.AppointmentMongoRepository},
 * relying on an in-memory MongoDB, using MongoDB Java server.
 *
 * These tests are meant to verify all paths of our repository.
 */
public class AppointmentMongoRepositoryTest {

	private static MongoServer server;
	private static InetSocketAddress serverAddress;

	private MongoClient client;
	private AppointmentMongoRepository appointmentRepository;
	private MongoCollection<Document> appointmentCollection;

	private static final String APPOINTMENT_DB_NAME = "appointment";
	private static final String APPOINTMENT_COLLECTION_NAME = "appointment";

	@BeforeClass
	public static void setupServer() {
		server = new MongoServer(new MemoryBackend());
		// bind on a random local port
		serverAddress = server.bind();
	}

	@AfterClass
	public static void shutdownServer() {
		server.shutdown();
	}

	@Before
	public void setup() {
		client = new MongoClient(new ServerAddress(serverAddress));
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
	public void testFindAllWhenDatabaseIsEmpty() {
		assertThat(appointmentRepository.findAll()).isEmpty();
	}

	@Test
	public void testFindAllWhenDatabaseIsNotEmpty() {
		addTestAppointmentToDatabase("1", "test1");
		addTestAppointmentToDatabase("2", "test2");
		assertThat(appointmentRepository.findAll())
			.containsExactly(
				new Appointment("1", "test1"),
				new Appointment("2", "test2"));
	}

	@Test
	public void testFindByIdNotFound() {
		assertThat(appointmentRepository.findById("1"))
			.isNull();
	}

	@Test
	public void testFindByIdFound() {
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
