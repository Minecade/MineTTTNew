package src.main.java.de.orion304.ttt.main;

import java.util.Random;
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

import src.main.java.de.orion304.ttt.players.DetectiveCompass;
import src.main.java.de.orion304.ttt.players.PlayerTeam;
import src.main.java.de.orion304.ttt.players.TTTPlayer;

public class MainThread implements Runnable {

	private static long preptime = FileManager.preparationTime;

	// Variables for use in processing
	private final MineTTT plugin;
	private final Server server;

	// Initialization variables
	private int playerThreshold = FileManager.minimumNumberOfPlayers;
	private ConcurrentHashMap<String, Location> arenaLocations = new ConcurrentHashMap<>();
	private Location arenaLocation, lobbyLocation;
	private final double radius = 4.5;
	private final Random random;

	private GameState state = GameState.OFF;
	private long time, starttime, lastannouncetime = (preptime - 1000) / 10000;

	public MainThread(MineTTT instance) {
		this.plugin = instance;
		this.server = Bukkit.getServer();
		this.random = new Random();

		preptime = FileManager.preparationTime;
		this.lastannouncetime = (preptime - 1000) / 10000;
		this.playerThreshold = FileManager.minimumNumberOfPlayers;

		this.arenaLocations = this.plugin.fileManager.getArenaLocations();
		this.lobbyLocation = this.plugin.fileManager.getLobbyLocation();

		if (this.lobbyLocation == null) {
			this.lobbyLocation = this.server.getWorlds().get(0)
					.getSpawnLocation();
		}
	}

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

	private void clearInventories() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(player);
			if (Tplayer.getTeam() != PlayerTeam.NONE) {
				player.getInventory().clear();
			}
		}
	}

	public void endGame(boolean forced) {
		this.state = GameState.OFF;
		this.plugin.playerListener.resetDeadPlayers();
		makeAllVisible();
		killScoreboard();
		boolean traitorsWon = true;
		if (TTTPlayer.getNumberOfTraitors() == 0) {
			traitorsWon = false;
		}

		String endMessage = "The game has ended! ";
		if (traitorsWon) {
			endMessage = FileManager.traitorColor + endMessage;
			endMessage += "The traitors were victorious!";
		} else {
			endMessage = FileManager.detectiveColor + endMessage;
			endMessage += " The detectives were victorious!";
		}

		if (forced) {
			endMessage = ChatColor.RED + "The game was ended by an admin.";
		}
		TTTPlayer.distributeCoinsToAll();
		TempBlock.revertAll();
		clearDrops();
		clearInventories();
		TTTPlayer.dealKarma();
		TTTPlayer.reset();
		Bukkit.broadcastMessage(endMessage);
		TTTPlayer.showAllPreGameScoreboards();

		for (Player player : Bukkit.getOnlinePlayers()) {
			teleportPlayer(player, this.lobbyLocation);
			player.setGameMode(GameMode.ADVENTURE);
		}

	}

	public void findKiller(Player detective, Player killer) {
		new DetectiveCompass(detective, killer);
	}

	public Location getArenaLocation(String name) {
		if (this.arenaLocations.containsKey(name)) {
			return this.arenaLocations.get(name);
		}
		return this.arenaLocations.get("default");
	}

	public ConcurrentHashMap<String, Location> getArenaLocations() {
		return this.arenaLocations;
	}

	public Location getCurrentArenaLocation() {
		return this.arenaLocation;
	}

	public GameState getGameStatus() {
		return this.state;
	}

	public Location getLobbyLocation() {
		return this.lobbyLocation;
	}

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
				if (player.getGameMode() != GameMode.CREATIVE
						&& Tplayer.canSpectate()) {
					player.setGameMode(GameMode.CREATIVE);
					player.closeInventory();
					player.getInventory().clear();
					Tplayer.resetPlayer();
					TTTPlayer.allRegisterPlayer(Tplayer);
					Tplayer.registerAllPlayers();
					player.sendMessage(FileManager.spectatorColor
							+ "You are now spectating.");
					teleportPlayer(player, this.arenaLocation);
				}
			}
		}
	}

	public boolean isGameRunning() {
		return this.state == GameState.GAME_RUNNING;
	}

	private void killScoreboard() {
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		board.clearSlot(DisplaySlot.SIDEBAR);
	}

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

	// The heart of the thread of the program
	@Override
	public void run() {
		this.time = System.currentTimeMillis();

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

	public void setArenaLocation(String name, Location location) {
		this.arenaLocations.put(name, location);
		this.plugin.fileManager.setArenaLocations(this.arenaLocations);
	}

	public void setLobbyLocation(Location location) {
		this.lobbyLocation = location;
		this.plugin.fileManager.setLobbyLocation(location);

	}

	private void startGame() {
		this.arenaLocation = TTTPlayer.getWinningLocation();
		this.state = GameState.GAME_RUNNING;
		TTTPlayer.reset();
		makeAllVisible();

		if (!this.plugin.teamHandler.initializeTeams()) {
			endGame(true);
			return;
		}

		clearInventories();

		for (Player p : Bukkit.getOnlinePlayers()) {
			TTTPlayer player = TTTPlayer.getTTTPlayer(p);
			PlayerTeam team = player.getTeam();

			if (p == null) {
				continue;
			}

			switch (team) {
			case INNOCENT:
				player.sendMessage(FileManager.innocentColor
						+ "You are an INNOCENT!");
				player.showKarma();
				break;
			case DETECTIVE:
				player.sendMessage(FileManager.detectiveColor
						+ "You are a DETECTIVE!");
				player.showKarma();
				p.getInventory().setItem(8, new ItemStack(Material.COMPASS, 1));
				break;
			case TRAITOR:
				player.sendMessage(FileManager.traitorColor
						+ "You are a TRAITOR!");
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

	public void startPreparations() {
		makeAllVisible();
		TTTPlayer.resetScoreboards();
		if (this.arenaLocations.isEmpty()) {
			Tools.verbose("Game cannot start - there is no arena location created.");
			return;
		}
		this.state = GameState.GAME_PREPARING;
		this.starttime = this.time;
		long duration = preptime / 1000L;
		this.server.broadcastMessage(ChatColor.LIGHT_PURPLE
				+ "The game will begin in " + duration + " seconds!");

		for (Player player : this.server.getOnlinePlayers()) {
			teleportPlayer(player, this.lobbyLocation);
		}

		TTTPlayer.showAllPrepScoreboards();
		TTTPlayer.installAllVoteTools();
	}

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
