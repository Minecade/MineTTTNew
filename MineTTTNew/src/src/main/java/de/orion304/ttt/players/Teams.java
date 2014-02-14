package src.main.java.de.orion304.ttt.players;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import src.main.java.de.orion304.ttt.main.MineTTT;
import src.main.java.de.orion304.ttt.main.Tools;

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
		ArrayList<Player> players = new ArrayList<Player>(
				Arrays.asList(this.server.getOnlinePlayers()));

		players.removeAll(TTTPlayer.getSpectators());

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

		int traitorCount = 0, detectiveCount = 0;

		while (traitorCount < numberOfTraitors) {
			int i = this.random.nextInt(players.size());
			Player player = players.get(i);
			players.remove(i);
			setTeam(player, PlayerTeam.TRAITOR);
			traitorCount++;
		}

		while (detectiveCount < numberOfDetectives) {
			int i = this.random.nextInt(players.size());
			Player player = players.get(i);
			players.remove(i);
			setTeam(player, PlayerTeam.DETECTIVE);
			player.getInventory()
					.setItem(8, new ItemStack(Material.COMPASS, 1));
			detectiveCount++;
		}

		for (Player player : players) {
			setTeam(player, PlayerTeam.INNOCENT);
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
		TTTPlayer.getTTTPlayer(player).setTeam(team);
	}

}
