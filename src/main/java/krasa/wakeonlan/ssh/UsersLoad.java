package krasa.wakeonlan.ssh;

import krasa.wakeonlan.data.Config;
import krasa.wakeonlan.data.UserData;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class UsersLoad extends AbstractSshProcess {
	private static final Logger log = LoggerFactory.getLogger(UsersLoad.class);

	public UsersLoad(Config config) {
		super(config);
	}

	public int execute() {
		try {
			ssh = connect();

			session = ssh.startSession();
			session.allocateDefaultPTY();
			String command = "cat ./iplist.txt";
			log.info("Executing command: " + command);
			Session.Command cmd = session.exec(command);

			int users = processOutput(cmd.getInputStream());

			log.info("disconnecting");
			session.close();

			Integer exitStatus = cmd.getExitStatus();
			if (exitStatus == null) {
				exitStatus = -1;
			}
			log.info("exit status: " + exitStatus);
			return users;
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


	public int processOutput(InputStream inputStream) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		UserData userData = UserData.load();
		List<UserData.WakeUpUser> users = new ArrayList<>();
		while (keepReceiving() && (line = r.readLine()) != null) {
			String[] split = line.split("=");
			if (split.length == 3) {
				users.add(new UserData.WakeUpUser(split[0], split[1], split[2]));
			} else {
				log.error("Invalid format: " + line);
			}
			sshOutput.info(line);
		}
		log.info("setUsers: " + users);
		userData.setUsers(users);
		UserData.save(userData);
		log.debug("receiving done");
		return users.size();
	}


}
