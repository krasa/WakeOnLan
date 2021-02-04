package krasa.wakeonlan.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
	public Button wakeUp;
	public Button remoteDesktop;

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
		wakeUp.disableProperty().bind(
				usersComboBox.valueProperty().isNull());
		remoteDesktop.disableProperty().bind(
				usersComboBox.valueProperty().isNull()
		);

		try {
			config = Config.load();
			appendLater("Ověřování připojení k VPN");

			networkService.async(() -> {
				try {
					boolean ok = networkService.ping(config.getVpnServerIp());
					if (ok) {
						log.info("ping ok");
						appendLater("Připojení k VPN - OK");
						appendLater("Připojování k WOL serveru");

						networkService.async(() -> {
							try {
								appendLaterWithoutNewLine("Vyhledávání aktualizací programu");
								new Updater(config, this).execute();
							} catch (IOException e) {
								log.error("", e);
								appendLater(" - chyba připojení");
							} catch (Throwable e) {
								log.error("", e);
								appendLater(" - chyba");
//									appendLater(Notifications.stacktraceToString(e));
								//	Notifications.showError(Thread.currentThread(), e);
							}

							try {
								appendLaterWithoutNewLine("Načítání uživatelů");
								int users = new UsersLoad(config).execute();
								if (users > 0) {
									appendLater(" - OK");
								} else {
									appendLater(" - chyba");
								}
								Platform.runLater(this::fillComboBox);
							} catch (IOException e) {
								log.error("", e);
								appendLater(" - chyba připojení");
							} catch (Throwable e) {
								log.error("", e);
								appendLater(" - chyba");
								//	appendLater(Notifications.stacktraceToString(e));
								//	Notifications.showError(Thread.currentThread(), e);
							}
						});

					} else {
						log.info("ping fail");
						appendLater("Chyba - zkontrolujte, že připojeni VPN je aktivní");
					}
				} catch (Throwable e) {
					log.warn("", e);
					appendLater(Notifications.stacktraceToString(e));
					appendLater("Obecná chyba, kontaktujte administrátora");
					//				Notifications.showError(Thread.currentThread(), e);
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
		usersComboBox.getItems().clear();
		List<String> collect = users.stream().map(UserData.WakeUpUser::getName).sorted().collect(Collectors.toList());
		usersComboBox.getItems().addAll(collect);

		String lastUser = data.getLastUser();
		if (collect.contains(lastUser)) {
			SingleSelectionModel<String> selectionModel = usersComboBox.getSelectionModel();
			selectionModel.select(lastUser);
			if (StringUtils.isBlank(lastUser)) {
				for (UserData.WakeUpUser user : users) {
					selectionModel.select(user.getName());
					break;
				}
			}
		}
	}

	public void error(String ip, Throwable e) {
		Platform.runLater(() -> displayException(ip, e));
	}

	public void appendLater(String line) {
		Platform.runLater(() -> status.appendText(line + "\n"));
	}

	public void appendLaterWithoutNewLine(String line) {
		Platform.runLater(() -> status.appendText(line));
	}

	public void appendNow(String line) {
		Platform.runLater(() -> status.appendText(line + "\n"));

	}

	public void kill(ActionEvent actionEvent) {
		networkService.kill();
	}

	public void appendProgress(long percent, long newPercent) {
		Platform.runLater(() -> {
			String text = status.getText();
			int i = text.lastIndexOf("\n");
			int i2 = text.indexOf(String.valueOf(percent), i);
			String substring = text;
			if (i2 > 0) {
				substring = text.substring(0, i2);
			}
			status.setText(substring + newPercent);
		});
	}

	public void appendProgress(String s) {
		Platform.runLater(() -> {
			String text = status.getText();
			status.setText(text.substring(0, text.lastIndexOf("\n")) + "\n" + s);
		});
	}
}