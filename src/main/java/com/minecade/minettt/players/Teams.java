package com.minecade.minettt.players;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.minecade.minettt.main.MineTTT;
import com.minecade.minettt.main.Tools;

public class Teams {

	// Where we store player names and their corresponding team.
	// private Set<String> traitors = new HashSet<>();
	// private Set<String> detectives = new HashSet<>();
	// private Set<String> innocents = new HashSet<>();

	// The percent of total players who are traitors, and the percent who are
	// detectives. The remaining will be innocents.
	private static final double percentTraitors = .25;
	private static final double percentDetectives = .125;

	// Instance variables for later processes.
	private final MineTTT plugin;
	private final Server server;
	private final Random random;

	/**
	 * Creates a new Teams instance. Teams now only handles setting teams and
	 * initializing them.
	 * 
	 * @param instance
	 *            The MineTTT instance.
	 */
	public Teams(MineTTT instance) {
		this.plugin = instance;
		this.server = Bukkit.getServer();
		this.random = new Random();
	}

	/**
	 * Initializes the teams, choosing random players to be traitors, detectives
	 * and innocents. Returns true if there are enough players to play the game,
	 * false otherwise.
	 * 
	 * @return True if there are enough players to play.
	 */
	public boolean initializeTeams() {
		Set<Player> players = new HashSet<Player>(Arrays.asList(this.server
				.getOnlinePlayers()));

		players.removeAll(TTTPlayer.getSpectators());

		Set<Player> desiredDetectives = TTTPlayer.getDesiredDetectives();
		Set<Player> desiredTraitors = TTTPlayer.getDesiredTraitors();
		Set<Player> desiredInnocents = TTTPlayer.getDesiredInnocents();

		String string = "";
		for (Player player : desiredDetectives) {
			string += player.getName() + ", ";
		}
		if (string.length() > 1) {
			string = string.substring(0, string.length() - 2);
		}
		Tools.verbose("Detectives: " + string);

		string = "";
		for (Player player : desiredTraitors) {
			string += player.getName() + ", ";
		}
		if (string.length() > 1) {
			string = string.substring(0, string.length() - 2);
		}
		Tools.verbose("Traitors: " + string);

		string = "";
		for (Player player : desiredInnocents) {
			string += player.getName() + ", ";
		}
		if (string.length() > 1) {
			string = string.substring(0, string.length() - 2);
		}
		Tools.verbose("Innocents: " + string);

		int numberOfPlayers = players.size();

		if (numberOfPlayers < 2) {
			Tools.verbose("There are not enough players to begin the game!");
			return false;
		}

		int numberOfTraitors = (int) (percentTraitors * numberOfPlayers);
		int numberOfDetectives = (int) (percentDetectives * numberOfPlayers);

		if (numberOfTraitors < 1) {
			numberOfTraitors = 1;
		}
		if (numberOfDetectives < 1) {
			numberOfDetectives = 1;
		}

		int numberOfInnocents = numberOfPlayers - numberOfTraitors
				- numberOfDetectives;

		int traitorCount = 0, detectiveCount = 0, innocentCount = 0;

		Player player;
		players.removeAll(desiredInnocents);
		players.removeAll(desiredDetectives);
		players.removeAll(desiredTraitors);

		while (traitorCount < numberOfTraitors && !desiredTraitors.isEmpty()) {
			// Tools.verbose("Desired traitor size: " + desiredTraitors.size());
			player = Tools.chooseFromSet(desiredTraitors);
			desiredTraitors.remove(player);
			setTeam(player, PlayerTeam.TRAITOR);
			traitorCount++;
		}

		players.addAll(desiredTraitors);

		while (detectiveCount < numberOfDetectives
				&& !desiredDetectives.isEmpty()) {
			// Tools.verbose("Desired detectives size: "
			// + desiredDetectives.size());
			player = Tools.chooseFromSet(desiredDetectives);
			desiredDetectives.remove(player);
			setTeam(player, PlayerTeam.DETECTIVE);
			player.getInventory()
					.setItem(8, new ItemStack(Material.COMPASS, 1));
			detectiveCount++;
		}

		players.addAll(desiredDetectives);

		while (innocentCount < numberOfInnocents && !desiredInnocents.isEmpty()) {
			// Tools.verbose("Desired innocents size: " +
			// desiredInnocents.size());
			player = Tools.chooseFromSet(desiredInnocents);
			desiredInnocents.remove(player);
			setTeam(player, PlayerTeam.INNOCENT);
			innocentCount++;
		}

		players.addAll(desiredInnocents);

		while (traitorCount < numberOfTraitors) {
			player = Tools.chooseFromSet(players);
			players.remove(player);
			setTeam(player, PlayerTeam.TRAITOR);
			traitorCount++;
		}

		while (detectiveCount < numberOfDetectives) {
			player = Tools.chooseFromSet(players);
			players.remove(player);
			setTeam(player, PlayerTeam.DETECTIVE);
			player.getInventory()
					.setItem(8, new ItemStack(Material.COMPASS, 1));
			detectiveCount++;
		}

		for (Player p : players) {
			setTeam(p, PlayerTeam.INNOCENT);
		}

		TTTPlayer.registerAllScoreboards();
		return true;

	}

	/**
	 * Gets whether the game is over - that is, if the one side no long has
	 * enough players.
	 * 
	 * @return True if the game is over.
	 */
	public boolean isGameOver() {
		if (TTTPlayer.getNumberOfDetectives() == 0
				&& TTTPlayer.getNumberOfInnocents() == 0) {
			return true;
		}

		if (TTTPlayer.getNumberOfTraitors() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Sets the team of a player.
	 * 
	 * @param player
	 *            The player to set.
	 * @param team
	 *            The team to set the player to.
	 */
	public void setTeam(Player player, PlayerTeam team) {
		TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(player);
		Tplayer.setTeam(team);
		Tplayer.logPlayedGame();
	}

}
