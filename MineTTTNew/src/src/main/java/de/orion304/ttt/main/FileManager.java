package src.main.java.de.orion304.ttt.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import src.main.java.de.orion304.ttt.players.TTTPlayer;
import src.main.java.de.orion304.ttt.properties.ConfigProperty;

public class FileManager {

	private static final String PropertiesLabel = "Properties";
	private static final String MinimumNumberOfPlayersLabel = PropertiesLabel
			+ ".minimumNumberOfPlayers";
	private static final String PreparationTimeLabel = PropertiesLabel
			+ ".preparationTime";
	private static final String DetectiveRangeLabel = PropertiesLabel
			+ ".detectiveFindBodyRange";
	private static final String CompassDurationLabel = PropertiesLabel
			+ ".detectiveCompassDuration";
	private static final String ChatItemCooldownLabel = PropertiesLabel
			+ ".chatItemCooldown";
	private static final String PercentTraitorsLabel = PropertiesLabel
			+ ".precentTraitors";
	private static final String PercentDetectivesLabel = PropertiesLabel
			+ ".percentDetectives";
	private static final String KarmaThresholdLabel = PropertiesLabel
			+ ".karmaThreshold";
	private static final String BanDurationLabel = PropertiesLabel
			+ ".banDuration";
	private static final String ColorLabel = "Colors";
	private static final String TraitorColorLabel = ColorLabel + ".traitor";
	private static final String DetectiveColorLabel = ColorLabel + ".detective";
	private static final String InnocentColorLabel = ColorLabel + ".innocent";
	private static final String SpectatorColorLabel = ColorLabel + ".spectator";

	private static final String SQLLabel = "Minecade_SQL";
	private static final String SQLUsernameLabel = SQLLabel + ".username";
	private static final String SQLPasswordLabel = SQLLabel + ".password";
	private static final String SQLDatabaseNameLabel = SQLLabel + ".database";
	private static final String SQLHostnameLabel = SQLLabel + ".hostname";
	private static final String SQLPortLabel = SQLLabel + ".port";
	private static final String PlayerSQLLabel = "Player_SQL";
	private static final String UsePlayerSQLLabel = PlayerSQLLabel + ".use";
	private static final String PlayerSQLUsernameLabel = PlayerSQLLabel
			+ ".username";
	private static final String PlayerSQLPasswordLabel = PlayerSQLLabel
			+ ".password";
	private static final String PlayerSQLDatabaseNameLabel = PlayerSQLLabel
			+ ".database";
	private static final String PlayerSQLHostnameLabel = PlayerSQLLabel
			+ ".hostname";
	private static final String PlayerSQLPortLabel = PlayerSQLLabel + ".port";

	private final FileConfiguration locations = new YamlConfiguration();
	private FileConfiguration players = new YamlConfiguration();
	private final FileConfiguration config = new YamlConfiguration();
	private final File locationsFile, playersFile, configFile;
	private final MineTTT plugin;

	public static int minimumNumberOfPlayers = 24; //
	public static long preparationTime = 30 * 1000L; //
	public static double detectiveRange = 5D;
	public static long compassDuration = 5 * 1000L; //
	public static double percentTraitors = .25, percentDetectives = .125;
	public static int karmaThreshold = 200; //
	public static long banDuration = 5 * 60 * 1000L; //
	public static long chatItemCooldown = 10 * 1000L; //
	public static ChatColor traitorColor = ChatColor.RED,
			detectiveColor = ChatColor.AQUA, innocentColor = ChatColor.WHITE,
			spectatorColor = ChatColor.GRAY;

	public static String SQLusername = "username", SQLpassword = "password",
			SQLdatabaseName = "database", SQLhostname = "minecadehost";
	public static int SQLport = 3306;

	public static String PlayerSQLusername = "username",
			PlayerSQLpassword = "password", PlayerSQLdatabaseName = "database",
			PlayerSQLhostname = "localhost";
	public static int PlayerSQLport = 3306;
	private static boolean UsePlayerSQL = false;

