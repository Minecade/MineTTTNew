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
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import src.main.java.de.orion304.ttt.players.DetectiveCompass;
import src.main.java.de.orion304.ttt.players.PlayerTeam;
import src.main.java.de.orion304.ttt.players.TTTPlayer;

public class MainThread implements Runnable {

	private static long preptime = FileManager.preparationTime;

	// Variables for use in processing
	private MineTTT plugin;
	private Server server;

	// Initialization variables
	private int playerThreshold = FileManager.minimumNumberOfPlayers;
	private ConcurrentHashMap<String, Location> arenaLocations = new ConcurrentHashMap<>();
	private Location arenaLocation, lobbyLocation;
	private double radius = 4.5;
	private Random random;

	private GameState state = GameState.OFF;
	private long time, starttime, lastannouncetime = (preptime - 1000) / 10000;

	public MainThread(MineTTT instance) {
		plugin = instance;
		server = Bukkit.getServer();
		random = new Random();

		preptime = FileManager.preparationTime;
		lastannouncetime = (preptime - 1000) / 10000;
		playerThreshold = FileManager.minimumNumberOfPlayers;

		arenaLocations = plugin.fileManager.getArenaLocations();
		lobbyLocation = plugin.fileManager.getLobbyLocation();

		if (lobbyLocation == null)
			lobbyLocation = server.getWorlds().get(0).getSpawnLocation();
	}

	// The heart of the thread of the program
	@Override
	public void run() {
		time = System.currentTimeMillis();

		switch (state) {
		case OFF:
			Player[] players = server.getOnlinePlayers();
			if (players.length >= playerThreshold) {
				startPreparations();
			}

			break;
		case GAME_PREPARING:
			long remainingtime = (starttime + preptime - time) / 1000L;
			TTTPlayer.setAllLevel((int) remainingtime);
			if (remainingtime <= 0) {
				startGame();
				break;
			}
			if ((remainingtime - 1) / 10 != lastannouncetime) {
				server.broadcastMessage(ChatColor.LIGHT_PURPLE
						+ "The game begins in " + remainingtime + " seconds!");
				lastannouncetime = (remainingtime - 1) / 10;
			}
			break;
		case GAME_RUNNING:
			DetectiveCompass.progressAll();
			handleSpectators();
			break;
		}

	}

	private void killScoreboard() {
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		board.clearSlot(DisplaySlot.SIDEBAR);
	}

	private void handleSpectators() {
		if (!isGameRunning())
			return;
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.isDead())
				continue;
			TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(player);
			if (Tplayer.getTeam() == PlayerTeam.NONE) {
				if (player.getGameMode() != GameMode.CREATIVE) {
					player.setGameMode(GameMode.CREATIVE);
					player.closeInventory();
					player.getInventory().clear();
					Tplayer.resetPlayer();
					TTTPlayer.allRegisterPlayer(Tplayer);
					Tplayer.registerAllPlayers();
					player.sendMessage(FileManager.spectatorColor
							+ "You are now spectating.");
					teleportPlayer(player, arenaLocation);
				}
			}
		}
	}

	public GameState getGameStatus() {
		return state;
	}

	public void endGame(boolean forced) {
		state = GameState.OFF;
		makeAllVisible();
		killScoreboard();
		boolean traitorsWon = true;
		if (TTTPlayer.getNumberOfTraitors() == 0)
			traitorsWon = false;

		String endMessage = "The game has ended! ";
		if (traitorsWon) {
			endMessage = FileManager.traitorColor + endMessage;
			endMessage += "The traitors were victorious!";
		} else {
			endMessage = FileManager.detectiveColor + endMessage;
			endMessage += " The detectives were victorious!";
		}

		if (forced)
			endMessage = ChatColor.RED + "The game was ended by an admin.";
		TempBlock.revertAll();
		clearDrops();
		clearInventories();
		TTTPlayer.dealKarma();
		TTTPlayer.reset();
		Bukkit.broadcastMessage(endMessage);
		TTTPlayer.showAllPreGameScoreboards();

		for (Player player : Bukkit.getOnlinePlayers()) {
			teleportPlayer(player, lobbyLocation);
			player.setGameMode(GameMode.ADVENTURE);
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

	private void clearDrops() {
		if (arenaLocation == null)
			return;
		for (Entity entity : arenaLocation.getWorld().getEntities()) {
			if (entity instanceof LivingEntity)
				continue;
			entity.remove();
		}
	}

	public void startPreparations() {
		makeAllVisible();
		TTTPlayer.resetScoreboards();
		if (arenaLocations.isEmpty()) {
			Tools.verbose("Game cannot start - there is no arena location created.");
			return;
		}
		state = GameState.GAME_PREPARING;
		starttime = time;
		long duration = preptime / 1000L;
		server.broadcastMessage(ChatColor.LIGHT_PURPLE
				+ "The game will begin in " + duration + " seconds!");

		for (Player player : server.getOnlinePlayers()) {
			teleportPlayer(player, lobbyLocation);
		}

		TTTPlayer.showAllPrepScoreboards();
		TTTPlayer.installAllVoteTools();
	}

	private void startGame() {
		arenaLocation = TTTPlayer.getWinningLocation();
		state = GameState.GAME_RUNNING;
		TTTPlayer.reset();
		makeAllVisible();

		if (!plugin.teamHandler.initializeTeams()) {
			endGame(true);
			return;
		}

		clearInventories();

		for (Player p : Bukkit.getOnlinePlayers()) {
			TTTPlayer player = TTTPlayer.getTTTPlayer(p);
			PlayerTeam team = player.getTeam();

			if (p == null)
				continue;

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

			teleportPlayer(p, arenaLocation);
			p.setGameMode(GameMode.ADVENTURE);
			p.setHealth(20D);
			p.getInventory().setItem(0, new ItemStack(Material.STONE_SWORD, 1));
		}

		server.broadcastMessage(ChatColor.GREEN + "The game has begun!");
		TTTPlayer.showAllGameScoreboards();
		TTTPlayer.giveChatItemsToAll();

	}

	private void teleportPlayer(Player player, Location location) {
		int numberOfTries = 5;
		int i = 0;
		Location loc;
		while (true) {
			double r = random.nextDouble() * radius;
			double theta = random.nextDouble() * 2 * Math.PI;

			double x = r * Math.cos(theta);
			double y = r * Math.sin(theta);

			loc = location.clone().add(x, 0, y);

			Block block = Tools.getFloor(loc, (int) radius);
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

	public boolean isGameRunning() {
		return state == GameState.GAME_RUNNING;
	}

	public void setArenaLocation(String name, Location location) {
		arenaLocations.put(name, location);
		plugin.fileManager.setArenaLocations(arenaLocations);
	}

	public void setLobbyLocation(Location location) {
		lobbyLocation = location;
		plugin.fileManager.setLobbyLocation(location);

	}

	public Location getLobbyLocation() {
		return lobbyLocation;
	}

	public Location getArenaLocation(String name) {
		if (arenaLocations.containsKey(name))
			return arenaLocations.get(name);
		return arenaLocations.get("default");
	}

	public ConcurrentHashMap<String, Location> getArenaLocations() {
		return arenaLocations;
	}

	public Location getCurrentArenaLocation() {
		return arenaLocation;
	}

	public void findKiller(Player detective, Player killer) {
		new DetectiveCompass(detective, killer);
	}

	public void makeAllVisible() {
		Player[] players = Bukkit.getOnlinePlayers();
		for (Player player1 : players) {
			for (Player player2 : players) {
				player1.showPlayer(player2);
			}
		}
	}

}
