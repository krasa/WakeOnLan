package krasa.wakeonlan;

import krasa.wakeonlan.controller.Settings;

import java.util.ArrayList;
import java.util.List;

public class SettingsData {
	String password="";
	String address="";
	String user="";
	String command="wakeonlan <ip>";
	List<String> wakeUpIps=new ArrayList<>();

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public List<String> getWakeUpIps() {
		return wakeUpIps;
	}

	public void setWakeUpIps(List<String> wakeUpIps) {
		this.wakeUpIps = wakeUpIps;
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

	public void addWakeUpIp(String text) {
		wakeUpIps.remove(text);
		wakeUpIps.add(text);
	}

	public void save() {
		Settings.save(this);
	}
}
