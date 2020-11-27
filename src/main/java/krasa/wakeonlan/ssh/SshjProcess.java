package krasa.wakeonlan.ssh;

import krasa.wakeonlan.controller.MainController;
import krasa.wakeonlan.data.Config;
import krasa.wakeonlan.data.UserData;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SshjProcess {
	private static final Logger log = LoggerFactory.getLogger(SshjProcess.class);
	protected static final Logger sshOutput = LoggerFactory.getLogger("ssh");

	private final UserData.WakeUpUser user;
	private final Config config;

	private Session session;
	private SSHClient ssh;

	private volatile boolean stop;


	public SshjProcess(UserData.WakeUpUser user, Config config) {
		if (config == null) {
			throw new IllegalArgumentException("config is null");
		}
		if (user == null) {
			throw new IllegalArgumentException("user is null");
		}
		this.user = user;
		this.config = config;
	}

	public void execute(MainController mainController) throws Throwable {
		try {
			ssh = new SSHClient();
			ssh.addHostKeyVerifier(new PromiscuousVerifier());
//			ssh.loadKnownHosts();
			mainController.appendLater("Connecting to: " + config.getAddress());
			String[] split = config.getAddress().split(":");
			if (split.length != 2) {
				throw new IllegalArgumentException("invalid server address (ip:port)");
			}
			ssh.connect(split[0], Integer.parseInt(split[1]));
			ssh.authPassword(config.getUser(), config.getPassword());

			session = ssh.startSession();
			session.allocateDefaultPTY();

			String command = config.getCommand().replace("<mac>", user.getMac());
			mainController.appendLater("Executing command: " + command);
			Session.Command cmd = session.exec(command);

			receiveLine(cmd.getInputStream(), mainController);
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


	public void receiveLine(InputStream inputStream, MainController mainController) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
		String line;

		while (keepReceiving() && (line = r.readLine()) != null) {
			sshOutput.info(line);
			mainController.appendLater("\t" + line);
		}
		log.debug("receiving done");
	}


	protected void releaseResources() {
		if (session != null) {
			try {
				session.close();
			} catch (Exception e) {
				log.error(String.valueOf(e), e);
				throw new RuntimeException(e);
			} finally {
				try {
					ssh.disconnect();
				} catch (IOException e) {
					log.error(String.valueOf(e), e);
				}
			}
		}
	}


	private boolean keepReceiving() {
		if (stop) {
			log.warn("#keepReceiving stop=true");
		}
		return !stop;
	}

	public void stop() {
		stop = true;
	}

	public void kill() {
		if (session.isOpen()) {
			releaseResources();
		}
	}
}
