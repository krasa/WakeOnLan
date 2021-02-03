package krasa.wakeonlan.data;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Config {
	private static final Logger log = LoggerFactory.getLogger(Config.class);

	String vpnServerIp = "";
	String password = "";
	String address = "";
	String user = "";
	String command = "wakeonlan <mac>";

	public static Config load() throws IOException {
		Config config = null;
		File file = new File("conf/defaultConfig.json");
		if (file.exists()) {
			FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8);
			config = new Gson().fromJson(fileReader, Config.class);
			fileReader.close();
			return config;
		} else {
			log.error("Config not found, current Path=" + new File("").getAbsolutePath());
			throw new IllegalStateException("Je to rozbite. Chybi config: " + file.getAbsolutePath());
		}
	}

	public String getVpnServerIp() {
		return vpnServerIp;
	}

	public void setVpnServerIp(String vpnServerIp) {
		this.vpnServerIp = vpnServerIp;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
}
