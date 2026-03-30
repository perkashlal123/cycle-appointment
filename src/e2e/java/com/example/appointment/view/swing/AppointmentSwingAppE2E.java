package com.example.appointment.view.swing;

import static org.assertj.swing.launcher.ApplicationLauncher.*;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;

import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.bson.Document;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.MongoDBContainer;

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;

@RunWith(GUITestRunner.class)
public class AppointmentSwingAppE2E // NOSONAR we want the name this way
	extends AssertJSwingJUnitTestCase {

	@ClassRule
	public static final MongoDBContainer mongo =
		new MongoDBContainer("mongo:5");

	private static final String DB_NAME = "test-db";
	private static final String COLLECTION_NAME = "test-collection";

	private static final String APPOINTMENT_FIXTURE_1_ID = "1";
	private static final String APPOINTMENT_FIXTURE_1_DESCRIPTION = "first appointment";
	private static final String APPOINTMENT_FIXTURE_2_ID = "2";
	private static final String APPOINTMENT_FIXTURE_2_DESCRIPTION = "second appointment";

	private MongoClient mongoClient;

	private FrameFixture window;

	@Override
	protected void onSetUp() {
		String containerIpAddress = mongo.getHost();
		Integer mappedPort = mongo.getFirstMappedPort();
		mongoClient = new MongoClient(containerIpAddress, mappedPort);
		// always start with an empty database
		mongoClient.getDatabase(DB_NAME).drop();
		// add some appointments to the database
		addTestAppointmentToDatabase(APPOINTMENT_FIXTURE_1_ID, APPOINTMENT_FIXTURE_1_DESCRIPTION);
		addTestAppointmentToDatabase(APPOINTMENT_FIXTURE_2_ID, APPOINTMENT_FIXTURE_2_DESCRIPTION);
		// start the Swing application
		application("com.example.appointment.app.swing.AppointmentSwingApp")
			.withArgs(
				"--mongo-host=" + containerIpAddress,
				"--mongo-port=" + mappedPort.toString(),
				"--db-name=" + DB_NAME,
				"--db-collection=" + COLLECTION_NAME
			)
			.start();
		// get a reference of its JFrame
		window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "Appointment View".equals(frame.getTitle()) && frame.isShowing();
			}
		}).using(robot());
	}

	@Override
	protected void onTearDown() {
		mongoClient.close();
	}

	@Test @GUITest
	public void testOnStartAllDatabaseElementsAreShown() {
		assertThat(window.list().contents())
			.anySatisfy(e -> assertThat(e).contains(APPOINTMENT_FIXTURE_1_ID, APPOINTMENT_FIXTURE_1_DESCRIPTION))
			.anySatisfy(e -> assertThat(e).contains(APPOINTMENT_FIXTURE_2_ID, APPOINTMENT_FIXTURE_2_DESCRIPTION));
	}

	@Test @GUITest
	public void testAddButtonSuccess() {
		window.textBox("idTextBox").enterText("10");
		window.textBox("descriptionTextBox").enterText("new appointment");
		window.button(JButtonMatcher.withText("Add")).click();
		assertThat(window.list().contents())
			.anySatisfy(e -> assertThat(e).contains("10", "new appointment"));
	}

	@Test @GUITest
	public void testAddButtonError() {
		window.textBox("idTextBox").enterText(APPOINTMENT_FIXTURE_1_ID);
		window.textBox("descriptionTextBox").enterText("new one");
		window.button(JButtonMatcher.withText("Add")).click();
		assertThat(window.label("errorMessageLabel").text())
			.contains(APPOINTMENT_FIXTURE_1_ID, APPOINTMENT_FIXTURE_1_DESCRIPTION);
	}

	@Test @GUITest
	public void testDeleteButtonSuccess() {
		window.list("appointmentList")
			.selectItem(Pattern.compile(".*" + APPOINTMENT_FIXTURE_1_DESCRIPTION + ".*"));
		window.button(JButtonMatcher.withText("Delete Selected")).click();
		assertThat(window.list().contents())
			.noneMatch(e -> e.contains(APPOINTMENT_FIXTURE_1_DESCRIPTION));
	}

	@Test @GUITest
	public void testDeleteButtonError() {
		// select the appointment in the list...
		window.list("appointmentList")
			.selectItem(Pattern.compile(".*" + APPOINTMENT_FIXTURE_1_DESCRIPTION + ".*"));
		// ... in the meantime, manually remove the appointment from the database
		removeTestAppointmentFromDatabase(APPOINTMENT_FIXTURE_1_ID);
		// now press the delete button
		window.button(JButtonMatcher.withText("Delete Selected")).click();
		// and verify an error is shown
		assertThat(window.label("errorMessageLabel").text())
			.contains(APPOINTMENT_FIXTURE_1_ID, APPOINTMENT_FIXTURE_1_DESCRIPTION);
	}

	private void addTestAppointmentToDatabase(String id, String description) {
		mongoClient
			.getDatabase(DB_NAME)
			.getCollection(COLLECTION_NAME)
			.insertOne(
				new Document()
					.append("id", id)
					.append("description", description));
	}

	private void removeTestAppointmentFromDatabase(String id) {
		mongoClient
			.getDatabase(DB_NAME)
			.getCollection(COLLECTION_NAME)
			.deleteOne(Filters.eq("id", id));
	}
}
