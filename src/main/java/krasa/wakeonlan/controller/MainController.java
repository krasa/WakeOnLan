package krasa.wakeonlan.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import krasa.wakeonlan.JavaFxApplication;
import krasa.wakeonlan.data.Config;
import krasa.wakeonlan.data.UserData;
import krasa.wakeonlan.ssh.NetworkService;
import krasa.wakeonlan.ssh.Updater;
import krasa.wakeonlan.ssh.UsersLoad;
import krasa.wakeonlan.utils.ThreadDump;
import krasa.wakeonlan.utils.ThreadDumper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController implements Initializable {
	private static final Logger log = LoggerFactory.getLogger(MainController.class);


	public TextArea status;
	public ComboBox<String> usersComboBox;

	private NetworkService networkService;
	private volatile Config config = new Config();

	public MainController() {
		networkService = new NetworkService();
	}

	@FXML
	public void wakeUp(ActionEvent actionEvent) {
		UserData data = UserData.load();
		status.clear();
		String user = usersComboBox.getSelectionModel().getSelectedItem();
		data.setLastUser(user);
		data.save();
		try {
			networkService.wakeUp(UserData.load().getUserByName(user), this, config);
		} catch (Throwable e) {
			displayException(user, e);
		}
	}

	@FXML
	public void remoteDesktop(ActionEvent actionEvent) throws IOException {
		String userName = usersComboBox.getSelectionModel().getSelectedItem();
		UserData.WakeUpUser user = UserData.load().getUserByName(userName);
		if (user != null) {
			ProcessBuilder processBuilder = new ProcessBuilder("mstsc", "/v:" + user.getIp(), "/f");
			processBuilder.start();
		} else {
			throw new RuntimeException("User not found " + userName);
		}
	}

	private void displayException(String ip, Throwable e) {
		log.error(ip, e);
		status.appendText(Notifications.stacktraceToString(e) + "\n");
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
		try {
			config = Config.load();
			networkService.async(() -> {
				try {
					new Updater(config, this).execute();
				} catch (Throwable e) {
					log.error("", e);
					appendLater("Chyba!");
					appendLater(Notifications.stacktraceToString(e));
					//				Notifications.showError(Thread.currentThread(), e);
				}
			});
			networkService.async(() -> {
				try {
					int users = new UsersLoad(config).execute();
					if (users > 0) {
						appendLater("Načítání uživatelů - OK");
					} else {
						appendLater("Načítání uživatelů - Selhalo");
					}
					Platform.runLater(this::fillComboBox);
				} catch (Throwable e) {
					log.error("", e);
					appendLater("Chyba!");
					appendLater(Notifications.stacktraceToString(e));
					//	Notifications.showError(Thread.currentThread(), e);
				}
			});
		} catch (Throwable e) {
			log.error("", e);
			appendNow("Chyba!");
			appendNow(Notifications.stacktraceToString(e));
		}

		fillComboBox();
	}

	private void fillComboBox() {
		UserData data = UserData.load();
		List<UserData.WakeUpUser> users = data.getUsers();
		usersComboBox.getItems().addAll(users.stream().map(UserData.WakeUpUser::getName).collect(Collectors.toList()));

		String lastUser = data.getLastUser();
		SingleSelectionModel<String> selectionModel = usersComboBox.getSelectionModel();
		selectionModel.select(lastUser);
		if (StringUtils.isBlank(lastUser)) {
			for (UserData.WakeUpUser user : users) {
				selectionModel.select(user.getName());
				break;
			}
		}
	}

	public void error(String ip, Throwable e) {
		Platform.runLater(() -> displayException(ip, e));
	}

	public void appendLater(String line) {
		Platform.runLater(() -> status.appendText(line + "\n"));

	}

	public void appendNow(String line) {
		Platform.runLater(() -> status.appendText(line + "\n"));

	}

	public void kill(ActionEvent actionEvent) {
		networkService.kill();
	}
}
