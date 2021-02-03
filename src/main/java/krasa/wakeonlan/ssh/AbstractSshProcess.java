package krasa.wakeonlan.ssh;

import krasa.wakeonlan.data.Config;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractSshProcess {
	protected static final Logger log = LoggerFactory.getLogger(WakeUpProcess.class);
	protected static final Logger sshOutput = LoggerFactory.getLogger("ssh");
	protected final Config config;
	protected Session session;
	protected SSHClient ssh;
	private volatile boolean stop;

	public AbstractSshProcess(Config config) {
		this.config = config;
		if (config == null) {
			throw new IllegalArgumentException("config is null");
		}
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

	protected boolean keepReceiving() {
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

	protected SSHClient connect() throws IOException {
		log.info("Connecting to: " + config.getAddress());
		ssh = new SSHClient();
		ssh.setConnectTimeout(3000);
		ssh.addHostKeyVerifier(new PromiscuousVerifier());
//			ssh.loadKnownHosts();
		String[] split = config.getAddress().split(":");
		if (split.length != 2) {
			throw new IllegalArgumentException("invalid server address (ip:port)");
		}
		ssh.connect(split[0], Integer.parseInt(split[1]));
		ssh.authPassword(config.getUser(), config.getPassword());
		return ssh;
	}
}
