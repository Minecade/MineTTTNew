package src.main.java.de.orion304.ttt.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Server;
import org.bukkit.entity.Player;

public class Teams {

	// Enum which contains the values for distinguishing between the teams. None
	// should only be used on players who are not involved in the game - most
	// likely players who joined after the game starts.
	public static enum Team {

		INNOCENT, DETECTIVE, TRAITOR, NONE;

	}

	// Where we store player names and their corresponding team.
	private Set<String> traitors = new HashSet<>();
	private Set<String> detectives = new HashSet<>();
	private Set<String> innocents = new HashSet<>();

	// The percent of total players who are traitors, and the percent who are
	// detectives. The remaining will be innocents.
	private static final double percentTraitors = .25;
	private static final double percentDetectives = .125;

	// Instance variables for later processes.
	private MineTTTNew plugin;
	private Server server;
	private Random random;

	// Constructor. Creates an object which handles players, their teams, and
	// their initiliazation of teams.
	public Teams(MineTTTNew instance) {
		plugin = instance;
		server = plugin.server;
		random = new Random();
	}

	// Method which randomly initializes teams, making sure that the proper
	// number of detectives and traitors are filled, then making the remaining
	// online players innocents.
	public void initializeTeams() {
		ArrayList<Player> players = new ArrayList<Player>(Arrays.asList(server
				.getOnlinePlayers()));

		int numberOfPlayers = players.size();

		int numberOfTraitors = (int) (percentTraitors * numberOfPlayers);
		int numberOfDetectives = (int) (percentDetectives * numberOfPlayers);

		int traitorCount = 0, detectiveCount = 0;

		while (traitorCount < numberOfTraitors) {
			int i = random.nextInt(players.size());
			Player player = players.get(i);
			players.remove(i);
			setTeam(player, Team.TRAITOR);
			traitorCount++;
		}

		while (detectiveCount < numberOfDetectives) {
			int i = random.nextInt(players.size());
			Player player = players.get(i);
			players.remove(i);
			setTeam(player, Team.DETECTIVE);
			detectiveCount++;
		}

		for (Player player : players) {
			setTeam(player, Team.INNOCENT);
		}

	}

	// It is more efficient to store players by their name instead of by their
	// player object, but is more useful in many situations to interact with
	// their player objects. This method does the redirection for you.
	public void setTeam(Player player, Team team) {
		setTeam(player.getName(), team);
	}

	// Sets the player to a specific team. To be certain that no player is ever
	// on more than one team, it also removes them from the remaining teams.
	public void setTeam(String playername, Team team) {
		switch (team) {
		case TRAITOR:
			detectives.remove(playername);
			innocents.remove(playername);
			traitors.add(playername);
			break;
		case INNOCENT:
			detectives.remove(playername);
			innocents.add(playername);
			traitors.remove(playername);
			break;
		case DETECTIVE:
			detectives.add(playername);
			innocents.remove(playername);
			traitors.remove(playername);
			break;
		case NONE:
			detectives.remove(playername);
			innocents.remove(playername);
			traitors.remove(playername);
			break;
		}
	}

	// Again a simple overload.
	public Team getTeam(Player player) {
		return getTeam(player.getName());
	}

	// Returns the team of the player. If the player is on no team, return NONE.
	public Team getTeam(String playername) {
		if (traitors.contains(playername))
			return Team.TRAITOR;
		if (detectives.contains(playername))
			return Team.DETECTIVE;
		if (innocents.contains(playername))
			return Team.INNOCENT;
		return Team.NONE;
	}

}
