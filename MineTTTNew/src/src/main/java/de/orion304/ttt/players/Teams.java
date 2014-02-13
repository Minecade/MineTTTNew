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

	// Constructor. Creates an object which handles players, their teams, and
	// their initiliazation of teams.
	public Teams(MineTTT instance) {
		this.plugin = instance;
		this.server = Bukkit.getServer();
		this.random = new Random();
	}

	// Method which randomly initializes teams, making sure that the proper
	// number of detectives and traitors are filled, then making the remaining
	// online players innocents.
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

	public boolean isGameOver() {
		if (TTTPlayer.getNumberOfDetectives() == 0
				&& TTTPlayer.getNumberOfInnocents() == 0) {
			return true;
		}

		if (TTTPlayer.getNumberOfTraitors() == 0) {
			return true;
		}
		// if (detectives.isEmpty() && innocents.isEmpty())
		// return true;
		// if (traitors.isEmpty())
		// return true;
		return false;
	}

	// Sets the player to a specific team. To be certain that no player is ever
	// on more than one team, it also removes them from the remaining teams.
	// public void setTeam(String playername, PlayerTeam team) {
	// switch (team) {
	// case TRAITOR:
	// detectives.remove(playername);
	// innocents.remove(playername);
	// traitors.add(playername);
	// break;
	// case INNOCENT:
	// detectives.remove(playername);
	// innocents.add(playername);
	// traitors.remove(playername);
	// break;
	// case DETECTIVE:
	// detectives.add(playername);
	// innocents.remove(playername);
	// traitors.remove(playername);
	// break;
	// case NONE:
	// detectives.remove(playername);
	// innocents.remove(playername);
	// traitors.remove(playername);
	// break;
	// }
	// }

	// Again a simple overload.
	// public PlayerTeam getTeam(Player player) {
	// return getTeam(player.getName());
	// }

	// Returns the team of the player. If the player is on no team, return NONE.
	// public PlayerTeam getTeam(String playername) {
	// if (traitors.contains(playername))
	// return PlayerTeam.TRAITOR;
	// if (detectives.contains(playername))
	// return PlayerTeam.DETECTIVE;
	// if (innocents.contains(playername))
	// return PlayerTeam.INNOCENT;
	// return PlayerTeam.NONE;
	// }

	// It is more efficient to store players by their name instead of by their
	// player object, but is more useful in many situations to interact with
	// their player objects. This method does the redirection for you.
	public void setTeam(Player player, PlayerTeam team) {
		TTTPlayer.getTTTPlayer(player).setTeam(team);
		// setTeam(player.getName(), team);
	}

}
