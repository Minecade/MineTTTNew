/**
 * 
 */
package src.main.java.de.orion304.ttt.minecade;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.OfflinePlayer;

import src.main.java.de.orion304.ttt.main.MineTTT;

/**
 * @author VictorV
 * 
 */
public class MinecadePersistence {

	/** Reference to plugin's main class. */
	public final MineTTT plugin;

	protected Connection connection;

	private final String centralJdbcUrl;

	private final String centralDbUsername;

	private final String centralDbpassword;

	/**
	 * Instantiates a new minecade persistence.
	 * 
	 * @param plugin
	 *            the plugin
	 * @throws ClassNotFoundException
	 */
	public MinecadePersistence(MineTTT minecade, String host, int port,
			String database, String username, String password)
			throws ClassNotFoundException {
		this.plugin = minecade;
		this.centralJdbcUrl = "jdbc:mysql://" + host + ":" + port + "/"
				+ database + "?autoReconnect=true";
		this.centralDbUsername = username;
		this.centralDbpassword = password;
		Class.forName("com.mysql.jdbc.Driver");
		connect();

		minecade.getLogger().info("Central Database successfully initialized");
	}

	/**
	 * Connect to the database.
	 * 
	 * @return true, if successful
	 */
	protected boolean connect() {
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

	/**
	 * Checks if is player staff.
	 * 
	 * @param bukkitPlayer
	 *            the bukkit player.
	 * @return true, if is the player is staff.
	 */
	public boolean isPlayerStaff(OfflinePlayer player) {
		if (player != null) {
			MinecadeAccount account = getMinecadeAccount(player.getName());
			return account != null
					&& (account.isGm() || account.isAdmin() || account.isCm());
		}

		return false;
	}

	/**
	 * Checks if is player staff.
	 * 
	 * @param players
	 *            the bukkit player.
	 * @return true, if is the player is staff.
	 */
	public boolean isPlayerStaffOrVIP(OfflinePlayer players) {
		if (players != null) {
			MinecadeAccount account = getMinecadeAccount(players.getName());
			return account != null
					&& (account.isVip() || account.isGm() || account.isAdmin() || account
							.isCm());
		}

		return false;
	}

	/**
	 * Checks if is player Admin or OP.
	 * 
	 * @param player
	 *            the bukkit player.
	 * @return true, if is the player is Admin or OP.
	 * @author kvnamo
	 */
	public boolean isPlayerAdmin(OfflinePlayer player) {
		if (player != null) {
			MinecadeAccount account = getMinecadeAccount(player.getName());
			return account != null && account.isAdmin();
		}

		return false;
	}

	/**
	 * Gets the sky network account.
	 * 
	 * @param username
	 *            the username
	 * @return the sky network account
	 */
	public MinecadeAccount getMinecadeAccount(String username) {
		if (!username.isEmpty() && this.connect()) {
			username = username.toLowerCase();
			PreparedStatement stmt = null;
			ResultSet set = null;
			MinecadeAccount account = new MinecadeAccount();
			account.setUsername(username);
			try {
				stmt = connection
						.prepareStatement("SELECT * FROM accounts WHERE username = ?;");
				stmt.setString(1, username);
				set = stmt.executeQuery();

				if (!set.first()) {
					// Player does not exist in database
					stmt.close();
					stmt = this.connection
							.prepareStatement("INSERT INTO accounts(username) VALUES (?)");
					stmt.setString(1, username);
					stmt.executeUpdate();
				} else {
					account.setVip(set.getBoolean("vip"));
					account.setYoutuber(set.getBoolean("youtuber"));
					account.setButterCoins(set.getLong("butter_coins"));
					account.setAdmin(set.getBoolean("admin"));
					account.setGm(set.getBoolean("gm"));
					account.setCm(set.getBoolean("cm"));
					account.setPet(set.getString("pet_type"));
					account.setVipPassDate(set.getTimestamp("vip_pass_date"));
					account.setVipPassDailyAttemps(set
							.getInt("vip_pass_daily_attemps"));
				}

				return account;
			} catch (Exception ex) {
				this.plugin.getLogger().severe(
						"Failed to load data from database for player: "
								+ username + ", error: " + ex.getMessage());
			} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}

					if (set != null) {
						set.close();
					}
				} catch (final SQLException e) {
					this.plugin.getLogger().severe(
							"Error closing resources while loading from the database for player: "
									+ username);
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	/**
	 * Gets the all minecade accounts.
	 * 
	 * @return the all minecade accounts
	 */
	public List<MinecadeAccount> getAllMinecadeAccounts() {
		if (this.connect()) {
			List<MinecadeAccount> accounts = new ArrayList<MinecadeAccount>();
			PreparedStatement stmt = null;
			ResultSet set = null;

			try {
				stmt = this.connection
						.prepareStatement("SELECT * FROM accounts;");
				set = stmt.executeQuery();

				while (set.next()) {
					MinecadeAccount account = new MinecadeAccount();
					account.setUsername(set.getString("username"));
					account.setVip(set.getBoolean("vip"));
					account.setYoutuber(set.getBoolean("youtuber"));
					account.setButterCoins(set.getLong("butter_coins"));
					account.setAdmin(set.getBoolean("admin"));
					account.setGm(set.getBoolean("gm"));
					account.setCm(set.getBoolean("cm"));
					account.setPet(set.getString("pet_type"));
					account.setVipPassDate(set.getTimestamp("vip_pass_date"));
					account.setVipPassDailyAttemps(set
							.getInt("vip_pass_daily_attemps"));
					accounts.add(account);
				}

				return accounts;
			} catch (Exception ex) {
				this.plugin.getLogger().severe(
						"Failed to load all players fron database");
				ex.printStackTrace();
			} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}

					if (set != null) {
						set.close();
					}
				} catch (final SQLException e) {
					this.plugin
							.getLogger()
							.severe("Error closing resources while loading all players.");
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * Update player butter coins.
	 * 
	 * @param username
	 *            the username
	 * @param buttercoins
	 *            the buttercoins
	 * @return true, if successful
	 */
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

	/**
	 * Check if the player is ban
	 * 
	 * @param username
	 * @return player ban
	 * @author: kvnamo
	 */
	public boolean isPlayerBanned(String username) {
		if (!username.isEmpty() && this.connect()) {
			username = username.toLowerCase();
			PreparedStatement stmt = null;
			ResultSet set = null;
			try {
				stmt = this.connection
						.prepareStatement("SELECT is_banned FROM bans WHERE username = ?");
				stmt.setString(1, username);
				set = stmt.executeQuery();

				if (set.first()) {
					return set.getBoolean("is_banned");
				}
			} catch (final SQLException e) {
				this.plugin.getLogger().severe(
						"Failed to fetch ban status of player: " + username);
				e.printStackTrace();

				return false;
			} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}

					if (set != null) {
						set.close();
					}
				} catch (final SQLException e) {
					this.plugin.getLogger().severe(
							"Error closing resources while fetching ban status of player: "
									+ username);
					e.printStackTrace();
				}
			}
		}

		return false;
	}

