package krasa.wakeonlan;

import krasa.wakeonlan.controller.MainController;
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

	private final String ip;
	private final SettingsData settingsData;

	private Session session;
	private SSHClient ssh;

	private volatile boolean stop;


	public SshjProcess(String ip, SettingsData settingsData) {
		this.ip = ip;
		this.settingsData = settingsData;
	}

	public void execute(MainController mainController) throws Throwable {
		try {
			ssh = new SSHClient();
			ssh.addHostKeyVerifier(new PromiscuousVerifier());
			ssh.loadKnownHosts();
			mainController.append("Connecting to: " + settingsData.getAddress());
			ssh.connect(settingsData.getAddress());
			ssh.authPassword(settingsData.getUser(), settingsData.getPassword());
			String command = settingsData.getCommand().replace("<ip>", ip);

			session = ssh.startSession();
			session.allocateDefaultPTY();
			mainController.append("Executing command: " + command);
			Session.Command cmd = session.exec(command);

			receiveLine(cmd.getInputStream(), mainController);
			mainController.append("disconnecting");

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
			mainController.append("done");
		}
	}


	public void receiveLine(InputStream inputStream, MainController mainController) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
		String line;

		while (keepReceiving() && (line = r.readLine()) != null) {
			sshOutput.info(line);
			mainController.append("\t" + line);
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
