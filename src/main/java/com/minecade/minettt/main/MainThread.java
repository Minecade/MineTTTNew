package com.minecade.minettt.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import com.minecade.minettt.players.DeathLocation;
import com.minecade.minettt.players.DetectiveCompass;
import com.minecade.minettt.players.PlayerTeam;
import com.minecade.minettt.players.TTTPlayer;
import org.orion304.enderbar.BarAPI;
import org.orion304.utils.HologramOld;
import org.orion304.utils.Justification;
import org.orion304.utils.MathUtils;

public class MainThread implements Runnable {

	private static long preptime = FileManager.preparationTime + 500L;
	private static final long gracetime = 10 * 1000L;

	// Variables for use in processing
	private final MineTTT plugin;
	private final Server server;

	// Initialization variables
	public int playerThreshold = FileManager.minimumNumberOfPlayers;
	private ConcurrentHashMap<String, Location> arenaLocations = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, List<String>> arenaLores = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, ChatColor> arenaColors = new ConcurrentHashMap<>();
	private final List<String> loadedArenaKeys = new ArrayList<>();
	private String arenaName;
	private Location arenaLocation, lobbyLocation;
	private final double radius = 34.5;
	private final Random random;

	private GameState state = GameState.OFF;
	private long time, starttime, lastannouncetime = 0, celebrationStartTime;
	private long fireworkTick = 0;
	private boolean traitorsWon = false;

	private boolean ended = false;
	private final boolean useHolograms = true;

	// private final HologramOld hologram, vipHologram, siteHologram,
	// nuggetHologram;

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

