package src.main.java.de.orion304.ttt.players;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DeathLocation {

	private static Set<DeathLocation> locations = new HashSet<DeathLocation>();

	private String killedPlayer, killer;
	private Location deathLocation;

	public DeathLocation(String killedPlayer, String killer, Location location) {
		this.killedPlayer = killedPlayer;
		this.killer = killer;
		this.deathLocation = location;

		locations.add(this);
	}

	public Location getDeathLocation() {
		return deathLocation;
	}

	public static synchronized Set<DeathLocation> getDeathLocations() {
		return locations;
	}

	public Player getKiller() {
		Player player = Bukkit.getPlayer(killer);
		return player;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((deathLocation == null) ? 0 : deathLocation.hashCode());
		result = prime * result
				+ ((killedPlayer == null) ? 0 : killedPlayer.hashCode());
		result = prime * result + ((killer == null) ? 0 : killer.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeathLocation other = (DeathLocation) obj;
		if (deathLocation == null) {
			if (other.deathLocation != null)
				return false;
		} else if (!deathLocation.equals(other.deathLocation))
			return false;
		if (killedPlayer == null) {
			if (other.killedPlayer != null)
				return false;
		} else if (!killedPlayer.equals(other.killedPlayer))
			return false;
		if (killer == null) {
			if (other.killer != null)
				return false;
		} else if (!killer.equals(other.killer))
			return false;
		return true;
	}

}
