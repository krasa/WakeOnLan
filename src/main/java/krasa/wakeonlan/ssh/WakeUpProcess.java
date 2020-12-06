package krasa.wakeonlan.ssh;

import krasa.wakeonlan.controller.MainController;
import krasa.wakeonlan.data.Config;
import krasa.wakeonlan.data.UserData;
import net.schmizz.sshj.connection.channel.direct.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WakeUpProcess extends AbstractSshProcess {

	private final UserData.WakeUpUser user;

	public WakeUpProcess(UserData.WakeUpUser user, Config config) {
		super(config);
		if (user == null) {
			throw new IllegalArgumentException("user is null");
		}
		this.user = user;
	}

	public void execute(MainController mainController) throws Throwable {
		try {
			mainController.appendLater("Connecting to: " + config.getAddress());
			connect();

			session = ssh.startSession();
			session.allocateDefaultPTY();

			String command = config.getCommand().replace("<mac>", user.getMac());
			mainController.appendLater("Executing command: " + command);
			Session.Command cmd = session.exec(command);

			processOutput(cmd.getInputStream(), mainController);
			mainController.appendLater("disconnecting");

			log.info("disconnecting");
			session.close();

			Integer exitStatus = cmd.getExitStatus();
			if (exitStatus == null) {
				exitStatus = -1;
			}
			log.info("exit status: " + exitStatus);
		} catch (Throwable e) {
			throw e;
		} finally {
			try {
				releaseResources();
			} catch (Throwable e) {
				log.error("process #finally failed", e);
			}
			mainController.appendLater("done");
		}
	}

	public void processOutput(InputStream inputStream, MainController mainController) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
		String line;

		while (keepReceiving() && (line = r.readLine()) != null) {
			sshOutput.info(line);
			mainController.appendLater("\t" + line);
		}
		log.debug("receiving done");
	}
	    
}
