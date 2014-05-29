package src.main.java.de.orion304.ttt.main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import src.main.java.de.orion304.ttt.players.PlayerTeam;
import src.main.java.de.orion304.ttt.players.TTTPlayer;
import src.main.java.org.orion304.SQLHandler;

public class LocalDatabase {

	private final MineTTT plugin;
	final SQLHandler handler;

	private final String database;

	public LocalDatabase(MineTTT instance, String host, int port,
			String database, String username, String password) {
		this.plugin = instance;
		this.database = database;
		this.handler = new SQLHandler(host, port, database, username, password);
		ensureFormat();
	}

	public void ensureFormat() {
		try {
			PreparedStatement statement = this.handler
					.getStatement("SHOW TABLES LIKE ?;");
			statement.setString(1, "players");
			ResultSet set = this.handler.get(statement);
			if (!set.first()) {
				statement = this.handler
						.getStatement("CREATE TABLE players(username VARCHAR(16), karma INT, banDate BIGINT, banDuration BIGINT, rank VARCHAR(16), traitorKillsAsInnocent INT, traitorKillsAsDetective INT, detectiveKills INT, innocentKills INT, gamesPlayed INT);");
				this.handler.update(statement);
			}
			this.handler
					.update("ALTER TABLE players ALTER karma SET DEFAULT 1000;");
			this.handler
					.update("ALTER TABLE players ALTER banDate SET DEFAULT 0;");
			this.handler
					.update("ALTER TABLE players ALTER banDuration SET DEFAULT 0;");
			this.handler
					.update("ALTER TABLE players ALTER gamesPlayed SET DEFAULT 0;");
			this.handler
					.update("ALTER TABLE players ALTER traitorKillsAsInnocent SET DEFAULT 0;");
			this.handler
					.update("ALTER TABLE players ALTER traitorKillsAsDetective SET DEFAULT 0;");
			this.handler
					.update("ALTER TABLE players ALTER innocentKills SET DEFAULT 0;");
			this.handler
					.update("ALTER TABLE players ALTER detectiveKills SET DEFAULT 0;");
			statement = this.handler
					.getStatement("ALTER TABLE players ALTER rank SET DEFAULT ?;");
			statement.setString(1, "NONE");
			this.handler.update(statement);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void loadAllPlayers() {
		try {
			ResultSet set = this.handler.get("SELECT * FROM players;");
			while (set.next()) {
				String username = set.getString("username");
				int karma = set.getInt("karma");
				long banDate = set.getLong("banDate");
				long banDuration = set.getLong("banDuration");
				String rank = set.getString("rank");
				int traitorKillsAsInnocent = set
						.getInt("traitorKillsAsInnocent");
				int traitorKillsAsDetective = set
						.getInt("traitorKillsAsDetective");
				int detectiveKills = set.getInt("detectiveKills");
				int innocentKills = set.getInt("innocentKills");
				int gamesPlayed = set.getInt("gamesPlayed");
				new TTTPlayer(username, karma, banDate, banDuration, rank,
						traitorKillsAsDetective, detectiveKills, innocentKills,
						traitorKillsAsInnocent, gamesPlayed);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public TTTPlayer loadPlayer(String name) {
		try {
			PreparedStatement statement = this.handler
					.getStatement("SELECT * FROM players WHERE username=?;");
			statement.setString(1, name);
			ResultSet set = this.handler.get(statement);
			if (set.first()) {
				int karma = set.getInt("karma");
				long banDate = set.getLong("banDate");
				long banDuration = set.getLong("banDuration");
				String rank = set.getString("rank");
				int traitorKillsAsInnocent = set
						.getInt("traitorKillsAsInnocent");
				int traitorKillsAsDetective = set
						.getInt("traitorKillsAsDetective");
				int detectiveKills = set.getInt("detectiveKills");
				int innocentKills = set.getInt("innocentKills");
				int gamesPlayed = set.getInt("gamesPlayed");
				return new TTTPlayer(name, karma, banDate, banDuration, rank,
						traitorKillsAsDetective, detectiveKills, innocentKills,
						traitorKillsAsInnocent, gamesPlayed);
			} else {
				return new TTTPlayer(name);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void logKill(TTTPlayer Tplayer, PlayerTeam kill) {
		String username = Tplayer.getName();
		try {
			PreparedStatement statement = this.handler
					.getStatement("SELECT * FROM players where username=?;");
			statement.setString(1, username);
			ResultSet result = this.handler.get(statement);
			String killColumn = "traitorKillsAsDetective";
			if (Tplayer.getTeam() == PlayerTeam.INNOCENT) {
				killColumn = "traitorKillsAsInnocent";
			}
			if (kill == PlayerTeam.DETECTIVE) {
				killColumn = "detectiveKills";
			} else if (kill == PlayerTeam.INNOCENT) {
				killColumn = "innocentKills";
			}
			if (result.first()) {
				statement = this.handler.getStatement("UPDATE players SET "
						+ killColumn + "=" + killColumn
						+ "+1 WHERE username=?;");
				statement.setString(1, username);
				this.handler.update(statement);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void logPlayedGame(TTTPlayer Tplayer) {
		String username = Tplayer.getName();
		try {
			PreparedStatement statement = this.handler
					.getStatement("SELECT * FROM players WHERE username=?;");
			statement.setString(1, username);
			ResultSet result = this.handler.get(statement);
			if (result.first()) {
				statement = this.handler
						.getStatement("UPDATE players SET gamesPlayed=gamesPlayed+1 WHERE username=?;");
				statement.setString(1, username);
				this.handler.update(statement);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveAllPlayers() {
		for (TTTPlayer Tplayer : TTTPlayer.getPlayers()) {
			updatePlayer(Tplayer);
		}
	}

	public void updatePlayer(TTTPlayer Tplayer) {
		try {
			PreparedStatement statement = this.handler
					.getStatement("SELECT * FROM players WHERE username=?;");
			statement.setString(1, Tplayer.getName());
			ResultSet set = this.handler.get(statement);
			if (set.first()) {
				statement = this.handler
						.getStatement("UPDATE players SET karma=?, banDate=?, banDuration=?, rank=? WHERE username=?;");
				statement.setInt(1, Tplayer.getKarma());
				statement.setLong(2, Tplayer.getBanDate());
				statement.setLong(3, Tplayer.getBanLength());
				statement.setString(4, Tplayer.getRank().name());
				statement.setString(5, Tplayer.getName());
				this.handler.update(statement);
			} else {
				statement = this.handler
						.getStatement("INSERT INTO players (username, karma, banDate, banDuration, rank) VALUES (?,?,?,?,?);");
				statement.setString(1, Tplayer.getName());
				statement.setInt(2, Tplayer.getKarma());
				statement.setLong(3, Tplayer.getBanDate());
				statement.setLong(4, Tplayer.getBanLength());
				statement.setString(5, Tplayer.getRank().name());
				this.handler.update(statement);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
