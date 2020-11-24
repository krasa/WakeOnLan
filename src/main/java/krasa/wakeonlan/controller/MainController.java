package krasa.wakeonlan.controller;

import javafx.application.*;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.*;
import krasa.wakeonlan.*;
import krasa.wakeonlan.utils.*;
import net.rgielen.fxweaver.core.*;
import org.apache.logging.log4j.util.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.lang.management.*;
import java.net.*;
import java.util.*;

@Component
@FxmlView
public class MainController implements Initializable {
	private static final Logger log = LoggerFactory.getLogger(MainController.class);


	public TextField wakeUpAddress;
	public TextArea status;
	@Autowired
	NetworkService networkService;

	@FXML
	public void wakeUp(ActionEvent actionEvent) {
		SettingsData data = Settings.load();
		status.clear();
		String ip = wakeUpAddress.getText();
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
		String ip = wakeUpAddress.getText();

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
		wakeUpAddress.setText(lastClient);
		if (Strings.isBlank(lastClient)) {
			List<SettingsData.WakeUpClient> clients = data.getClients();
			for (SettingsData.WakeUpClient client : clients) {
				wakeUpAddress.setText(client.getIp());
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
