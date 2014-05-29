package com.minecade.minettt.properties;

public class ConfigProperty {

	private static final String locations = "locations", arena = "arena",
			lobby = "lobby", world = "world", x = "x", y = "y", z = "z";

	public static final String LOCATION_WORLD = getProperty(locations, world);
	public static final String LOCATION_X = getProperty(locations, x);
	public static final String LOCATION_Y = getProperty(locations, y);
	public static final String LOCATION_Z = getProperty(locations, z);

	private static String LOBBY_LOCATION = getProperty(locations, lobby);
	public static final String LOBBY_LOCATION_WORLD = getProperty(
			LOBBY_LOCATION, world);
	public static final String LOBBY_LOCATION_X = getProperty(LOBBY_LOCATION, x);
	public static final String LOBBY_LOCATION_Y = getProperty(LOBBY_LOCATION, y);
	public static final String LOBBY_LOCATION_Z = getProperty(LOBBY_LOCATION, z);

	public static String getProperty(String... strings) {
		String property = "";
		for (String string : strings) {
			if (property != "")
				property += ".";
			property += string;
		}
		return property;
	}

}
