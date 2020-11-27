package krasa.wakeonlan.ssh;

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
import java.util.ArrayList;
import java.util.List;

public class UsersLoad {
	private static final Logger log = LoggerFactory.getLogger(UsersLoad.class);
	protected static final Logger sshOutput = LoggerFactory.getLogger("ssh");


	private Session session;
	private SSHClient ssh;

	private volatile boolean stop;
	private Config config;


	public UsersLoad(Config config) {
		this.config = config;
	}

	public void execute() {
		try {
			ssh = new SSHClient();
			ssh.addHostKeyVerifier(new PromiscuousVerifier());
			log.info("Connecting to: " + config.getAddress());
			String[] split = config.getAddress().split(":");
			if (split.length != 2) {
				throw new IllegalArgumentException("invalid server address (ip:port)");
			}
			ssh.connect(split[0], Integer.parseInt(split[1]));
			ssh.authPassword(config.getUser(), config.getPassword());

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
			throw new RuntimeException("Load failed. Server=" + config.getAddress(), e);
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
		UserData userData = UserData.load();
		List<UserData.WakeUpUser> wakeUpIps = new ArrayList<>();
		while (keepReceiving() && (line = r.readLine()) != null) {
			String[] split = line.split("=");
			if (split.length == 3) {
				wakeUpIps.add(new UserData.WakeUpUser(split[0], split[1], split[2]));
			} else {
				log.error("Invalid format: " + line);
			}
			sshOutput.info(line);
		}
		log.info("setUsers: " + wakeUpIps);
		userData.setUsers(wakeUpIps);
		UserData.save(userData);
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