		for (Player player : Bukkit.getOnlinePlayers()) {
			showHologram(player);
		}
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
		if (this.useHolograms) {
			// this.hologram.destroy();
			// this.vipHologram.destroy();
			// this.siteHologram.destroy();
			// this.nuggetHologram.destroy();
		}
	}

	/**
	 * Ends the game.
	 * 
	 * @param forced
	 *            True if the game was forcibly ended, false if the game ended
	 *            by completing its normal course.
	 */
	public void endGame(boolean forced) {
		this.ended = !forced;
		this.arenaName = null;
		this.state = GameState.CELEBRATIONS;
		this.fireworkTick = 0;
		this.celebrationStartTime = System.currentTimeMillis();
		this.plugin.playerListener.resetDeadPlayers();
		makeAllVisible();
		killScoreboard();
		this.traitorsWon = true;
		if (TTTPlayer.getNumberOfTraitors() == 0) {
			this.traitorsWon = false;
		}

		String endMessage = ChatColor.BOLD + "The game has ended! ";
		if (this.traitorsWon) {
			endMessage = FileManager.traitorColor + endMessage;
			endMessage += "The traitors were victorious!";
		} else {
			endMessage = FileManager.detectiveColor + endMessage;
			endMessage += " The detectives were victorious!";
		}

		if (!forced) {
			Bukkit.broadcastMessage(endMessage);
		} else {
			this.celebrationStartTime = 0;
		}
		TTTPlayer.distributeCoinsToAll();
		TempBlock.revertAll();
		DeathLocation.reset();
		clearDrops();
		clearInventories();
		Claymore.reset();

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
	 * Fires a random firework at the location.
	 * 
	 * @param location
	 */
	private void fireFirework(Location location) {
		Firework fw = (Firework) location.getWorld().spawnEntity(location,
				EntityType.FIREWORK);
		FireworkMeta fwm = fw.getFireworkMeta();
		// Our random generator
		Random r = new Random();

		// Get the type
		int rt = r.nextInt(5) + 1;
		Type type = Type.BALL;
		if (rt == 1) {
			type = Type.BALL;
		}
		if (rt == 2) {
			type = Type.BALL_LARGE;
		}
		if (rt == 3) {
			type = Type.BURST;
		}
		if (rt == 4) {
			type = Type.CREEPER;
		}
		if (rt == 5) {
			type = Type.STAR;
		}

		// Get our random colours
		int r1i = r.nextInt(17) + 1;
		int r2i = r.nextInt(17) + 1;
		Color c1 = getColor(r1i);
		Color c2 = getColor(r2i);

		// Create our effect with this
		FireworkEffect effect = FireworkEffect.builder()
				.flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type)
				.trail(r.nextBoolean()).build();

		// Then apply the effect to the meta
		fwm.addEffect(effect);

		// Generate some random power and set it
		int rp = r.nextInt(2) + 1;
		fwm.setPower(rp);

		// Then apply this to our rocket
		fw.setFireworkMeta(fwm);
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

	public String getArenaName() {
		return this.arenaName;
	}

	/**
	 * Returns a color based on the integer parameter
	 * 
	 * @param i
	 *            The integer
	 * @return The chat color
	 */
	private Color getColor(int i) {
		Color c = null;
		if (i == 1) {
			c = Color.AQUA;
		}
		if (i == 2) {
			c = Color.BLACK;
		}
		if (i == 3) {
			c = Color.BLUE;
		}
		if (i == 4) {
			c = Color.FUCHSIA;
		}
		if (i == 5) {
			c = Color.GRAY;
		}
		if (i == 6) {
			c = Color.GREEN;
		}
		if (i == 7) {
			c = Color.LIME;
		}
		if (i == 8) {
			c = Color.MAROON;
		}
		if (i == 9) {
			c = Color.NAVY;
		}
		if (i == 10) {
			c = Color.OLIVE;
		}
		if (i == 11) {
			c = Color.ORANGE;
		}
		if (i == 12) {
			c = Color.PURPLE;
		}
		if (i == 13) {
			c = Color.RED;
		}
		if (i == 14) {
			c = Color.SILVER;
		}
		if (i == 15) {
			c = Color.TEAL;
		}
		if (i == 16) {
			c = Color.WHITE;
		}
		if (i == 17) {
			c = Color.YELLOW;
		}

		return c;
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
	 * Handles the celebrations/fireworks display
	 */
	private void handleCelebrations() {
		long delta = System.currentTimeMillis() - this.celebrationStartTime;

		if (this.celebrationStartTime != 0) {
			handleSpectators();
		}

		if (delta >= 10000L || this.celebrationStartTime == 0) {
			boolean forced = this.celebrationStartTime == 0;
			this.state = GameState.OFF;
			TTTPlayer.dealKarma();
			TTTPlayer.reset();
			TTTPlayer.showAllPreGameScoreboards();
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

			// showHologram();
			this.celebrationStartTime = 0;

			if (!forced) {
				new BukkitRunnable() {
					@Override
					public void run() {
						Bukkit.getServer().shutdown();
					}
				}.runTaskLater(this.plugin, 10 * 20);
			}

		}

		if (delta / 500 > this.fireworkTick) {
			this.fireworkTick = delta / 500;
			for (Player player : Bukkit.getOnlinePlayers()) {
				TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(player);
				if (Tplayer.getTeam() == PlayerTeam.NONE) {
					continue;
				}

				if (Tplayer.getTeam() == PlayerTeam.TRAITOR && this.traitorsWon) {
					fireFirework(player.getLocation());
				} else {
					fireFirework(player.getLocation());
				}
			}
		}

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
					Tplayer.giveSpectatorInventory();
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
		return this.state == GameState.GAME_RUNNING
				|| this.state == GameState.CELEBRATIONS;
	}

	public boolean isOver() {
		return this.ended;
	}

	/**
	 * Forces the sidebar scoreboard of the server to clear.
	 */
	private void killScoreboard() {
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		board.clearSlot(DisplaySlot.SIDEBAR);
	}

	/**
	 * Loads 4 random arenas into memory for voting purposes.
	 */
	public void load4Arenas() {
		TTTPlayer.resetVoteKeys();
		Set<String> set = getArenaLocations().keySet();
		List<String> list = new ArrayList<>(set);
		this.loadedArenaKeys.clear();
		for (int i = 0; i < 4; i++) {
			String arena = MathUtils.randomChoiceFromCollection(list);
			list.remove(arena);
			this.loadedArenaKeys.add(arena);
			TTTPlayer.installVoteKey(arena);
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

	/**
	 * Reloads the holograms.
	 */
	public void reloadHologram() {
		// destroyHologram();
		// showHologram();
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
			if (players.length >= this.playerThreshold && !this.ended) {
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
		case CELEBRATIONS:
			handleCelebrations();
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
	public void showHologram(Player player) {
		if (this.useHolograms) {
			String traitor = ChatColor.RED.toString()
					+ ChatColor.ITALIC.toString() + ChatColor.BOLD + "Traitor";
			String innocent = ChatColor.AQUA.toString()
					+ ChatColor.ITALIC.toString() + ChatColor.BOLD + "Innocent";
			String detective = ChatColor.DARK_AQUA.toString()
					+ ChatColor.ITALIC.toString() + ChatColor.BOLD
					+ "Detective";
			String reset = ChatColor.RESET.toString()
					+ ChatColor.BOLD.toString();

			HologramOld hologram = new HologramOld(
					this.plugin,
					ChatColor.GOLD.toString() + ChatColor.BOLD
							+ ChatColor.UNDERLINE + "WELCOME TO MINETTT, "
							+ player.getName() + "!",
					" ",
					reset
							+ "MineTTT is a complex game about betrayal and trust!",
					reset + "Be a " + detective + reset + " to hunt down "
							+ traitor + "s!", reset + "Be a " + traitor + reset
							+ " to eliminate " + innocent + "s" + reset
							+ " and " + detective + "s!", reset + "Or be an "
							+ innocent + reset
							+ " to find, hide from, and eliminate " + traitor
							+ "s!");
			HologramOld vipHologram = new HologramOld(this.plugin,
					Justification.LEFT, ChatColor.AQUA + "VIP PERKS:",
					"\u2022Two map votes", "\u2022Double the coins",
					"\u2022Join full servers", "\u2022Choose your role",
					"\u2022Choose to spectate");
			HologramOld siteHologram = new HologramOld(this.plugin,
					Justification.LEFT, "minecade.com", "twitter.com/Minecade",
					"fb.me/LegendaryNetwork");
			HologramOld nuggetHologram = new HologramOld(this.plugin,
					Justification.LEFT, ChatColor.GOLD + "Golden Nugget Info:",
					"Earn gold nuggets in game by killing",
					"  appropriate players.",
					"Click on a gold nugget as a traitor",
					"  to open the shop and buy", "  unique, powerful items.",
					"Nuggets not used at the end of the",
					"  game are turned into coins.");

			Location location = this.lobbyLocation.clone();
			location.add(-1, 1.5, 7);
			hologram.show(location, player);

			Location vipLocation = this.lobbyLocation.clone();
			vipLocation.add(-10, 1.5, 15);
			vipHologram.show(vipLocation, player);

			Location siteLocation = this.lobbyLocation.clone();
			siteLocation.add(-10, 1.5, -9);
			siteHologram.show(siteLocation, player);

			Location nuggetLocation = this.lobbyLocation.clone();
			nuggetLocation.add(10, 1.5, -7);
			nuggetHologram.show(nuggetLocation, player);
		}
	}

	/**
	 * Attempts to start the game. The game will fail to start if too few
	 * players choose to play.
	 */
	private void startGame() {
		this.arenaName = TTTPlayer.getWinningLocation();
		this.arenaLocation = getArenaLocation(this.arenaName);
		this.state = GameState.GAME_RUNNING;
		TTTPlayer.reset();
		makeAllVisible();
		destroyHologram();

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

		// if (this.state == GameState.OFF
		// || this.state == GameState.GAME_PREPARING) {
		// reloadHologram();
		// }

	}

}
