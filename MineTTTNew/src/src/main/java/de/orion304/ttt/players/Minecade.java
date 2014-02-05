package src.main.java.de.orion304.ttt.players;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import src.main.java.de.orion304.ttt.main.MineTTT;

public class Minecade {

	private static String centralJdbcUrl;
	private static String centralDbUsername;
	private static String centralDbpassword;

	private static String username, password, host, port, database;

	private Connection connection;
	private MineTTT plugin;

	public Minecade(MineTTT instance) throws ClassNotFoundException {
		plugin = instance;
		centralJdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database
				+ "?autoReconnect=true";
		centralDbUsername = username;
		centralDbpassword = password;
		Class.forName("com.mysql.jdbc.Driver");
		connect();

		plugin.getLogger().info("Central Database successfully initialized");
	}

	public boolean connect() {
		if (this.connection != null) {
			try {
				if (this.connection.isValid(1)) {
					return true;
				}
			} catch (final SQLException e) {
				// This only throws an SQLException if the number input is less
				// than 0
			}
		}

		try {
			this.connection = DriverManager.getConnection(centralJdbcUrl,
					centralDbUsername, centralDbpassword);
			return true;
		} catch (final SQLException e) {
			plugin.getLogger().severe("Failed to connect to the database!");
			e.printStackTrace();
			return false;
		}
	}

	public boolean addCoins(String username, long buttercoins) {
		if (!username.isEmpty() && this.connect()) {
			username = username.toLowerCase();
			PreparedStatement stmt = null;
			try {
				stmt = this.connection
						.prepareStatement("UPDATE accounts SET butter_coins = butter_coins + ? WHERE username = ?");
				stmt.setLong(1, buttercoins);
				stmt.setString(2, username);
				int affectedRecords = stmt.executeUpdate();

				if (affectedRecords > 0) {
					return true;
				}
			} catch (final SQLException e) {
				this.plugin.getLogger().severe(
						"Failed update player butter coins: " + username);
				e.printStackTrace();

				return false;
			} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}
				} catch (final SQLException e) {
					this.plugin.getLogger().severe(
							"Error closing resources while updating player butter coins: "
									+ username);
					e.printStackTrace();
				}
			}
		}

		return false;
	}

}
