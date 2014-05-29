package src.main.java.de.orion304.ttt.main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;

import src.main.java.org.orion304.SQLHandler;

public class ServerStatusThread implements Runnable {

	private final MineTTT plugin;
	private final SQLHandler handler;
	private final int id;

	public ServerStatusThread(MineTTT instance) {
		this.plugin = instance;
		this.handler = this.plugin.fileManager.database.handler;
		this.id = this.plugin.id;
		ensureFormat();
	}

	private void ensureFormat() {
		PreparedStatement statement;
		try {
			statement = this.handler.getStatement("SHOW TABLES LIKE ?;");

			statement.setString(1, "servers");
			ResultSet set = this.handler.get(statement);
			if (!set.first()) {
				statement = this.handler
						.getStatement("CREATE TABLE servers(id INT(11) NOT NULL AUTO_INCREMENT, max_players INT(11) NOT NULL, online_players INT(11) NOT NULL, state varchar(50) NOT NULL, map varchar(50) NOT NULL, PRIMARY KEY (id)) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;");
				this.handler.update(statement);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		update(false);
	}

	void update(boolean offline) {
		try {
			PreparedStatement statement;
			ResultSet set;

			statement = this.handler
					.getStatement("SELECT * FROM servers WHERE id=?;");
			statement.setInt(1, this.id);
			set = this.handler.get(statement);
			if (set.first()) {
				statement = this.handler
						.getStatement("UPDATE servers SET max_players=?, online_players=?, state=?, map=? WHERE id=?;");
			} else {
				statement = this.handler
						.getStatement("INSERT INTO servers (max_players, online_players, state, map, id) VALUES (?, ?, ?, ?, ?);");
			}
			if (this.plugin.thread.isOver()) {
				offline = true;
			}
			int maxPlayers = Bukkit.getMaxPlayers();
			int players = (offline) ? 0 : Bukkit.getOnlinePlayers().length;
			statement.setInt(1, maxPlayers);
			statement.setInt(2, players);

			String state;
			GameState gameState = this.plugin.thread.getGameStatus();
			switch (gameState) {
			default:
			case OFF:
			case GAME_PREPARING:
				state = "WAITING_FOR_PLAYERS";
				break;
			case GAME_RUNNING:
			case CELEBRATIONS:
				state = "IN_PROGRESS";
				break;
			}
			if (players >= maxPlayers) {
				state = "FULL";
			}
			if (offline) {
				state = "OFFLINE";
			}
			statement.setString(3, state);

			String map = this.plugin.thread.getArenaName();
			if (map == null) {
				map = "Lobby";
			}
			if (offline) {
				map = "Offline";
			}
			statement.setString(4, map);

			statement.setInt(5, this.id);

			this.handler.update(statement);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
