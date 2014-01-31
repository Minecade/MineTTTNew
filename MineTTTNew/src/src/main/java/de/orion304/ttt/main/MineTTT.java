package src.main.java.de.orion304.ttt.main;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import src.main.java.de.orion304.ttt.listeners.PlayerListener;
import src.main.java.de.orion304.ttt.players.Teams;

public class MineTTT extends JavaPlugin {

	private long delay = 20L * 0;
	private long tick = 20L * 1;

	public Server server;

	public Teams teamHandler;
	public MainThread thread;
	public BukkitScheduler scheduler;
	private CommandHandler commandHandler;

	private PluginManager manager;

	private PlayerListener playerListener;

	public void onEnable() {
		server = getServer();
		manager = server.getPluginManager();
		registerListeners();
		teamHandler = new Teams(this);
		thread = new MainThread(this);
		commandHandler = new CommandHandler(this);

		scheduler = server.getScheduler();

		scheduler.scheduleSyncRepeatingTask(this, thread, delay, tick);
	}

	public void onDisable() {

	}

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		return commandHandler.handleCommand(sender, cmd, label, args);
	}

	private void registerListeners() {
		playerListener = new PlayerListener(this);

		manager.registerEvents(playerListener, this);
	}

}
