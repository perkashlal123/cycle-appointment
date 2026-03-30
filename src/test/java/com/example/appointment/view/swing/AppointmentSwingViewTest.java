package com.example.appointment.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import javax.swing.DefaultListModel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.appointment.controller.AppointmentController;
import com.example.appointment.model.Appointment;

@RunWith(GUITestRunner.class)
public class AppointmentSwingViewTest extends AssertJSwingJUnitTestCase {

	private FrameFixture window;

	private AppointmentSwingView appointmentSwingView;

	@Mock
	private AppointmentController appointmentController;

	private AutoCloseable closeable;

	@Override
	protected void onSetUp() {
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			appointmentSwingView = new AppointmentSwingView();
			appointmentSwingView.setAppointmentController(appointmentController);
			return appointmentSwingView;
		});
		window = new FrameFixture(robot(), appointmentSwingView);
		window.show(); // shows the frame to test
	}

	@Override
	protected void onTearDown() throws Exception {
		closeable.close();
	}

	@Test @GUITest
	public void testControlsInitialStates() {
		window.label(JLabelMatcher.withText("id"));
		window.textBox("idTextBox").requireEnabled();
		window.label(JLabelMatcher.withText("description"));
		window.textBox("descriptionTextBox").requireEnabled();
		window.button(JButtonMatcher.withText("Add")).requireDisabled();
		window.list("appointmentList");
		window.button(JButtonMatcher.withText("Delete Selected")).requireDisabled();
		window.label("errorMessageLabel").requireText(" ");
	}

	@Test
	public void testWhenIdAndDescriptionAreNonEmptyThenAddButtonShouldBeEnabled() {
		window.textBox("idTextBox").enterText("1");
		window.textBox("descriptionTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Add")).requireEnabled();
	}

	@Test
	public void testWhenEitherIdOrDescriptionAreBlankThenAddButtonShouldBeDisabled() {
		JTextComponentFixture idTextBox = window.textBox("idTextBox");
		JTextComponentFixture descriptionTextBox = window.textBox("descriptionTextBox");

		idTextBox.enterText("1");
		descriptionTextBox.enterText(" ");
		window.button(JButtonMatcher.withText("Add")).requireDisabled();

		idTextBox.setText("");
		descriptionTextBox.setText("");

		idTextBox.enterText(" ");
		descriptionTextBox.enterText("test");
		window.button(JButtonMatcher.withText("Add")).requireDisabled();
	}

	@Test
	public void testDeleteButtonShouldBeEnabledOnlyWhenAnAppointmentIsSelected() {
		GuiActionRunner.execute(() -> appointmentSwingView.getListAppointmentsModel().addElement(new Appointment("1", "test")));
		window.list("appointmentList").selectItem(0);
		JButtonFixture deleteButton = window.button(JButtonMatcher.withText("Delete Selected"));
		deleteButton.requireEnabled();
		window.list("appointmentList").clearSelection();
		deleteButton.requireDisabled();
	}

	@Test
	public void testsShowAllAppointmentsShouldAddAppointmentDescriptionsToTheList() {
		Appointment appointment1 = new Appointment("1", "test1");
		Appointment appointment2 = new Appointment("2", "test2");
		GuiActionRunner.execute(
			() -> appointmentSwingView.showAllAppointments(
					Arrays.asList(appointment1, appointment2))
		);
		String[] listContents = window.list().contents();
		assertThat(listContents)
			.containsExactly("1 - test1", "2 - test2");
	}

	@Test
	public void testShowErrorShouldShowTheMessageInTheErrorLabel() {
		Appointment appointment = new Appointment("1", "test1");
		GuiActionRunner.execute(
			() -> appointmentSwingView.showError("error message", appointment)
		);
		window.label("errorMessageLabel")
			.requireText("error message: 1 - test1");
	}

	@Test
	public void testShowErrorAppointmentNotFound() {
		// setup
		Appointment appointment1 = new Appointment("1", "test1");
		Appointment appointment2 = new Appointment("2", "test2");
		GuiActionRunner.execute(
			() -> {
				DefaultListModel<Appointment> listAppointmentsModel = appointmentSwingView.getListAppointmentsModel();
				listAppointmentsModel.addElement(appointment1);
				listAppointmentsModel.addElement(appointment2);
			}
		);
		GuiActionRunner.execute(
			() -> appointmentSwingView.showErrorAppointmentNotFound("error message", appointment1)
		);
		window.label("errorMessageLabel")
			.requireText("error message: 1 - test1");
		assertThat(window.list().contents())
			.containsExactly("2 - test2");
	}

	@Test
	public void testAppointmentAddedShouldAddTheAppointmentToTheListAndResetTheErrorLabel() {
		GuiActionRunner.execute(
				() ->
				appointmentSwingView.appointmentAdded(new Appointment("1", "test1"))
				);
		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly("1 - test1");
		window.label("errorMessageLabel").requireText(" ");
	}

	@Test
	public void testAppointmentRemovedShouldRemoveTheAppointmentFromTheListAndResetTheErrorLabel() {
		// setup
		Appointment appointment1 = new Appointment("1", "test1");
		Appointment appointment2 = new Appointment("2", "test2");
		GuiActionRunner.execute(
			() -> {
				DefaultListModel<Appointment> listAppointmentsModel = appointmentSwingView.getListAppointmentsModel();
				listAppointmentsModel.addElement(appointment1);
				listAppointmentsModel.addElement(appointment2);
			}
		);
		// execute
		GuiActionRunner.execute(
			() ->
			appointmentSwingView.appointmentRemoved(new Appointment("1", "test1"))
		);
		// verify
		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly("2 - test2");
		window.label("errorMessageLabel").requireText(" ");
	}

	@Test
	public void testAddButtonShouldDelegateToAppointmentControllerNewAppointment() {
		window.textBox("idTextBox").enterText("1");
		window.textBox("descriptionTextBox").enterText("test");
		window.button(JButtonMatcher.withText("Add")).click();
		verify(appointmentController).newAppointment(new Appointment("1", "test"));
	}

	@Test
	public void testDeleteButtonShouldDelegateToAppointmentControllerDeleteAppointment() {
		Appointment appointment1 = new Appointment("1", "test1");
		Appointment appointment2 = new Appointment("2", "test2");
		GuiActionRunner.execute(
			() -> {
				DefaultListModel<Appointment> listAppointmentsModel = appointmentSwingView.getListAppointmentsModel();
				listAppointmentsModel.addElement(appointment1);
				listAppointmentsModel.addElement(appointment2);
			}
		);
		window.list("appointmentList").selectItem(1);
		window.button(JButtonMatcher.withText("Delete Selected")).click();
		verify(appointmentController).deleteAppointment(appointment2);
	}
}