	/**
	 * Ban a user in the central database.
	 * 
	 * @param player
	 *            the player
	 * @param reason
	 *            the reason
	 * @param bannedBy
	 *            the banned by
	 */
	public boolean banPlayer(String player, String reason, String bannedBy) {
		if (!player.isEmpty() && this.connect()) {
			player = player.toLowerCase();
			PreparedStatement stmt = null;
			ResultSet set = null;

			try {

				stmt = this.connection
						.prepareStatement("SELECT * FROM bans WHERE username = ?");
				stmt.setString(1, player);
				set = stmt.executeQuery();

				if (set.first()) {
					stmt.close();
					stmt = this.connection
							.prepareStatement("UPDATE bans SET is_banned = ?, reason = ?, banned_date = ?, banned_by = ? WHERE username = ?");
					stmt.setBoolean(1, true);
					stmt.setString(2, reason);
					stmt.setDate(3, new java.sql.Date(new Date().getTime()));
					stmt.setString(4, bannedBy);
					stmt.setString(5, player);
				} else {
					// Player does not exist in database
					stmt.close();
					stmt = this.connection
							.prepareStatement("INSERT INTO bans (username, is_banned, reason, banned_date, banned_by) VALUES (?,?,?,?,?)");
					stmt.setString(1, player);
					stmt.setBoolean(2, true);
					stmt.setString(3, reason);
					stmt.setDate(4, new java.sql.Date(new Date().getTime()));
					stmt.setString(5, bannedBy);
				}

				stmt.executeUpdate();
				this.plugin.getLogger().info(
						"'" + player + "' was banned by '" + bannedBy
								+ "' for reason:" + reason);
			} catch (final SQLException e) {
				this.plugin.getLogger().severe(
						"Failed to ban player: " + player);
				e.printStackTrace();

				return false;
			} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}

					if (set != null) {
						set.close();
					}
				} catch (final SQLException e) {
					this.plugin.getLogger().severe(
							"Error closing resources while banning player: "
									+ player);
					e.printStackTrace();
				}
			}

			return true;
		}

		return false;
	}

	/**
	 * Unban a user in the central database.
	 * 
	 * @param player
	 *            the player
	 * @param unbannedBy
	 *            the unbanned by
	 */
	public boolean unbanPlayer(String player, String unbannedBy) {
		if (!player.isEmpty() && this.connect()) {
			player = player.toLowerCase();
			PreparedStatement stmt = null;
			ResultSet set = null;
			try {
				stmt = this.connection
						.prepareStatement("SELECT * FROM bans WHERE username = ?");
				stmt.setString(1, player);
				set = stmt.executeQuery();

				if (set.first()) {
					stmt.close();
					stmt = this.connection
							.prepareStatement("UPDATE bans SET is_banned = ?, unbanned_by = ? WHERE username = ?");
					stmt.setBoolean(1, false);
					stmt.setString(2, unbannedBy);
					stmt.setString(3, player);
					stmt.executeUpdate();
					this.plugin.getLogger().info(
							"'" + player + "' was unbanned.");
					return true;
				} else {
					this.plugin.getLogger()
							.info("'" + player
									+ "' was not found on the bans table.");
					return false;
				}
			} catch (final SQLException e) {
				this.plugin.getLogger().severe(
						"Failed to unban player: " + player);
				e.printStackTrace();

				return false;
			} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}

					if (set != null) {
						set.close();
					}
				} catch (final SQLException e) {
					this.plugin.getLogger().severe(
							"Error closing resources while unbanning player: "
									+ player);
					e.printStackTrace();
				}
			}
		}
		this.plugin.getLogger().info(
				"Unable to connect to the database to unban player: '" + player
						+ "'");
		return false;
	}

	/**
	 * Sets the rank.
	 * 
	 * @param player
	 *            the player
	 * @param rank
	 *            the rank
	 * @param modifier
	 *            the modifier
	 * @throws SQLException
	 */
	// public void setRank(String player, String rank, boolean modifier)
	// throws SQLException {
	// setPlayerTag(player, rank, modifier);
	// }

	/**
	 * Sets the player tag.
	 * 
	 * @param player
	 *            the player
	 * @param tag
	 *            the tag
	 * @param value
	 *            the value
	 * @return true, if successful
	 * @throws SQLException
	 */
	// private void setPlayerTag(String player, String tag, boolean value)
	// throws SQLException {
	// if (!"youtuber".equalsIgnoreCase(tag)
	// && (tag.isEmpty() || PlayerTagEnum.valueOf(tag.toUpperCase()) == null)) {
	// throw new IllegalArgumentException(String.format(
	// "Invalid Player Tag: [%s]", tag));
	// }
	//
	// if (!player.isEmpty() && this.connect()) {
	// tag = tag.equalsIgnoreCase("YT") ? "youtuber" : tag;
	// player = player.toLowerCase();
	// PreparedStatement stmt = null;
	// ResultSet set = null;
	// try {
	// stmt = this.connection
	// .prepareStatement("SELECT * FROM accounts WHERE username = ?");
	// stmt.setString(1, player);
	// set = stmt.executeQuery();
	//
	// if (!set.first()) {
	// stmt.close();
	// stmt = this.connection
	// .prepareStatement("INSERT INTO accounts(username, "
	// + tag + ") VALUES (?, ?)");
	// stmt.setString(1, player);
	// stmt.setBoolean(2, value);
	// } else {
	// stmt.close();
	// stmt = this.connection
	// .prepareStatement("UPDATE accounts SET " + tag
	// + " = ? WHERE username = ?");
	// stmt.setBoolean(1, value);
	// stmt.setString(2, player);
	// }
	//
	// stmt.executeUpdate();
	// this.plugin.getLogger().info(
	// "'" + player + "' was tagged: " + tag.toUpperCase()
	// + " = " + value);
	// } finally {
	// if (stmt != null) {
	// stmt.close();
	// }
	//
	// if (set != null) {
	// set.close();
	// }
	// }
	// }
	// }

	// public HashMap<String, String> getGeneralSettings() {
	// if (this.connect()) {
	// PreparedStatement stmt = null;
	// ResultSet set = null;
	// HashMap<String, String> settings = new HashMap<>();
	// try {
	// stmt = this.connection
	// .prepareStatement("SELECT * FROM settings");
	// set = stmt.executeQuery();
	// while (set.next()) {
	// SettingsEnum value = SettingsEnum
	// .getSettingsEnumByString(set.getString("setting"));
	// if (value != null) {
	// settings.put(value.toString(), set.getString("value"));
	// }
	// }
	// return settings;
	// } catch (Exception ex) {
	// this.plugin.getLogger().severe(
	// "Failed to load settings from central database"
	// + ", error: " + ex.getMessage());
	// } finally {
	// try {
	// if (stmt != null) {
	// stmt.close();
	// }
	//
	// if (set != null) {
	// set.close();
	// }
	// } catch (final SQLException e) {
	// this.plugin
	// .getLogger()
	// .severe("Error closing resources while loading settings from central database");
	// e.printStackTrace();
	// }
	// }
	// }
	//
	// return null;
	// }

	public List<String> getAllBannedAccounts() {
		if (this.connect()) {
			List<String> accounts = new ArrayList<String>();
			PreparedStatement stmt = null;
			ResultSet set = null;

			try {
				stmt = this.connection
						.prepareStatement("SELECT username FROM bans where is_banned = 1;");
				set = stmt.executeQuery();

				while (set.next()) {
					accounts.add(set.getString("username"));
				}

				return accounts;
			} catch (Exception ex) {
				this.plugin.getLogger().severe(
						"Failed to load all bans from database");
				ex.printStackTrace();
			} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}

					if (set != null) {
						set.close();
					}
				} catch (final SQLException e) {
					this.plugin.getLogger().severe(
							"Error closing resources while loading all bans.");
					e.printStackTrace();
				}
			}
		}
		return null;
	}
}
