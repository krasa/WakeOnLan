package krasa.wakeonlan.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import krasa.wakeonlan.JavaFxApplication;
import krasa.wakeonlan.NetworkService;
import krasa.wakeonlan.SettingsData;
import krasa.wakeonlan.utils.ThreadDump;
import krasa.wakeonlan.utils.ThreadDumper;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Component
@FxmlView
public class MainController implements Initializable {
	private static final Logger log = LoggerFactory.getLogger(MainController.class);


    public TextArea status;
    public ComboBox wakeUpPerson;
    @Autowired
	NetworkService networkService;

	@FXML
	public void wakeUp(ActionEvent actionEvent) {
		SettingsData data = Settings.load();
		status.clear();
        String ip = wakeUpPerson.getSelectionModel();
		data.setLastClient(ip);
		data.save();
		try {
			networkService.wakeUp(ip, this);
		} catch (Throwable e) {
			displayException(ip, e);
		}
	}

	private void displayException(String ip, Throwable e) {
		log.error(ip, e);
		StringWriter errorMsg = new StringWriter();
		e.printStackTrace(new PrintWriter(errorMsg));
		status.appendText(errorMsg.toString() + "\n");
	}

	@FXML
	public void ping(ActionEvent actionEvent) {
        String ip = wakeUpPerson.getSelectionModel().getSelectedItem();

		try {
			boolean ping = networkService.ping(ip);
			String s = ping ? "OK" : "ERROR";
			status.setText("Ping " + ip + ": " + s + "\n");

		} catch (Throwable e) {
			displayException(ip, e);
		}
	}

	@FXML
	public void settings(ActionEvent actionEvent) {
		Stage dialog = new Stage();
		dialog.initModality(Modality.APPLICATION_MODAL);

		// enable style
		FXMLLoader loader = new FXMLLoader(Settings.class.getResource("Settings.fxml"));
		try {
			Parent root = loader.load();
			Settings controller = (Settings) loader.getController();
			Scene scene = new Scene(root);
			dialog.setScene(scene);
			String styleSheetURL = JavaFxApplication.class.getResource("dark.css").toString();
			scene.getStylesheets().add(styleSheetURL);
			dialog.setTitle("Nastaveni");
			dialog.addEventHandler(KeyEvent.KEY_RELEASED, (KeyEvent event) -> {
				if (KeyCode.ESCAPE == event.getCode()) {
					dialog.close();
				}
			});
			dialog.show();
		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}

	@FXML
	public void diagnostics(ActionEvent actionEvent) {
		ThreadDump threadDumpInfo = ThreadDumper.getThreadDumpInfo(ManagementFactory.getThreadMXBean());
		String rawDump = threadDumpInfo.getRawDump();
		status.setText(rawDump);
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {

		SettingsData data = Settings.load();
		String lastClient = data.getLastClient();
        wakeUpPerson.setText(lastClient);
		if (Strings.isBlank(lastClient)) {
			List<SettingsData.WakeUpClient> clients = data.getClients();
			for (SettingsData.WakeUpClient client : clients) {
				wakeUpAddress.setText(client.getName());
				break;
			}
		}
	}

	public void error(String ip, Throwable e) {
		Platform.runLater(() -> displayException(ip, e));
	}

	public void append(String line) {
		Platform.runLater(() -> status.appendText(line + "\n"));

	}

	public void kill(ActionEvent actionEvent) {
		networkService.kill();
	}
}
