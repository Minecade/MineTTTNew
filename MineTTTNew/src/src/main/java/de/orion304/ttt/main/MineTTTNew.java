package src.main.java.de.orion304.ttt.main;

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class MineTTTNew extends JavaPlugin {

	private long delay = 20L * 0;
	private long tick = 20L * 5;

	Server server;

	public void onEnable() {
		server = getServer();
		registerListeners();
		Teams teamHandler = new Teams(this);
		MainThread thread = new MainThread(this);

		BukkitScheduler scheduler = server.getScheduler();

		scheduler.scheduleSyncRepeatingTask(this, thread, delay, tick);
	}

	public void onDisable() {

	}

	private void registerListeners() {

	}

}
