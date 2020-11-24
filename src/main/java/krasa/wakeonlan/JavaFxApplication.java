package krasa.wakeonlan;

import javafx.application.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.stage.*;
import krasa.wakeonlan.controller.*;
import krasa.wakeonlan.ssh.*;
import net.rgielen.fxweaver.core.*;
import org.slf4j.*;
import org.springframework.boot.builder.*;
import org.springframework.context.*;

import java.util.prefs.*;


public class JavaFxApplication extends Application {
	private static final Logger log = LoggerFactory.getLogger(JavaFxApplication.class);
	private static final String WINDOW_POSITION_X = "Window_Position_X";
	private static final String WINDOW_POSITION_Y = "Window_Position_Y";
	private static final String WINDOW_WIDTH = "Window_Width";
	private static final String WINDOW_HEIGHT = "Window_Height";
	private static final double DEFAULT_X = 10;
	private static final double DEFAULT_Y = 10;
	private static final double DEFAULT_WIDTH = 800;
	private static final double DEFAULT_HEIGHT = 600;
	private static final String NODE_NAME = "Vzbuzovac";

	private ConfigurableApplicationContext applicationContext;

	@Override
	public void init() {
		Thread.setDefaultUncaughtExceptionHandler(Notifications::showError);
		String[] args = getParameters().getRaw().toArray(new String[0]);

		this.applicationContext = new SpringApplicationBuilder()
			.sources(WakeOnLanApplication.class)
			.run(args);
	}

	@Override
	public void start(Stage stage) {
		try {
			new ConfigLoad(Settings.load()).execute();
		} catch (Throwable e) {
			Notifications.showError(Thread.currentThread(), e);
		}

		FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
		Parent root = fxWeaver.loadView(MainController.class);
		Scene scene = new Scene(root);


		Preferences pref = Preferences.userRoot().node(NODE_NAME);
		double x = pref.getDouble(WINDOW_POSITION_X, DEFAULT_X);
		double y = pref.getDouble(WINDOW_POSITION_Y, DEFAULT_Y);
		double width = pref.getDouble(WINDOW_WIDTH, DEFAULT_WIDTH);
		double height = pref.getDouble(WINDOW_HEIGHT, DEFAULT_HEIGHT);
		stage.setX(x);
		stage.setY(y);
		stage.setWidth(width);
		stage.setHeight(height);
		
		
		String styleSheetURL = JavaFxApplication.class.getResource("dark.css").toString();
//		stage.getIcons().add(MyUtils.getImage("icon.png"));

		// enable style
		scene.getStylesheets().add(styleSheetURL);
		stage.setTitle("Probouzeč PC");
		stage.setScene(scene);
		stage.setOnCloseRequest((final WindowEvent event) -> {
			Preferences preferences = Preferences.userRoot().node(NODE_NAME);
			preferences.putDouble(WINDOW_POSITION_X, stage.getX());
			preferences.putDouble(WINDOW_POSITION_Y, stage.getY());
			preferences.putDouble(WINDOW_WIDTH, stage.getWidth());
			preferences.putDouble(WINDOW_HEIGHT, stage.getHeight());
		});

		stage.getIcons().add(new Image(JavaFxApplication.class.getResourceAsStream("/krasa/wakeonlan/icon.png")));
		log.info("show");
		stage.show();
	}

	@Override
	public void stop() {
		this.applicationContext.close();
		Platform.exit();
	}

}