	private LocalDatabase database;

	/**
	 * Instantiates a FileHandler, which handles all hard files associated with
	 * the plugin.
	 * 
	 * @param instance
	 * 
	 * @return FileManager instance
	 */
	public FileManager(MineTTT instance) {
		this.plugin = instance;
		File folder = instance.getDataFolder();
		this.locationsFile = new File(folder, "locations.yml");
		this.playersFile = new File(folder, "players.yml");
		this.configFile = new File(folder, "config.yml");

		load();
		loadPlayers();
	}

	/**
	 * Retrieves the chat colors of all the arena.
	 * 
	 * @return A concurrent hash map with the colors.
	 */
	public ConcurrentHashMap<String, ChatColor> getArenaColors() {
		ConcurrentHashMap<String, ChatColor> colors = new ConcurrentHashMap<>();
		for (String key : this.locations.getKeys(false)) {
			if (!key.equalsIgnoreCase("lobby")) {
				ChatColor color = getColor(key);

				if (color != null) {
					colors.put(key, color);
				}
			}
		}
		return colors;

	}

	/**
	 * Retrieves the arena location.
	 * 
	 * @param name
	 *            The name of the arena.
	 * @return The location of the arena.
	 */
	public Location getArenaLocation(String name) {
		String worldProperty = ConfigProperty.getProperty(name,
				ConfigProperty.LOCATION_WORLD);
		String xProperty = ConfigProperty.getProperty(name,
				ConfigProperty.LOCATION_X);
		String yProperty = ConfigProperty.getProperty(name,
				ConfigProperty.LOCATION_Y);
		String zProperty = ConfigProperty.getProperty(name,
				ConfigProperty.LOCATION_Z);

		String worldname = this.locations.getString(worldProperty, null);
		double x = this.locations.getDouble(xProperty, 0);
		double y = this.locations.getDouble(yProperty, 0);
		double z = this.locations.getDouble(zProperty, 0);

		if (worldname == null) {
			return null;
		}

		World world = Bukkit.getWorld(worldname);

		if (world == null) {
			return null;
		}
		return new Location(world, x, y, z);
	}

	/**
	 * Retrieves a map of all the arena locations, with their names as the keys
	 * and locations as the values.
	 * 
	 * @return A concurrent hash map of the arena locations.
	 */
	public ConcurrentHashMap<String, Location> getArenaLocations() {
		ConcurrentHashMap<String, Location> locs = new ConcurrentHashMap<>();
		for (String key : this.locations.getKeys(false)) {
			if (!key.equalsIgnoreCase("lobby")) {
				ChatColor color = getColor(key);
				Location location = getArenaLocation(key);
				if (location != null) {
					locs.put(color + key, location);
				}

			}
		}
		return locs;

	}

	/**
	 * Retrieves a map of all arena lores, with their names as the keys and
	 * their lores as the values.
	 * 
	 * @return A concurrent hash map of the arena lores.
	 */
	public ConcurrentHashMap<String, List<String>> getArenaLores() {
		ConcurrentHashMap<String, List<String>> lores = new ConcurrentHashMap<>();
		for (String key : this.locations.getKeys(false)) {
			if (!key.equalsIgnoreCase("lobby")) {
				ChatColor color = getColor(key);
				List<String> lore = getLore(key);
				if (lore != null) {
					lores.put(color + key, lore);
				}
			}
		}
		return lores;

	}

	/**
	 * Retrieves the chat color from the string stored in the configuration
	 * file.
	 * 
	 * @param label
	 *            The key of the config.
	 * @param defaultColor
	 *            The color to resort to should the key not hold correct
	 *            information.
	 * @return The chat color of the key.
	 */
	private ChatColor getChatColor(String label, ChatColor defaultColor) {
		String color = this.config.getString(label, defaultColor.name());
		ChatColor result = defaultColor;
		try {
			result = ChatColor.valueOf(color);
		} catch (IllegalArgumentException e) {
			this.config.set(label, defaultColor.name());
		}
		return result;
	}

