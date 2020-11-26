package krasa.wakeonlan.ssh;

import krasa.wakeonlan.controller.MainController;
import krasa.wakeonlan.controller.Notifications;
import krasa.wakeonlan.data.Config;
import krasa.wakeonlan.data.UserData;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

public class NetworkService {
	List<SshjProcess> processList = new CopyOnWriteArrayList<>();

	private ExecutorService executorService = Executors.newFixedThreadPool(4,
			new ThreadFactory() {
				public Thread newThread(Runnable r) {
					Thread t = Executors.defaultThreadFactory().newThread(r);
					t.setDaemon(true);
					return t;
				}
			});

	public void async(Runnable xxx) {
		executorService.execute(xxx);
	}

	public void wakeUp(UserData.WakeUpUser user, MainController mainController, Config config) throws IOException {
		SshjProcess sshjProcess = new SshjProcess(user, config);
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
