package src.main.java.de.orion304.ttt.main;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import src.main.java.de.orion304.ttt.players.PlayerTeam;

public class MainThread implements Runnable {

	private static final long preptime = 60000L;

	// Variables for use in processing
	private MineTTT plugin;
	private Server server;

	// Initialization variables
	private int playerThreshold = 24;
	private Location teleportLocation = new Location(null, 0, 0, 0);
	private double radius = 6;
	private Random random;

	private GameState state = GameState.OFF;
	private long time, starttime;

	public static enum GameState {

		OFF, GAME_RUNNING, GAME_PREPARING;

	}

	public MainThread(MineTTT instance) {
		plugin = instance;
		server = plugin.server;
		random = new Random();
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
			if (time >= starttime + preptime) {
				startGame();
			}
			break;
		case GAME_RUNNING:
			break;
		}

	}

	private void startPreparations() {
		state = GameState.GAME_PREPARING;
		starttime = time;
	}

	private void startGame() {
		state = GameState.GAME_RUNNING;

		plugin.teamHandler.initializeTeams();

		for (Player player : server.getOnlinePlayers()) {
			PlayerTeam team = plugin.teamHandler.getTeam(player);

			switch (team) {
			case INNOCENT:
				player.sendMessage("You are an INNOCENT!");
				break;
			case DETECTIVE:
				player.sendMessage("You are a DETECTIVE!");
				break;
			case TRAITOR:
				player.sendMessage("You are a TRAITOR!");
				break;
			default:
				return;
			}

			teleportPlayer(player);
		}

	}

	private void teleportPlayer(Player player) {
		double r = random.nextDouble() * radius;
		double theta = random.nextDouble() * 2 * Math.PI;

		double x = r * Math.cos(theta);
		double y = r * Math.sin(theta);

		player.teleport(teleportLocation.clone().add(x, 0, y));

	}

	public boolean isGameRunning() {
		return state == GameState.GAME_RUNNING;
	}

	public void setArenaLocation(Location location) {
		teleportLocation = location;
	}

}
