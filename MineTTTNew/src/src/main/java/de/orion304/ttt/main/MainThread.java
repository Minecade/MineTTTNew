package src.main.java.de.orion304.ttt.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import src.main.java.de.orion304.ttt.enderbar.BarAPI;
import src.main.java.de.orion304.ttt.players.DeathLocation;
import src.main.java.de.orion304.ttt.players.DetectiveCompass;
import src.main.java.de.orion304.ttt.players.PlayerTeam;
import src.main.java.de.orion304.ttt.players.TTTPlayer;
import src.main.java.org.orion304.utils.Hologram;
import src.main.java.org.orion304.utils.MathUtils;

public class MainThread implements Runnable {

	private static long preptime = FileManager.preparationTime + 500L;

	// Variables for use in processing
	private final MineTTT plugin;
	private final Server server;

	// Initialization variables
	public int playerThreshold = FileManager.minimumNumberOfPlayers;
	private ConcurrentHashMap<String, Location> arenaLocations = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, List<String>> arenaLores = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, ChatColor> arenaColors = new ConcurrentHashMap<>();
	private final List<String> loadedArenaKeys = new ArrayList<>();
	private Location arenaLocation, lobbyLocation;
	private final double radius = 4.5;
	private final Random random;

	private GameState state = GameState.OFF;
	private long time, starttime, lastannouncetime = 0;

	private final Hologram hologram, vipHologram;

	/**
	 * Creates a new MainThread for MineTTT, which handles anything to do with
	 * timing.
	 * 
	 * @param instance
	 *            The MineTTT instance.
	 */
	public MainThread(MineTTT instance) {
		this.plugin = instance;
		this.server = Bukkit.getServer();
		this.random = new Random();

		preptime = FileManager.preparationTime + 500L;
		this.lastannouncetime = 0;
		this.playerThreshold = FileManager.minimumNumberOfPlayers;

		this.arenaLocations = this.plugin.fileManager.getArenaLocations();
		this.arenaColors = this.plugin.fileManager.getArenaColors();
		this.arenaLores = this.plugin.fileManager.getArenaLores();
		this.lobbyLocation = this.plugin.fileManager.getLobbyLocation();

		if (this.lobbyLocation == null) {
			this.lobbyLocation = this.server.getWorlds().get(0)
					.getSpawnLocation();
		}

		String traitor = ChatColor.RED.toString() + ChatColor.ITALIC.toString()
				+ ChatColor.BOLD + "Traitor";
		String innocent = ChatColor.AQUA.toString()
				+ ChatColor.ITALIC.toString() + ChatColor.BOLD + "Innocent";
		String detective = ChatColor.DARK_AQUA.toString()
				+ ChatColor.ITALIC.toString() + ChatColor.BOLD + "Detective";
		String reset = ChatColor.RESET.toString() + ChatColor.BOLD.toString();

		this.hologram = new Hologram(instance, ChatColor.GOLD.toString()
				+ ChatColor.BOLD + ChatColor.UNDERLINE + "WELCOME TO MINETTT!",
				" ",
				reset + "MineTTT is a complex game about betrayal and trust!",
				reset + "Be a " + detective + reset + " to hunt down "
						+ traitor + "s!", reset + "Be a " + traitor + reset
						+ " to eliminate " + innocent + "s" + reset + " and "
						+ detective + "s!", reset + "Or be an " + innocent
						+ reset + " to find, hide from, and eliminate "
						+ traitor + "s!");
		this.vipHologram = new Hologram(instance,
				ChatColor.AQUA + "VIP PERKS:", "\u2022Two map votes",
				"\u2022Double the tokens", "\u2022Join full servers",
				"\u2022Choose your role", "\u2022Choose to spectate");
		showHologram();
	}

	/**
	 * Clears any dropped item in the worlds.
	 */
	private void clearDrops() {
		if (this.arenaLocation == null) {
			return;
		}
		for (Entity entity : this.arenaLocation.getWorld().getEntities()) {
			if (entity instanceof LivingEntity) {
				continue;
			}
			entity.remove();
		}
	}

