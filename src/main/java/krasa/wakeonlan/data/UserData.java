package krasa.wakeonlan.data;

import com.google.gson.Gson;
import krasa.wakeonlan.JavaFxApplication;
import krasa.wakeonlan.controller.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class UserData {
	String lastUser;

	List<WakeUpUser> users = new ArrayList<>();

	public static UserData load() {
		try {

			Preferences preferences = Preferences.userRoot().node(JavaFxApplication.NODE_NAME);
			String s = preferences.get(Settings.SETTINGS, "");
			UserData userData = new Gson().fromJson(s, UserData.class);

			if (userData == null) {
				userData = new UserData();
			}
			return userData;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static void save(UserData userData) {
		try {
			String str = new Gson().toJson(userData);
			Preferences preferences = Preferences.userRoot().node(JavaFxApplication.NODE_NAME);
			preferences.put(Settings.SETTINGS, str);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}


	public String getLastUser() {
		return lastUser;
	}

	public void setLastUser(String lastUser) {
		this.lastUser = lastUser;
	}

	public List<WakeUpUser> getUsers() {
		return users;
	}

	public void setUsers(List<WakeUpUser> users) {
		this.users = users;
	}

	public void save() {
		save(this);
	}

	public WakeUpUser getUserByName(String name) {
		for (WakeUpUser user : users) {
			if (user.getName().equals(name)) {
				return user;
			}
		}
		throw new IllegalArgumentException("User not found for ip:" + name);

	}

	public static class WakeUpUser {
		String ip;
		String mac;
		String name;

		public WakeUpUser(String name, String ip, String mac) {
			this.name = name;
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

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "WakeUpUser{" +
					"ip='" + ip + '\'' +
					", mac='" + mac + '\'' +
					", name='" + name + '\'' +
					'}';
		}
	}


	@Override
	public String toString() {
		return "UserData{" +
				"lastUser='" + lastUser + '\'' +
				", users=" + users +
				'}';
	}
}
