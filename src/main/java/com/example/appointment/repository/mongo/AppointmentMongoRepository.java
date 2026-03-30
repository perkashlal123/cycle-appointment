package com.example.appointment.repository.mongo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;

import com.example.appointment.model.Appointment;
import com.example.appointment.repository.AppointmentRepository;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

public class AppointmentMongoRepository implements AppointmentRepository {

	private MongoCollection<Document> appointmentCollection;

	public AppointmentMongoRepository(MongoClient client, String databaseName, String collectionName) {
		appointmentCollection = client
			.getDatabase(databaseName)
			.getCollection(collectionName);
	}

	@Override
	public List<Appointment> findAll() {
		return StreamSupport.
				stream(appointmentCollection.find().spliterator(), false)
				.map(this::fromDocumentToAppointment)
				.collect(Collectors.toList());
	}

	private Appointment fromDocumentToAppointment(Document d) {
		return new Appointment(""+d.get("id"), ""+d.get("description"));
	}

	@Override
	public Appointment findById(String id) {
		Document d = appointmentCollection.find(Filters.eq("id", id)).first();
		if (d != null)
			return fromDocumentToAppointment(d);
		return null;
	}

	@Override
	public void save(Appointment appointment) {
		appointmentCollection.insertOne(
			new Document()
				.append("id", appointment.getId())
				.append("description", appointment.getDescription()));
	}

	@Override
	public void delete(String id) {
		appointmentCollection.deleteOne(Filters.eq("id", id));
	}

}