	/**
	 * Clears all player inventories if they are playing.
	 */
	private void clearInventories() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(player);
			if (Tplayer.getTeam() != PlayerTeam.NONE) {
				Tools.clearInventory(player);
			}
		}
	}

	/**
	 * Destroys the holograms.
	 */
	public void destroyHologram() {
		this.hologram.destroy();
		this.vipHologram.destroy();
	}

	/**
	 * Ends the game.
	 * 
	 * @param forced
	 *            True if the game was forcibly ended, false if the game ended
	 *            by completing its normal course.
	 */
	public void endGame(boolean forced) {
		this.state = GameState.OFF;
		this.plugin.playerListener.resetDeadPlayers();
		makeAllVisible();
		killScoreboard();
		boolean traitorsWon = true;
		if (TTTPlayer.getNumberOfTraitors() == 0) {
			traitorsWon = false;
		}

		String endMessage = ChatColor.BOLD + "The game has ended! ";
		if (traitorsWon) {
			endMessage = FileManager.traitorColor + endMessage;
			endMessage += "The traitors were victorious!";
		} else {
			endMessage = FileManager.detectiveColor + endMessage;
			endMessage += " The detectives were victorious!";
		}

		if (!forced) {
			Bukkit.broadcastMessage(endMessage);
		}
		TTTPlayer.distributeCoinsToAll();
		TempBlock.revertAll();
		DeathLocation.reset();
		clearDrops();
		clearInventories();
		TTTPlayer.dealKarma();
		TTTPlayer.reset();
		TTTPlayer.showAllPreGameScoreboards();
		Claymore.reset();

		for (Player player : Bukkit.getOnlinePlayers()) {
			teleportPlayer(player, this.lobbyLocation);
			player.setGameMode(GameMode.ADVENTURE);
			player.setAllowFlight(false);
			player.setLevel(0);
			Tools.clearInventory(player);
			TTTPlayer.giveLeaveItem(player);
			TTTPlayer.giveStatsBook(player);
			BarAPI.removeBar(player);
			if (!forced) {
				Bungee.disconnect(player);
			}
		}

		reloadHologram();

		if (!forced) {
			// new BukkitRunnable() {
			// @Override
			// public void run() {
			// Bukkit.getServer().shutdown();
			// }
			// }.runTaskLater(this.plugin, 8 * 20);
		}

	}

	/**
	 * Makes the detective's compass point to the killer.
	 * 
	 * @param detective
	 *            The detective who has the compass.
	 * @param killer
	 *            The traitor who recently killed someone.
	 */
	public void findKiller(Player detective, Player killer) {
		new DetectiveCompass(detective, killer);
	}

	/**
	 * Gets the color of the arena.
	 * 
	 * @param key
	 *            The name of the arena.
	 * @return The color of the arena.
	 */
	public ChatColor getArenaColor(String key) {
		if (this.arenaColors.containsKey(key)) {
			return this.arenaColors.get(key);
		}
		return ChatColor.WHITE;
	}

	/**
	 * Gets an arena's location.
	 * 
	 * @param name
	 *            The name of the arena.
	 * @return The location of the arena.
	 */
	public Location getArenaLocation(String name) {
		if (this.arenaLocations.containsKey(name)) {
			return this.arenaLocations.get(name);
		}
		return this.arenaLocations.get("default");
	}

	/**
	 * Gets the map of all arena locations.
	 * 
	 * @return The map of all arena locations.
	 */
	public ConcurrentHashMap<String, Location> getArenaLocations() {
		return this.arenaLocations;
	}

	/**
	 * Gets the lore of an arena.
	 * 
	 * @param key
	 *            The name of the arena.
	 * @return The lore.
	 */
	public List<String> getArenaLore(String key) {
		if (this.arenaLores.containsKey(key)) {
			return this.arenaLores.get(key);
		}
		List<String> lore = new ArrayList<>();
		lore.add("There was an error in retrieving what should be written here.");
		return lore;
	}

	/**
	 * Gets the arena which is currently in use.
	 * 
	 * @return The location of the arena.
	 */
	public Location getCurrentArenaLocation() {
		return this.arenaLocation;
	}

	/**
	 * Gets the state of the game: OFF, GAME_PREPARING or GAME_RUNNING.
	 * 
	 * @return The state of the game.
	 */
	public GameState getGameStatus() {
		return this.state;
	}

	/**
	 * Gets the keys of the 4 loaded arenas.
	 * 
	 * @return
	 */
	public List<String> getLoadedArenaKeys() {
		return this.loadedArenaKeys;
	}

	/**
	 * Gets the lobby location.
	 * 
	 * @return The location of the lobby.
	 */
	public Location getLobbyLocation() {
		return this.lobbyLocation;
	}

	/**
	 * Called every tick, this checks for any players who are not in the game
	 * and have permission to spectate, then puts them in spectator mode.
	 */
	private void handleSpectators() {
		if (!isGameRunning()) {
			return;
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.isDead()) {
				continue;
			}
			TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(player);
			if (Tplayer.getTeam() == PlayerTeam.NONE) {
				if (!player.getAllowFlight() && Tplayer.canSpectate()) {
					player.setAllowFlight(true);
					player.closeInventory();
					Tools.clearInventory(player);
					Tplayer.resetPlayer();
					TTTPlayer.allRegisterPlayer(Tplayer);
					Tplayer.registerAllPlayers();
					player.sendMessage(FileManager.spectatorColor
							+ "You are now spectating.");
					BarAPI.setMessage(player, FileManager.spectatorColor
							+ "You are spectating.");
					teleportPlayer(player, this.arenaLocation);
				}
			}
		}
	}

	/**
	 * Returns true if the game state is GAME_RUNNING.
	 * 
	 * @return True if the game is running.
	 */
	public boolean isGameRunning() {
		return this.state == GameState.GAME_RUNNING;
	}

	/**
	 * Forces the sidebar scoreboard of the server to clear.
	 */
	private void killScoreboard() {
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		board.clearSlot(DisplaySlot.SIDEBAR);
	}

	public void load4Arenas() {
		Set<String> set = getArenaLocations().keySet();
		List<String> list = new ArrayList<>(set);
		this.loadedArenaKeys.clear();
		for (int i = 0; i < 4; i++) {
			String arena = MathUtils.randomChoiceFromCollection(list);
			list.remove(arena);
			this.loadedArenaKeys.add(arena);
		}
	}

	/**
	 * Forces each player to be able to see the other.
	 */
	public void makeAllVisible() {
		Player[] players = Bukkit.getOnlinePlayers();
		for (Player player1 : players) {
			for (Player player2 : players) {
				if (!player1.canSee(player2)) {
					TTTPlayer.showPlayer(player2, player1);
				}
			}
		}
	}

	public void reloadHologram() {
		destroyHologram();
		showHologram();
	}

	/**
	 * The heart of the thread, this checks all game mechanics which require
	 * timing.
	 */
	@Override
	public void run() {
		this.time = System.currentTimeMillis();
		this.plugin.chestHandler.handleChests();
		Claymore.handleClaymores();

		switch (this.state) {
		case OFF:
			Player[] players = this.server.getOnlinePlayers();
			if (players.length >= this.playerThreshold) {
				startPreparations();
			}

			break;
		case GAME_PREPARING:
			long remainingtime = (this.starttime + preptime - this.time) / 1000L;
			TTTPlayer.setAllLevel((int) remainingtime);
			if (remainingtime <= 0) {
				startGame();
				break;
			}
			if ((remainingtime - 1) / 10 != this.lastannouncetime) {
				this.server.broadcastMessage(ChatColor.LIGHT_PURPLE
						+ "The game begins in " + remainingtime + " seconds!");
				this.lastannouncetime = (remainingtime - 1) / 10;
			}
			break;
		case GAME_RUNNING:
			DetectiveCompass.progressAll();
			handleSpectators();
			break;
		}

	}

	/**
	 * Sets the arena location and saves all the locations in memory.
	 * 
	 * @param name
	 *            The name of the arena.
	 * @param location
	 *            Its location.
	 */
	public void setArenaLocation(String name, Location location) {
		this.arenaLocations.put(name, location);
		this.plugin.fileManager.setArenaLocations(this.arenaLocations);
	}

	/**
	 * Sets the lobby location and saves the locations in memory.
	 * 
	 * @param location
	 *            The lobby location.
	 */
	public void setLobbyLocation(Location location) {
		this.lobbyLocation = location;
		this.plugin.fileManager.setLobbyLocation(location);

	}

	/**
	 * Displays the holograms.
	 */
	public void showHologram() {
		Location location = this.lobbyLocation.clone();
		location.add(-1, 1.5, 7);
		this.hologram.show(location);

		Location vipLocation = this.lobbyLocation.clone();
		vipLocation.add(-10, 1.5, 15);
		this.vipHologram.show(vipLocation);
	}

	/**
	 * Attempts to start the game. The game will fail to start if too few
	 * players choose to play.
	 */
	private void startGame() {
		this.arenaLocation = TTTPlayer.getWinningLocation();
		this.state = GameState.GAME_RUNNING;
		TTTPlayer.reset();
		makeAllVisible();

		if (!this.plugin.teamHandler.initializeTeams()) {
			endGame(true);
			Bukkit.broadcastMessage(ChatColor.RED
					+ "The game cannot start because either there are no longer enough players, or too few chose not to spectate.");
			return;
		}

		clearInventories();

		for (Player p : Bukkit.getOnlinePlayers()) {
			TTTPlayer player = TTTPlayer.getTTTPlayer(p);
			PlayerTeam team = player.getTeam();

			if (p == null) {
				continue;
			}

			String message;
			switch (team) {
			case INNOCENT:
				message = FileManager.innocentColor + "You are an INNOCENT!";
				player.sendMessage(message);
				BarAPI.setMessage(p, message);
				player.showKarma();
				break;
			case DETECTIVE:
				message = FileManager.detectiveColor + "You are a DETECTIVE!";
				player.sendMessage(message);
				BarAPI.setMessage(p, message);
				player.showKarma();
				p.getInventory().setItem(8, new ItemStack(Material.COMPASS, 1));
				break;
			case TRAITOR:
				message = FileManager.traitorColor + "You are a TRAITOR!";
				player.sendMessage(message);
				BarAPI.setMessage(p, message);
				player.showKarma();
				break;
			default:
				continue;
			}

			teleportPlayer(p, this.arenaLocation);
			p.setGameMode(GameMode.ADVENTURE);
			p.setHealth(20D);
			Inventory inventory = p.getInventory();
			inventory.setItem(0, new ItemStack(Material.STONE_SWORD, 1));
			inventory.setItem(1, new ItemStack(Material.BOW, 1));
			inventory.setItem(4, new ItemStack(Material.ARROW, 40));
		}

		this.server.broadcastMessage(ChatColor.GREEN + "The game has begun!");
		TTTPlayer.showAllGameScoreboards();
		TTTPlayer.giveChatItemsToAll();

	}

	/**
	 * Begins the preparation stage of the game, where players can vote on a
	 * map, and if they have permission, can choose to spectate instead.
	 */
	public void startPreparations() {
		makeAllVisible();
		load4Arenas();
		TTTPlayer.resetScoreboards();
		if (this.arenaLocations.isEmpty()) {
			Tools.verbose("Game cannot start - there is no arena location created.");
			return;
		}
		this.state = GameState.GAME_PREPARING;
		this.starttime = this.time;

		for (Player player : this.server.getOnlinePlayers()) {
			teleportPlayer(player, this.lobbyLocation);
		}

		TTTPlayer.showAllPrepScoreboards();
		TTTPlayer.installAllVoteTools();
	}

	/**
	 * Teleports a player to a location, but in a fitting random spot in a
	 * radius around that location.
	 * 
	 * @param player
	 *            The player to teleport.
	 * @param location
	 *            The desired location.
	 */
	private void teleportPlayer(Player player, Location location) {
		int numberOfTries = 5;
		int i = 0;
		Location loc;
		while (true) {
			double r = this.random.nextDouble() * this.radius;
			double theta = this.random.nextDouble() * 2 * Math.PI;

			double x = r * Math.cos(theta);
			double y = r * Math.sin(theta);

			loc = location.clone().add(x, 0, y);

			Block block = Tools.getFloor(loc, (int) this.radius);
			if (block == null) {
				if (i < numberOfTries) {
					i++;
				} else {
					break;
				}
			} else {
				loc = block.getLocation().add(.5, 0, .5);
				break;
			}

		}
		player.teleport(loc);

	}

}
