package krasa.wakeonlan.ssh;

import krasa.wakeonlan.controller.MainController;
import krasa.wakeonlan.data.Config;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.xfer.FileSystemFile;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

public class Updater extends AbstractSshProcess {
	private static final Logger log = LoggerFactory.getLogger(UsersLoad.class);
	private final MainController mainController;

	public Updater(Config config, MainController mainController) {
		super(config);
		this.mainController = mainController;
	}

	public void execute() {
		try {
			ssh = connect();

			session = ssh.startSession();
			session.allocateDefaultPTY();
			String command = "ls -t ./update/ | tail -n 1";
			log.info("Executing command: " + command);
			Session.Command cmd = session.exec(command);

			String fileName = processOutput(cmd.getInputStream());
			log.info(fileName);
			if (fileName.endsWith("exe") || fileName.endsWith("msi")) {
				String currentVersion = getCurrentVersion();
				log.info("currentVersion={}", currentVersion);
				String serverVersion = substringBeforeLast(substringAfter(fileName, "-"), ".");
				log.info("serverVersion={}", serverVersion);
				if (!StringUtils.equals(serverVersion, currentVersion)) {
					FileSystemFile localFile = new FileSystemFile(Files.createTempFile("", fileName).toFile());
					log.info("downloading {}", "./update/" + fileName);
					int copy = ssh.newSCPFileTransfer().newSCPDownloadClient().copy("\"./update/" + fileName + "\"", localFile);
					log.info("copy={}", copy);
					if (copy == 0) {
						log.info("starting {}", localFile.getFile().getAbsolutePath());
						mainController.appendLater("Spouštím aktualizaci.");
						ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", localFile.getFile().getAbsolutePath());
						processBuilder.start();
					}
				}

			}
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

	private String getCurrentVersion() throws IOException {
		Object o = System.getProperties().get("javafx.runtime.version");


		String[] apps = Path.of("app").toFile().list((dir, name) -> name.endsWith(".cfg"));
		String currentVersion = null;
		for (String app : apps) {
			Path app1 = Path.of("app", app);
			String s = Files.readString(app1);
			Properties properties = new Properties();
			properties.load(new FileInputStream(app1.toFile()));
			currentVersion = properties.getProperty("app.version");
			break;
		}
		return currentVersion;
	}


	public String processOutput(InputStream inputStream) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
		return r.readLine();
	}
}
