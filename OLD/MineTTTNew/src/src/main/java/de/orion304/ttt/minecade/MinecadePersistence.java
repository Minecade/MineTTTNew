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
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

/**
 * @author VictorV
 * 
 */
public class MinecadePersistence {

	private Connection con = null;
	public int query_count = 0;
	/** Reference to plugin's main class. */
	public final Plugin plugin;

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
	public MinecadePersistence(Plugin minecade, String host, int port,
			String database, String username, String password)
			throws ClassNotFoundException {
		this.plugin = minecade;
		this.centralJdbcUrl = "jdbc:mysql://" + host + ":" + port + "/"
				+ database + "?autoReconnect=true";
		this.centralDbUsername = username;
		this.centralDbpassword = password;
		Class.forName("com.mysql.jdbc.Driver");

		minecade.getLogger().info("Central Database successfully initialized");
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
	public boolean addCoins(UUID uuid, long buttercoins) {
		if (uuid != null) {
			PreparedStatement stmt = null;
			String id = uuid.toString().replaceAll("-", "");
			try {
				stmt = getConnection()
						.prepareStatement(
								"UPDATE accounts SET butter_coins = butter_coins + ? WHERE uuid = ?");
				stmt.setLong(1, buttercoins);
				stmt.setString(2, id);
				int affectedRecords = stmt.executeUpdate();

				if (affectedRecords > 0) {
					return true;
				}
			} catch (final SQLException e) {
				this.plugin.getLogger().severe(
						"Failed update player butter coins: " + uuid);
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
									+ uuid);
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
	public boolean banPlayer(UUID uuid, String reason, String bannedBy) {
		if (uuid != null) {
			PreparedStatement stmt = null;
			ResultSet set = null;
			String id = uuid.toString().replaceAll("-", "");
			try {

				stmt = getConnection().prepareStatement(
						"SELECT * FROM bans WHERE uuid = ?");
				stmt.setString(1, id);
				set = stmt.executeQuery();

				if (set.first()) {
					stmt.close();
					stmt = getConnection()
							.prepareStatement(
									"UPDATE bans SET is_banned = ?, reason = ?, banned_date = ?, banned_by = ? WHERE uuid = ?");
					stmt.setBoolean(1, true);
					stmt.setString(2, reason);
					stmt.setDate(3, new java.sql.Date(new Date().getTime()));
					stmt.setString(4, bannedBy);
					stmt.setString(5, id);
				} else {
					// Player does not exist in database
					stmt.close();
					stmt = getConnection()
							.prepareStatement(
									"INSERT INTO bans (uuid, is_banned, reason, banned_date, banned_by) VALUES (?,?,?,?,?)");
					stmt.setString(1, id);
					stmt.setBoolean(2, true);
					stmt.setString(3, reason);
					stmt.setDate(4, new java.sql.Date(new Date().getTime()));
					stmt.setString(5, bannedBy);
				}

				stmt.executeUpdate();
				this.plugin.getLogger().info(
						"'" + uuid + "' was banned by '" + bannedBy
								+ "' for reason:" + reason);
			} catch (final SQLException e) {
				this.plugin.getLogger().severe("Failed to ban player: " + uuid);
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
									+ uuid);
					e.printStackTrace();
				}
			}

			return true;
		}

		return false;
	}

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

		List<String> accounts = new ArrayList<String>();
		PreparedStatement stmt = null;
		ResultSet set = null;

		try {
			stmt = getConnection().prepareStatement(
					"SELECT uuid FROM bans where is_banned = 1;");
			set = stmt.executeQuery();

			while (set.next()) {
				accounts.add(set.getString("uuid"));
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

		return null;
	}

	/**
	 * Gets the all minecade accounts.
	 * 
	 * @return the all minecade accounts
	 */
	public List<MinecadeAccount> getAllMinecadeAccounts() {

		List<MinecadeAccount> accounts = new ArrayList<MinecadeAccount>();
		PreparedStatement stmt = null;
		ResultSet set = null;

		try {
			stmt = getConnection().prepareStatement("SELECT * FROM accounts;");
			set = stmt.executeQuery();

			while (set.next()) {
				MinecadeAccount account = new MinecadeAccount();
				account.setUUID(set.getString("uuid"));
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
				this.plugin.getLogger().severe(
						"Error closing resources while loading all players.");
				e.printStackTrace();
			}
		}

		return null;
	}

	public Connection getConnection() {
		try {
			if (this.query_count >= 1000) {
				if (this.con != null) {
					this.con.close();
				}

				this.con = DriverManager.getConnection(this.centralJdbcUrl,
						this.centralDbUsername, this.centralDbpassword);
				this.query_count = 0;
			}
			if (this.con == null || this.con.isClosed()) {
				Class.forName("com.mysql.jdbc.Driver");
				this.con = DriverManager.getConnection(this.centralJdbcUrl,
						this.centralDbUsername, this.centralDbpassword);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.query_count++;
		return this.con;
	}

	/**
	 * Gets the sky network account.
	 * 
	 * @param username
	 *            the username
	 * @return the sky network account
	 */
	public MinecadeAccount getMinecadeAccount(UUID uuid, String username) {
		if (uuid != null) {
			PreparedStatement stmt = null;
			ResultSet set = null;
			MinecadeAccount account = new MinecadeAccount();
			account.setUUID(uuid);
			String id = uuid.toString().replaceAll("-", "");
			try {
				stmt = getConnection().prepareStatement(
						"SELECT * FROM accounts WHERE uuid = ?;");
				stmt.setString(1, id);
				set = stmt.executeQuery();

				if (!set.first()) {
					// Player does not exist in database
					stmt.close();
					stmt = getConnection()
							.prepareStatement(
									"INSERT INTO accounts(uuid, username) VALUES (?, ?) ON DUPLICATE KEY UPDATE uuid=?;");
					stmt.setString(1, id);
					stmt.setString(2, username);
					stmt.setString(3, id);
					stmt.executeUpdate();
				} else {
					account.setVip(set.getBoolean("vip"));
					account.setYoutuber(set.getBoolean("youtuber"));
					account.setButterCoins(set.getLong("butter_coins"));
					account.setAdmin(set.getBoolean("admin"));
					account.setGm(set.getBoolean("gm"));
					account.setCm(set.getBoolean("cm"));
					account.setDev(set.getBoolean("dev"));
					account.setOwner(set.getBoolean("owner"));
					account.setTitan(set.getBoolean("titan"));
					account.setPet(set.getString("pet_type"));
					account.setVipPassDate(set.getTimestamp("vip_pass_date"));
					account.setVipPassDailyAttemps(set
							.getInt("vip_pass_daily_attemps"));
				}

				return account;
			} catch (Exception ex) {
				this.plugin.getLogger().severe(
						"Failed to load data from database for player: " + uuid
								+ ", error: " + ex.getMessage());
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
									+ uuid);
					e.printStackTrace();
				}
			}
		}

		return null;
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
			MinecadeAccount account = getMinecadeAccount(player.getUniqueId(),
					player.getName());
			return account != null && account.isAdmin();
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
	public boolean isPlayerBanned(UUID uuid) {
		if (uuid != null) {
			PreparedStatement stmt = null;
			ResultSet set = null;
			String id = uuid.toString().replaceAll("-", "");
			try {
				stmt = getConnection().prepareStatement(
						"SELECT is_banned FROM bans WHERE uuid = ?");
				stmt.setString(1, id);
				set = stmt.executeQuery();

				if (set.first()) {
					return set.getBoolean("is_banned");
				}
			} catch (final SQLException e) {
				this.plugin.getLogger().severe(
						"Failed to fetch ban status of player: " + uuid);
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
									+ uuid);
					e.printStackTrace();
				}
			}
		}

		return false;
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
			MinecadeAccount account = getMinecadeAccount(player.getUniqueId(),
					player.getName());
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
			MinecadeAccount account = getMinecadeAccount(players.getUniqueId(),
					players.getName());
			return account != null
					&& (account.isVip() || account.isGm() || account.isAdmin() || account
							.isCm());
		}

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
	 * Unban a user in the central database.
	 * 
	 * @param player
	 *            the player
	 * @param unbannedBy
	 *            the unbanned by
	 */
	public boolean unbanPlayer(UUID uuid, String unbannedBy) {
		if (uuid != null) {
			PreparedStatement stmt = null;
			ResultSet set = null;
			String id = uuid.toString().replaceAll("-", "");
			try {
				stmt = getConnection().prepareStatement(
						"SELECT * FROM bans WHERE uuid = ?");
				stmt.setString(1, id);
				set = stmt.executeQuery();

				if (set.first()) {
					stmt.close();
					stmt = getConnection()
							.prepareStatement(
									"UPDATE bans SET is_banned = ?, unbanned_by = ? WHERE uuid = ?");
					stmt.setBoolean(1, false);
					stmt.setString(2, unbannedBy);
					stmt.setString(3, id);
					stmt.executeUpdate();
					this.plugin.getLogger()
							.info("'" + uuid + "' was unbanned.");
					return true;
				} else {
					this.plugin.getLogger().info(
							"'" + uuid + "' was not found on the bans table.");
					return false;
				}
			} catch (final SQLException e) {
				this.plugin.getLogger().severe(
						"Failed to unban player: " + uuid);
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
									+ uuid);
					e.printStackTrace();
				}
			}
		}
		this.plugin.getLogger().info(
				"Unable to connect to the database to unban player: '" + uuid
						+ "'");
		return false;
	}
}
