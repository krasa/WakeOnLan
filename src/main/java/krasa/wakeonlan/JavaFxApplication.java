package krasa.wakeonlan;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import krasa.wakeonlan.controller.MainController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.prefs.Preferences;


public class JavaFxApplication extends Application {
	private static final Logger log = LoggerFactory.getLogger(JavaFxApplication.class);
	private static final String WINDOW_POSITION_X = "Window_Position_X";
	private static final String WINDOW_POSITION_Y = "Window_Position_Y";
	private static final String WINDOW_WIDTH = "Window_Width";
	private static final String WINDOW_HEIGHT = "Window_Height";
	private static final double DEFAULT_WIDTH = 800;
	private static final double DEFAULT_HEIGHT = 600;
	private static final String NODE_NAME = "Propouzeč PC";


	@Override
	public void init() {
		log.info("java.version=" + System.getProperty("java.version"));
		Thread.setDefaultUncaughtExceptionHandler(Notifications::showError);
	}

	@Override
	public void start(Stage stage) throws IOException {
		FXMLLoader loader = new FXMLLoader(MainController.class.getResource("MainController.fxml"));
		Parent root = loader.load();
		Scene scene = new Scene(root);


		String styleSheetURL = JavaFxApplication.class.getResource("dark.css").toString();
//		stage.getIcons().add(MyUtils.getImage("icon.png"));

		// enable style
		scene.getStylesheets().add(styleSheetURL);
		stage.setTitle("Probouze\u010D PC");
		stage.setScene(scene);
		stage.setOnCloseRequest((final WindowEvent event) -> {
			Preferences preferences = Preferences.userRoot().node(NODE_NAME);
			preferences.putDouble(WINDOW_POSITION_X, stage.getX());
			preferences.putDouble(WINDOW_POSITION_Y, stage.getY());
			preferences.putDouble(WINDOW_WIDTH, stage.getWidth());
			preferences.putDouble(WINDOW_HEIGHT, stage.getHeight());
		});
		stage.setOnCloseRequest(event -> {
			Platform.exit();
			System.exit(0);
		});
		stage.getIcons().add(new Image(JavaFxApplication.class.getResourceAsStream("/krasa/wakeonlan/icon.png")));
		log.info("show");


		Preferences pref = Preferences.userRoot().node(NODE_NAME);


		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		double x = pref.getDouble(WINDOW_POSITION_X, (screenBounds.getWidth() - DEFAULT_WIDTH) / 2);
		double y = pref.getDouble(WINDOW_POSITION_Y, (screenBounds.getHeight() - DEFAULT_HEIGHT) / 2);
		double width = pref.getDouble(WINDOW_WIDTH, DEFAULT_WIDTH);
		double height = pref.getDouble(WINDOW_HEIGHT, DEFAULT_HEIGHT);
		stage.setX(x);
		stage.setY(y);
//		stage.setWidth(width);
//		stage.setHeight(height);


		stage.show();
	}

	@Override
	public void stop() {
		Platform.exit();
	}

}
