package krasa.wakeonlan.controller;

import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import krasa.wakeonlan.JavaFxApplication;
import krasa.wakeonlan.SettingsData;
import krasa.wakeonlan.utils.UiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class Settings implements Initializable {
	private static final Logger log = LoggerFactory.getLogger(Settings.class);
	public static final String FILE = "settings.json";
	public static final String SETTINGS = "settings";
	@FXML
	public TextField password;
	@FXML
	public TextField address;
	@FXML
	public TextField user;
	@FXML
	public TextField command;


	public static SettingsData load() {
		try {
			Preferences preferences = Preferences.userRoot().node(JavaFxApplication.NODE_NAME);
			String s = preferences.get(SETTINGS, "");
			SettingsData settingsData = new Gson().fromJson(s, SettingsData.class);

			if (settingsData == null) {
				settingsData = new SettingsData();
			}
			return settingsData;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static void save(SettingsData settingsData) {
		try {
			String str = new Gson().toJson(settingsData);
			Preferences preferences = Preferences.userRoot().node(JavaFxApplication.NODE_NAME);
			preferences.put(SETTINGS, str);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public void ok(ActionEvent actionEvent) {
		SettingsData data = load();
		data.setAddress(address.getText());
		data.setPassword(password.getText());
		data.setUser(user.getText());
		data.setCommand(command.getText());
		data.save();
		UiUtils.getStage(address).close();
	}

	public void cancel(ActionEvent actionEvent) {
		UiUtils.getStage(address).close();
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {

		SettingsData load = load();
		password.setText(load.getPassword());
		user.setText(load.getUser());
		address.setText(load.getAddress());
		command.setText(load.getCommand());
	}
}
