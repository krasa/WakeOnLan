package krasa.wakeonlan.controller;

import com.google.gson.Gson;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import krasa.wakeonlan.SettingsData;
import krasa.wakeonlan.UiUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class Settings  implements Initializable {
	private static final Logger log = LoggerFactory.getLogger(Settings.class);
	public static final String FILE = "settings.json";
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
			File file = new File(FILE);
			if (file.exists()) {
				FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8);
				SettingsData settingsData = new Gson().fromJson(fileReader, SettingsData.class);
				fileReader.close();
				
				if (settingsData == null) {
					settingsData=new SettingsData();
				}
				return settingsData;
			} else {
				log.warn("settings does not exists");
				return new SettingsData();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void save(SettingsData settingsData) {
		try {
			File file = new File(FILE);
			FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8);
			fileWriter.write(new Gson().toJson(settingsData));
			fileWriter.close();
			
		} catch (Exception e) {
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
		user	.setText(load.getUser());
		address	.setText(load.getAddress());
		command	.setText(load.getCommand());
	}
}
