package krasa.wakeonlan;

import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

public class WakeOnLanApplication {
	static {
		SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
	}

	private static final Logger log = LoggerFactory.getLogger(WakeOnLanApplication.class);

	public static void main(String[] args) {
		Application.launch(JavaFxApplication.class, args);
	}

}
