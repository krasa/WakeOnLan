package krasa.wakeonlan.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import krasa.wakeonlan.data.UserData;
import krasa.wakeonlan.utils.UiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

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


	public void ok(ActionEvent actionEvent) {
		UserData data = UserData.load();
//		data.setAddress(address.getText());
//		data.setPassword(password.getText());
//		data.setUser(user.getText());
//		data.setCommand(command.getText());
		data.save();
		UiUtils.getStage(address).close();
	}

	public void cancel(ActionEvent actionEvent) {
		UiUtils.getStage(address).close();
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {

		UserData load = UserData.load();
//		password.setText(load.getPassword());
//		user.setText(load.getUser());
//		address.setText(load.getAddress());
//		command.setText(load.getCommand());
	}
}