	/**
	 * Gets the chat color of the location name.
	 * 
	 * @param key
	 *            The location's name.
	 * @return The chat color of the location, or ChatColor.WHITE if there was
	 *         an error in retrieving that information.
	 */
	private ChatColor getColor(String key) {
		if (this.locations.contains(key + ".color")) {
			String string = this.locations.getString(key + ".color", "WHITE");
			ChatColor result = ChatColor.WHITE;
			try {
				result = ChatColor.valueOf(string);
				return result;
			} catch (IllegalArgumentException e) {
				this.locations.set(key + ".color", "WHITE");
				return ChatColor.WHITE;
			}
		}
		ChatColor color = ChatColor.WHITE;
		this.locations.set(key + ".color", color.name());
		return color;
	}

	/**
	 * Gets the lobby location from the locations file.
	 * 
	 * @return The lobby's location.
	 */
	public Location getLobbyLocation() {
		String worldname = this.locations.getString(
				ConfigProperty.LOBBY_LOCATION_WORLD, null);
		double x = this.locations.getDouble(ConfigProperty.LOBBY_LOCATION_X, 0);
		double y = this.locations.getDouble(ConfigProperty.LOBBY_LOCATION_Y, 0);
		double z = this.locations.getDouble(ConfigProperty.LOBBY_LOCATION_Z, 0);

		if (worldname == null) {
			return null;
		}

		World world = Bukkit.getWorld(worldname);

		if (world == null) {
			return null;
		}
		return new Location(world, x, y, z);
	}

	/**
	 * Gets the lore of an arena.
	 * 
	 * @param key
	 *            The name of the arena.
	 * @return The lore of the arena.
	 */
	private List<String> getLore(String key) {
		if (this.locations.contains(key + ".lore")) {
			return this.locations.getStringList(key + ".lore");
		}
		List<String> lore = new ArrayList<>();
		lore.add("Write lore here");
		this.locations.set(key + ".lore", lore);
		return lore;
	}

	/**
	 * Loads all saved players into TTTPlayer.
	 */
	public void getSavedPlayers() {
		String name;
		int karma;
		long bandate, banlength;
		String rank;
		for (String player : this.players.getKeys(false)) {
			name = this.players.getString(player + ".name", null);
			karma = this.players.getInt(player + ".karma", 0);
			bandate = this.players.getLong(player + ".bandate", 0);
			banlength = this.players.getLong(player + ".banlength", 0);
			rank = this.players.getString(player + ".rank", "NONE");

			if (name != null) {
				new TTTPlayer(name, karma, bandate, banlength, rank);
			}

		}
	}

