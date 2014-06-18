package com.minecade.minettt.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.ResourceBundle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.minecade.minettt.listeners.PlayerListener;
import com.minecade.minettt.listeners.WorldListener;
import com.minecade.minettt.minecade.MinecadePersistence;
import com.minecade.minettt.players.TTTPlayer;
import com.minecade.minettt.players.Teams;

public class MineTTT extends JavaPlugin {

	private final long delay = 20L * 0;
	private final long tick = 5L;

	private static MineTTT plugin;
	
	private ResourceBundle resourceBundle;

	/**
	 * Returns the plugin currently in use by the server.
	 * 
	 * @return The MineTTT instance.
	 */
	public static MineTTT getPlugin() {
		return plugin;
	}

	private ServerStatusThread serverStatusThread;
	private Server server;
	public Teams teamHandler;
	public MainThread thread;
	public BukkitScheduler scheduler;
	private CommandHandler commandHandler;
	public FileManager fileManager;
	public MinecadePersistence minecade;
	public int id;

	public ChestHandler chestHandler;

	private PluginManager manager;

	public PlayerListener playerListener;

	/**
	 * Passes MineTTT commands to the CommandHandler for processing.
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		return this.commandHandler.handleCommand(sender, cmd, label, args);
	}

	/**
	 * Resets all players, ends the game, saves the players, and resets all
	 * chests when the plugin disables.
	 */
	@Override
	public void onDisable() {
		this.thread.destroyHologram();
		if (this.thread.getGameStatus() != GameState.OFF) {
			this.thread.endGame(true);
			Bukkit.broadcastMessage(MineTTT.getPlugin().getMessage("minettt.end"));
		}
		this.fileManager.savePlayers();
		this.chestHandler.resetChests();

		for (Player player : Bukkit.getOnlinePlayers()) {
			for (Player other : Bukkit.getOnlinePlayers()) {
				if (other == player || player.canSee(other)) {
					continue;
				}
				player.showPlayer(other);
			}
		}

		this.playerListener.resetDeadPlayers();
		this.serverStatusThread.update(true);
	}

	@Override
    public void onLoad() {
        plugin = this;
        // default config
        saveDefaultConfig();
        // server custom config should override this
        getConfig().options().copyDefaults(false);
        // i18n config
        resourceBundle = ResourceBundle.getBundle("messages", new Locale(getConfig().getString("locale"), "en"));
    }
	
	/**
	 * On the enabling of the plugin, this registers all listeners, handlers,
	 * managers, and threads.
	 */
	@Override
	public void onEnable() {
		try {
			//plugin = this;
			this.server = getServer();
			this.manager = this.server.getPluginManager();
			registerListeners();
			this.teamHandler = new Teams(this);
			this.commandHandler = new CommandHandler(this);
			this.fileManager = new FileManager(this);
			this.thread = new MainThread(this);
			this.chestHandler = new ChestHandler(this);
			this.id = this.fileManager.getID();
			this.serverStatusThread = new ServerStatusThread(this);

			try {
				this.minecade = new MinecadePersistence(this,
						FileManager.SQLhostname, FileManager.SQLport,
						FileManager.SQLdatabaseName, FileManager.SQLusername,
						FileManager.SQLpassword);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			TTTPlayer.setPlugin(this);

			this.scheduler = this.server.getScheduler();

			this.scheduler.scheduleSyncRepeatingTask(this, this.thread,
					this.delay, this.tick);
			this.scheduler.scheduleSyncRepeatingTask(this,
					this.serverStatusThread, 20, 20);

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

			TTTPlayer.newScoreboards();
			TTTPlayer.showAllPreGameScoreboards();
		} catch (Exception e) {
			this.server
					.broadcastMessage(MineTTT.getPlugin().getMessage("minettt.error"));
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
					.format(Calendar.getInstance().getTime());
			File file = new File(getDataFolder(), "crash" + timeStamp + ".log");
			file.getParentFile().mkdirs();
			try {
				e.printStackTrace(new PrintWriter(file));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			this.server.shutdown();
		}
	}

	/**
	 * This registers all listeners.
	 */
	private void registerListeners() {
		this.playerListener = new PlayerListener(this);

		this.manager.registerEvents(this.playerListener, this);
		this.manager.registerEvents(new WorldListener(), this);
	}
	
	public String getMessage(String key) {
        if (resourceBundle.containsKey(key)) {
            return ChatColor.translateAlternateColorCodes('&', resourceBundle.getString(key));
        }
        return key;
    }

}
