package krasa.wakeonlan;

import krasa.wakeonlan.controller.*;

import java.util.*;

public class SettingsData {
	String password = "";
	String address = "";
	String user = "";
	String command = "wakeonlan <mac>";
	String lastClient;

	List<WakeUpClient> clients = new ArrayList<>();

	public String getCommand() {
		return command;
	}

	public String getLastClient() {
		return lastClient;
	}

	public void setLastClient(String lastClient) {
		this.lastClient = lastClient;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public List<WakeUpClient> getClients() {
		return clients;
	}

	public void setClients(List<WakeUpClient> clients) {
		this.clients = clients;
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


	public void save() {
		Settings.save(this);
	}

	public WakeUpClient getClientByIp(String ip) {
		for (WakeUpClient client : clients) {
			if (client.getIp().equals("ip")) {
				return client;
			}
		}
		return null;
	}


	public static class WakeUpClient {
		String ip;
		String mac;

		public WakeUpClient(String ip, String mac) {
			this.ip = ip;
			this.mac = mac;
		}

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public String getMac() {
			return mac;
		}

		public void setMac(String mac) {
			this.mac = mac;
		}

		@Override
		public String toString() {
			return "WakeUpClient{" +
				"ip='" + ip + '\'' +
				", mac='" + mac + '\'' +
				'}';
		}
	}


	@Override
	public String toString() {
		return "SettingsData{" +
			"password='" + password + '\'' +
			", address='" + address + '\'' +
			", user='" + user + '\'' +
			", command='" + command + '\'' +
			", clients=" + clients +
			'}';
	}
}
