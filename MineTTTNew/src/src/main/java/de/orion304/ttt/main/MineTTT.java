package src.main.java.de.orion304.ttt.main;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import src.main.java.de.orion304.ttt.listeners.PlayerListener;
import src.main.java.de.orion304.ttt.listeners.WorldListener;
import src.main.java.de.orion304.ttt.minecade.MinecadePersistence;
import src.main.java.de.orion304.ttt.players.TTTPlayer;
import src.main.java.de.orion304.ttt.players.Teams;

public class MineTTT extends JavaPlugin {

	private long delay = 20L * 0;
	private long tick = 5L;

	private Server server;

	public Teams teamHandler;
	public MainThread thread;
	public BukkitScheduler scheduler;
	private CommandHandler commandHandler;
	public FileManager fileManager;
	public MinecadePersistence minecade;

	private PluginManager manager;

	private PlayerListener playerListener;

	public void onEnable() {
		server = getServer();
		manager = server.getPluginManager();
		registerListeners();
		teamHandler = new Teams(this);
		commandHandler = new CommandHandler(this);
		fileManager = new FileManager(this);
		thread = new MainThread(this);
		String host = "localhost";
		int port = 3306;
		String database = "Minecade";
		String username = "root";
		String password = "fagba11z";

		try {
			minecade = new MinecadePersistence(this, host, port, database,
					username, password);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TTTPlayer.setPlugin(this);

		scheduler = server.getScheduler();

		scheduler.scheduleSyncRepeatingTask(this, thread, delay, tick);

		for (World world : Bukkit.getWorlds()) {
			world.setAutoSave(false);
			world.setSpawnFlags(false, false);
			world.setStorm(false);
			world.setThundering(false);
			world.setTime(500);
			world.setWeatherDuration(Integer.MAX_VALUE);
			world.setGameRuleValue("commandBlockOutput", "false");
			world.setGameRuleValue("doDaylightCycle", "false");
			world.setGameRuleValue("doFireTick", "false");
			world.setGameRuleValue("doMobLoot", "false");
			world.setGameRuleValue("doMobSpawning", "false");
			world.setGameRuleValue("doTileDrops", "false");
			world.setGameRuleValue("keepInventory", "false");
			world.setGameRuleValue("mobGriefing", "false");
		}

		TTTPlayer.showAllPreGameScoreboards();
	}

	public void onDisable() {
		if (thread.getGameStatus() != GameState.OFF)
			thread.endGame(true);
		fileManager.savePlayers();

	}

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		return commandHandler.handleCommand(sender, cmd, label, args);
	}

	private void registerListeners() {
		playerListener = new PlayerListener(this);

		manager.registerEvents(playerListener, this);
		manager.registerEvents(new WorldListener(), this);
	}

}