	/**
	 * Loads the config and locations files into memory.
	 */
	void load() {
		// Make the folder containing these hard files, if necessary
		if (!this.plugin.getDataFolder().exists()) {
			this.plugin.getDataFolder().mkdir();
		}

		// Load all the arena locations, or create the file that stores them if
		// there are none.
		if (this.locationsFile.exists()) {
			try {
				this.locations.load(this.locationsFile);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		} else {
			try {
				this.locationsFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Load the configuration for the plugin, or create the file that stores
		// it if there is none.
		if (this.configFile.exists()) {
			try {
				this.config.load(this.configFile);
				loadConfigProperties();
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		} else {
			try {
				this.configFile.createNewFile();
				saveConfigProperties();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (UsePlayerSQL) {
			this.database = new LocalDatabase(this.plugin, PlayerSQLhostname,
					PlayerSQLport, PlayerSQLdatabaseName, PlayerSQLusername,
					PlayerSQLpassword);
		}
	}

	/**
	 * Loads all config properties into memory.
	 */
	private void loadConfigProperties() {
		minimumNumberOfPlayers = this.config.getInt(
				MinimumNumberOfPlayersLabel, minimumNumberOfPlayers);
		preparationTime = this.config.getLong(PreparationTimeLabel,
				preparationTime);
		detectiveRange = this.config.getDouble(DetectiveRangeLabel,
				detectiveRange);
		compassDuration = this.config.getLong(CompassDurationLabel,
				compassDuration);
		percentTraitors = this.config.getDouble(PercentTraitorsLabel,
				percentTraitors);
		percentDetectives = this.config.getDouble(PercentDetectivesLabel,
				percentDetectives);
		karmaThreshold = this.config
				.getInt(KarmaThresholdLabel, karmaThreshold);
		banDuration = this.config.getLong(BanDurationLabel, banDuration);
		chatItemCooldown = this.config.getLong(ChatItemCooldownLabel,
				chatItemCooldown);
		traitorColor = getChatColor(TraitorColorLabel, traitorColor);
		innocentColor = getChatColor(InnocentColorLabel, innocentColor);
		detectiveColor = getChatColor(DetectiveColorLabel, detectiveColor);
		spectatorColor = getChatColor(SpectatorColorLabel, spectatorColor);
		SQLusername = this.config.getString(SQLUsernameLabel, SQLusername);
		SQLpassword = this.config.getString(SQLPasswordLabel, SQLpassword);
		SQLdatabaseName = this.config.getString(SQLDatabaseNameLabel,
				SQLdatabaseName);
		SQLhostname = this.config.getString(SQLHostnameLabel, SQLhostname);
		SQLport = this.config.getInt(SQLPortLabel, SQLport);
		UsePlayerSQL = this.config.getBoolean(UsePlayerSQLLabel, UsePlayerSQL);
		PlayerSQLusername = this.config.getString(PlayerSQLUsernameLabel,
				PlayerSQLusername);
		PlayerSQLpassword = this.config.getString(PlayerSQLPasswordLabel,
				PlayerSQLpassword);
		PlayerSQLdatabaseName = this.config.getString(
				PlayerSQLDatabaseNameLabel, PlayerSQLdatabaseName);
		PlayerSQLhostname = this.config.getString(PlayerSQLHostnameLabel,
				PlayerSQLhostname);
		PlayerSQLport = this.config.getInt(PlayerSQLPortLabel, PlayerSQLport);
		TTTPlayer.loadColors();
		saveConfig();
	}

	/**
	 * Loads all saved players into memory.
	 */
	private void loadPlayers() {

		if (UsePlayerSQL) {
			this.database.loadAllPlayers();
		} else {
			// Make the folder containing these hard files, if necessary
			if (!this.plugin.getDataFolder().exists()) {
				this.plugin.getDataFolder().mkdir();
			}

			// Load all saved players, or make the files that stores them if
			// there
			// are none.
			if (this.playersFile.exists()) {
				try {
					this.players.load(this.playersFile);
					getSavedPlayers();
				} catch (IOException | InvalidConfigurationException e) {
					e.printStackTrace();
				}
			} else {
				try {
					this.playersFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Saves the locations and players files.
	 */
	public void save() {
		try {
			this.locations.save(this.locationsFile);
			this.players.save(this.playersFile);
			saveConfigProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void save(TTTPlayer tttPlayer) {
		if (UsePlayerSQL) {
			this.database.updatePlayer(tttPlayer);
		}
	}

	/**
	 * Saves the configuration file.
	 */
	private void saveConfig() {
		try {
			this.config.save(this.configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves the config properties in memory.
	 */
	private void saveConfigProperties() {
		this.config.set(MinimumNumberOfPlayersLabel, minimumNumberOfPlayers);
		this.config.set(PreparationTimeLabel, preparationTime);
		this.config.set(DetectiveRangeLabel, detectiveRange);
		this.config.set(CompassDurationLabel, compassDuration);
		this.config.set(PercentTraitorsLabel, percentTraitors);
		this.config.set(PercentDetectivesLabel, percentDetectives);
		this.config.set(KarmaThresholdLabel, karmaThreshold);
		this.config.set(BanDurationLabel, banDuration);
		this.config.set(ChatItemCooldownLabel, chatItemCooldown);
		this.config.set(TraitorColorLabel, traitorColor.name());
		this.config.set(InnocentColorLabel, innocentColor.name());
		this.config.set(DetectiveColorLabel, detectiveColor.name());
		this.config.set(SpectatorColorLabel, spectatorColor.name());
		this.config.set(SQLHostnameLabel, SQLhostname);
		this.config.set(SQLPortLabel, SQLport);
		this.config.set(SQLUsernameLabel, SQLusername);
		this.config.set(SQLPasswordLabel, SQLpassword);
		this.config.set(SQLDatabaseNameLabel, SQLdatabaseName);
		this.config.set(UsePlayerSQLLabel, UsePlayerSQL);
		this.config.set(PlayerSQLHostnameLabel, PlayerSQLhostname);
		this.config.set(PlayerSQLPortLabel, PlayerSQLport);
		this.config.set(PlayerSQLUsernameLabel, PlayerSQLusername);
		this.config.set(PlayerSQLPasswordLabel, PlayerSQLpassword);
		this.config.set(PlayerSQLDatabaseNameLabel, PlayerSQLdatabaseName);
		saveConfig();
	}

	/**
	 * Saves the TTTPlayers to file.
	 */
	public void savePlayers() {
		if (UsePlayerSQL) {
			this.database.saveAllPlayers();
		} else {
			this.players = new YamlConfiguration();
			String name, key;
			int karma;
			long banDate, banLength;
			String rank;
			Object value;
			for (TTTPlayer player : TTTPlayer.getPlayers()) {
				name = player.getName();
				karma = player.getKarma();
				banDate = player.getBanDate();
				banLength = player.getBanLength();
				rank = player.getRank().name();

				String[] keys = { "name", "karma", "bandate", "banlength",
						"rank" };
				Object[] values = { name, karma, banDate, banLength, rank };

				for (int i = 0; i < keys.length; i++) {
					key = name + "." + keys[i];
					value = values[i];
					this.players.set(key, value);
				}
			}
		}
		save();
	}

	/**
	 * Sets the arena location and saves the locations file.
	 * 
	 * @param name
	 *            The name of the arena.
	 * @param location
	 *            Its location.
	 */
	public void setArenaLocation(String name, Location location) {
		String worldProperty = ConfigProperty.getProperty(name,
				ConfigProperty.LOCATION_WORLD);
		String xProperty = ConfigProperty.getProperty(name,
				ConfigProperty.LOCATION_X);
		String yProperty = ConfigProperty.getProperty(name,
				ConfigProperty.LOCATION_Y);
		String zProperty = ConfigProperty.getProperty(name,
				ConfigProperty.LOCATION_Z);

		String worldname = location.getWorld().getName();
		double x = location.getX();
		double y = location.getY();
		double z = location.getZ();

		this.locations.set(worldProperty, worldname);
		this.locations.set(xProperty, x);
		this.locations.set(yProperty, y);
		this.locations.set(zProperty, z);

		this.locations.set(name + ".color", ChatColor.WHITE);
		List<String> lore = new ArrayList<>();
		lore.add("Write lore here.");
		this.locations.set(name + ".lore", lore);
		save();
	}

	/**
	 * Saves all arena locations to file.
	 * 
	 * @param locations
	 *            The map of the locations.
	 */
	public void setArenaLocations(ConcurrentHashMap<String, Location> locations) {
		for (String key : locations.keySet()) {
			setArenaLocation(key, locations.get(key));
		}
	}

	/**
	 * Sets the lobby location and saves the file.
	 * 
	 * @param location
	 *            The lobby's location.
	 */
	public void setLobbyLocation(Location location) {
		String worldname = location.getWorld().getName();
		double x = location.getX();
		double y = location.getY();
		double z = location.getZ();

		this.locations.set(ConfigProperty.LOBBY_LOCATION_WORLD, worldname);
		this.locations.set(ConfigProperty.LOBBY_LOCATION_X, x);
		this.locations.set(ConfigProperty.LOBBY_LOCATION_Y, y);
		this.locations.set(ConfigProperty.LOBBY_LOCATION_Z, z);
		save();

	}

}
