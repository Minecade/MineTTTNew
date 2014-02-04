package src.main.java.de.orion304.ttt.main;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import src.main.java.de.orion304.ttt.players.TTTPlayer;
import src.main.java.de.orion304.ttt.properties.ConfigProperty;

public class FileManager {

	private FileConfiguration config = new YamlConfiguration();
	private FileConfiguration players = new YamlConfiguration();
	private File file, playersFile;
	private MineTTT plugin;

	public FileManager(MineTTT instance) {
		plugin = instance;
		File folder = instance.getDataFolder();
		file = new File(folder, "locations.yml");
		playersFile = new File(folder, "players.yml");
		if (!instance.getDataFolder().exists()) {
			instance.getDataFolder().mkdir();
		}
		if (file.exists()) {

			try {
				config.load(file);
			} catch (IOException | InvalidConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (playersFile.exists()) {
			try {
				players.load(playersFile);
				getSavedPlayers();
			} catch (IOException | InvalidConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {

			try {
				playersFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void save() {
		try {
			config.save(file);
			players.save(playersFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ConcurrentHashMap<String, Location> getArenaLocations() {
		ConcurrentHashMap<String, Location> locations = new ConcurrentHashMap<>();
		for (String key : config.getKeys(false)) {
			if (!key.equalsIgnoreCase("lobby")) {
				Location location = getArenaLocation(key);
				if (location != null)
					locations.put(key, location);
			}
		}
		return locations;

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

		String worldname = config.getString(worldProperty, null);
		double x = config.getDouble(xProperty, 0);
		double y = config.getDouble(yProperty, 0);
		double z = config.getDouble(zProperty, 0);

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

		config.set(worldProperty, worldname);
		config.set(xProperty, x);
		config.set(yProperty, y);
		config.set(zProperty, z);
		save();
	}

	public void setLobbyLocation(Location location) {
		String worldname = location.getWorld().getName();
		double x = location.getX();
		double y = location.getY();
		double z = location.getZ();

		config.set(ConfigProperty.LOBBY_LOCATION_WORLD, worldname);
		config.set(ConfigProperty.LOBBY_LOCATION_X, x);
		config.set(ConfigProperty.LOBBY_LOCATION_Y, y);
		config.set(ConfigProperty.LOBBY_LOCATION_Z, z);
		save();

	}

	public Location getLobbyLocation() {
		String worldname = config.getString(
				ConfigProperty.LOBBY_LOCATION_WORLD, null);
		double x = config.getDouble(ConfigProperty.LOBBY_LOCATION_X, 0);
		double y = config.getDouble(ConfigProperty.LOBBY_LOCATION_Y, 0);
		double z = config.getDouble(ConfigProperty.LOBBY_LOCATION_Z, 0);

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
