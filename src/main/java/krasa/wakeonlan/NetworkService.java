package krasa.wakeonlan;

import krasa.wakeonlan.controller.MainController;
import krasa.wakeonlan.controller.Settings;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NetworkService {
	List<SshjProcess> processList = new CopyOnWriteArrayList<>();

	public void wakeUp(String ip, MainController mainController) throws IOException {
		SshjProcess sshjProcess = new SshjProcess(ip, Settings.load());
		CompletableFuture.supplyAsync(() -> {
			try {
				processList.add(sshjProcess);
				sshjProcess.execute(mainController);
			} catch (Throwable e) {
				Notifications.showError(Thread.currentThread(), e);
			}
			return "OK";
		}).whenCompleteAsync((s, throwable) -> {
			processList.remove(sshjProcess);
		});
	}

	public boolean ping(String ip) throws IOException {
		InetAddress address = InetAddress.getByName(ip);
		boolean reachable = address.isReachable(1000);
		return reachable;
	}

	public void kill() {
		for (SshjProcess sshjProcess : processList) {
			sshjProcess.stop();
			
			new Timer(true).schedule(new TimerTask() {
				@Override
				public void run() {
					sshjProcess.kill();
				}
			}, 5000);
		}
	}
}
