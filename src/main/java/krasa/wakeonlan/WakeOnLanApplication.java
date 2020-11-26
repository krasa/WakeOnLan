package krasa.wakeonlan;

import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WakeOnLanApplication {

	private static final Logger log = LoggerFactory.getLogger(WakeOnLanApplication.class);

	public static void main(String[] args) {
		Application.launch(JavaFxApplication.class, args);
	}

}
