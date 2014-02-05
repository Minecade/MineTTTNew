package src.main.java.de.orion304.ttt.main;

import java.io.File;
import java.io.IOException;
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

	private static final String SQLLabel = "SQL";
	private static final String SQLUsernameLabel = SQLLabel + ".username";
	private static final String SQLPasswordLabel = SQLLabel + ".password";
	private static final String SQLDatabaseNameLabel = SQLLabel + ".database";
	private static final String SQLHostnameLabel = SQLLabel + ".hostname";
	private static final String SQLPortLabel = SQLLabel + ".port";

	private FileConfiguration locations = new YamlConfiguration();
	private FileConfiguration players = new YamlConfiguration();
	private FileConfiguration config = new YamlConfiguration();
	private File locationsFile, playersFile, configFile;
	private MineTTT plugin;

	public static int minimumNumberOfPlayers = 24; //
	public static long preparationTime = 15 * 1000L; //
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
			SQLdatabaseName = "database", SQLhostname = "localhost";
	public static int SQLport = 3306;

	/**
	 * Instantiates a FileHandler, which handles all hard files associated with
	 * the plugin.
	 * 
	 * @param instance
	 * 
	 * @return FileManager instance
	 */
	public FileManager(MineTTT instance) {
		plugin = instance;
		File folder = instance.getDataFolder();
		locationsFile = new File(folder, "locations.yml");
		playersFile = new File(folder, "players.yml");
		configFile = new File(folder, "config.yml");

		// Make the folder containing these hard files, if necessary
		if (!instance.getDataFolder().exists()) {
			instance.getDataFolder().mkdir();
		}

		// Load all the arena locations, or create the file that stores them if
		// there are none.
		if (locationsFile.exists()) {
			try {
				locations.load(locationsFile);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		} else {
			try {
				locationsFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Load all saved players, or make the files that stores them if there
		// are none.
		if (playersFile.exists()) {
			try {
				players.load(playersFile);
				getSavedPlayers();
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		} else {
			try {
				playersFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Load the configuration for the plugin, or create the file that stores
		// it if there is none.
		if (configFile.exists()) {
			try {
				config.load(configFile);
				loadConfigProperties();
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		} else {
			try {
				configFile.createNewFile();
				saveConfigProperties();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void loadConfigProperties() {
		minimumNumberOfPlayers = config.getInt(MinimumNumberOfPlayersLabel,
				minimumNumberOfPlayers);
		preparationTime = config.getLong(PreparationTimeLabel, preparationTime);
		detectiveRange = config.getDouble(DetectiveRangeLabel, detectiveRange);
		compassDuration = config.getLong(CompassDurationLabel, compassDuration);
		percentTraitors = config.getDouble(PercentTraitorsLabel,
				percentTraitors);
		percentDetectives = config.getDouble(PercentDetectivesLabel,
				percentDetectives);
		karmaThreshold = config.getInt(KarmaThresholdLabel, karmaThreshold);
		banDuration = config.getLong(BanDurationLabel, banDuration);
		chatItemCooldown = config.getLong(ChatItemCooldownLabel,
				chatItemCooldown);
		traitorColor = getChatColor(TraitorColorLabel, traitorColor);
		innocentColor = getChatColor(InnocentColorLabel, innocentColor);
		detectiveColor = getChatColor(DetectiveColorLabel, detectiveColor);
		spectatorColor = getChatColor(SpectatorColorLabel, spectatorColor);
		SQLusername = config.getString(SQLUsernameLabel, SQLusername);
		SQLpassword = config.getString(SQLPasswordLabel, SQLpassword);
		SQLdatabaseName = config.getString(SQLDatabaseNameLabel,
				SQLdatabaseName);
		SQLhostname = config.getString(SQLHostnameLabel, SQLhostname);
		SQLport = config.getInt(SQLPortLabel, SQLport);
		TTTPlayer.loadColors();
		saveConfig();
	}

	private ChatColor getChatColor(String label, ChatColor defaultColor) {
		String color = config.getString(label, defaultColor.name());
		ChatColor result = defaultColor;
		try {
			result = ChatColor.valueOf(color);
		} catch (IllegalArgumentException e) {
			config.set(label, defaultColor.name());
		}
		return result;
	}

	private void saveConfig() {
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveConfigProperties() {
		config.set(MinimumNumberOfPlayersLabel, minimumNumberOfPlayers);
		config.set(PreparationTimeLabel, preparationTime);
		config.set(DetectiveRangeLabel, detectiveRange);
		config.set(CompassDurationLabel, compassDuration);
		config.set(PercentTraitorsLabel, percentTraitors);
		config.set(PercentDetectivesLabel, percentDetectives);
		config.set(KarmaThresholdLabel, karmaThreshold);
		config.set(BanDurationLabel, banDuration);
		config.set(ChatItemCooldownLabel, chatItemCooldown);
		config.set(TraitorColorLabel, traitorColor.name());
		config.set(InnocentColorLabel, innocentColor.name());
		config.set(DetectiveColorLabel, detectiveColor.name());
		config.set(SpectatorColorLabel, spectatorColor.name());
		config.set(SQLHostnameLabel, SQLhostname);
		config.set(SQLPortLabel, SQLport);
		config.set(SQLUsernameLabel, SQLusername);
		config.set(SQLPasswordLabel, SQLpassword);
		config.set(SQLDatabaseNameLabel, SQLdatabaseName);
		saveConfig();
	}

	public void save() {
		try {
			locations.save(locationsFile);
			players.save(playersFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ConcurrentHashMap<String, Location> getArenaLocations() {
		ConcurrentHashMap<String, Location> locs = new ConcurrentHashMap<>();
		for (String key : locations.getKeys(false)) {
			if (!key.equalsIgnoreCase("lobby")) {
				Location location = getArenaLocation(key);
				if (location != null)
					locs.put(key, location);
			}
		}
		return locs;

	}

	public Location getArenaLocation(String name) {
		String worldProperty = ConfigProperty.getProperty(name,
				ConfigProperty.LOCATION_WORLD);
		String xProperty = ConfigProperty.getProperty(name,
				ConfigProperty.LOCATION_X);
		String yProperty = ConfigProperty.getProperty(name,
				ConfigProperty.LOCATION_Y);
		String zProperty = ConfigProperty.getProperty(name,
				ConfigProperty.LOCATION_Z);

		String worldname = locations.getString(worldProperty, null);
		double x = locations.getDouble(xProperty, 0);
		double y = locations.getDouble(yProperty, 0);
		double z = locations.getDouble(zProperty, 0);

		if (worldname == null)
			return null;

		World world = Bukkit.getWorld(worldname);

		if (world == null)
			return null;
		return new Location(world, x, y, z);
	}

	public void setArenaLocations(ConcurrentHashMap<String, Location> locations) {
		for (String key : locations.keySet()) {
			setArenaLocation(key, locations.get(key));
		}
	}

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

		locations.set(worldProperty, worldname);
		locations.set(xProperty, x);
		locations.set(yProperty, y);
		locations.set(zProperty, z);
		save();
	}

	public void setLobbyLocation(Location location) {
		String worldname = location.getWorld().getName();
		double x = location.getX();
		double y = location.getY();
		double z = location.getZ();

		locations.set(ConfigProperty.LOBBY_LOCATION_WORLD, worldname);
		locations.set(ConfigProperty.LOBBY_LOCATION_X, x);
		locations.set(ConfigProperty.LOBBY_LOCATION_Y, y);
		locations.set(ConfigProperty.LOBBY_LOCATION_Z, z);
		save();

	}

	public Location getLobbyLocation() {
		String worldname = locations.getString(
				ConfigProperty.LOBBY_LOCATION_WORLD, null);
		double x = locations.getDouble(ConfigProperty.LOBBY_LOCATION_X, 0);
		double y = locations.getDouble(ConfigProperty.LOBBY_LOCATION_Y, 0);
		double z = locations.getDouble(ConfigProperty.LOBBY_LOCATION_Z, 0);

		if (worldname == null)
			return null;

		World world = Bukkit.getWorld(worldname);

		if (world == null)
			return null;
		return new Location(world, x, y, z);
	}

	public void getSavedPlayers() {
		String name;
		int karma;
		long bandate, banlength;
		for (String player : players.getKeys(false)) {
			name = players.getString(player + ".name", null);
			karma = players.getInt(player + ".karma", 0);
			bandate = players.getLong(player + ".bandate", 0);
			banlength = players.getLong(player + ".banlength", 0);

			if (name != null) {
				new TTTPlayer(name, karma, bandate, banlength);
			}

		}
	}

	public void savePlayers() {
		players = new YamlConfiguration();
		String name, key;
		int karma;
		long banDate, banLength;
		Object value;
		for (TTTPlayer player : TTTPlayer.getPlayers()) {
			name = player.getName();
			karma = player.getKarma();
			banDate = player.getBanDate();
			banLength = player.getBanLength();

			String[] keys = { "name", "karma", "bandate", "banlength" };
			Object[] values = { name, karma, banDate, banLength };

			for (int i = 0; i < keys.length; i++) {
				key = name + "." + keys[i];
				value = values[i];
				players.set(key, value);
			}
		}
		save();
	}

}
