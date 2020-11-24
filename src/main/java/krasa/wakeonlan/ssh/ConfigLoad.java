package krasa.wakeonlan.ssh;

import krasa.wakeonlan.*;
import krasa.wakeonlan.controller.*;
import net.schmizz.sshj.*;
import net.schmizz.sshj.connection.channel.direct.*;
import net.schmizz.sshj.transport.verification.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;

public class ConfigLoad {
	private static final Logger log = LoggerFactory.getLogger(ConfigLoad.class);
	protected static final Logger sshOutput = LoggerFactory.getLogger("ssh");

	private final SettingsData settingsData;

	private Session session;
	private SSHClient ssh;

	private volatile boolean stop;


	public ConfigLoad(SettingsData settingsData) {
		this.settingsData = settingsData;
	}

	public void execute() {
		try {
			ssh = new SSHClient();
			ssh.addHostKeyVerifier(new PromiscuousVerifier());
			log.info("Connecting to: " + settingsData.getAddress());
			String[] split = settingsData.getAddress().split(":");
			if (split.length != 2) {
				throw new IllegalArgumentException("invalid server address (ip:port)");
			}
			ssh.connect(split[0], Integer.parseInt(split[1]));
			ssh.authPassword(settingsData.getUser(), settingsData.getPassword());

			session = ssh.startSession();
			session.allocateDefaultPTY();
			String command = "cat ./iplist.txt";
			log.info("Executing command: " + command);
			Session.Command cmd = session.exec(command);

			receiveLine(cmd.getInputStream());

			log.info("disconnecting");
			session.close();

			Integer exitStatus = cmd.getExitStatus();
			if (exitStatus == null) {
				exitStatus = -1;
			}
			log.info("exit status: " + exitStatus);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			try {
				releaseResources();
			} catch (Throwable e) {
				log.error("process #finally failed", e);
			}
		}
	}


	public void receiveLine(InputStream inputStream) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
		String line;

		List<SettingsData.WakeUpClient> wakeUpIps = new ArrayList<>();
		while (keepReceiving() && (line = r.readLine()) != null) {
			String[] split = line.split("=");
			if (split.length == 3) {
				wakeUpIps.add(new SettingsData.WakeUpClient(split[0], split[1], split[2]));
			} else {
				log.error("Invalid format: " + line);
			}
			sshOutput.info(line);
		}
		log.info("setClients: " + wakeUpIps);
		settingsData.setClients(wakeUpIps);
		Settings.save(settingsData);
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
