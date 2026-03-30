package com.example.appointment.app.swing;

import java.awt.EventQueue;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.appointment.controller.AppointmentController;
import com.example.appointment.repository.mongo.AppointmentMongoRepository;
import com.example.appointment.view.swing.AppointmentSwingView;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(mixinStandardHelpOptions = true)
public class AppointmentSwingApp implements Callable<Void> {

	@Option(names = { "--mongo-host" }, description = "MongoDB host address")
	private String mongoHost = "localhost";

	@Option(names = { "--mongo-port" }, description = "MongoDB host port")
	private int mongoPort = 27017;

	@Option(names = { "--db-name" }, description = "Database name")
	private String databaseName = "appointment";

	@Option(names = { "--db-collection" }, description = "Collection name")
	private String collectionName = "appointment";

	public static void main(String[] args) {
		new CommandLine(new AppointmentSwingApp()).execute(args);
	}

	@Override
	public Void call() throws Exception {
		EventQueue.invokeLater(() -> {
			try {
				AppointmentMongoRepository appointmentRepository = new AppointmentMongoRepository(
						new MongoClient(new ServerAddress(mongoHost, mongoPort)), databaseName, collectionName);
				AppointmentSwingView appointmentView = new AppointmentSwingView();
				AppointmentController appointmentController = new AppointmentController(appointmentView, appointmentRepository);
				appointmentView.setAppointmentController(appointmentController);
				appointmentView.setVisible(true);
				appointmentController.allAppointments();
			} catch (Exception e) {
				Logger.getLogger(getClass().getName())
					.log(Level.SEVERE, "Exception", e);
			}
		});
		return null;
	}

}
