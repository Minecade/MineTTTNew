package com.minecade.minettt.players;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DeathLocation {

	private static Set<DeathLocation> locations = new HashSet<DeathLocation>();

	/**
	 * Gets all locations where players have died.
	 * 
	 * @return The Set of all DeathLocations.
	 */
	public static synchronized Set<DeathLocation> getDeathLocations() {
		return locations;
	}

	/**
	 * Resets all death locations.
	 */
	public static void reset() {
		locations.clear();
	}

	private final String killedPlayer, killer;

	private final Location deathLocation;

	/**
	 * Sets the location a player died at, along with its killer.
	 * 
	 * @param killedPlayer
	 *            The name of the player that died.
	 * @param killer
	 *            The name of the player that killed them.
	 * @param location
	 *            The location of the death.
	 */
	public DeathLocation(String killedPlayer, String killer, Location location) {
		this.killedPlayer = killedPlayer;
		this.killer = killer;
		this.deathLocation = location;

		locations.add(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DeathLocation other = (DeathLocation) obj;
		if (this.deathLocation == null) {
			if (other.deathLocation != null) {
				return false;
			}
		} else if (!this.deathLocation.equals(other.deathLocation)) {
			return false;
		}
		if (this.killedPlayer == null) {
			if (other.killedPlayer != null) {
				return false;
			}
		} else if (!this.killedPlayer.equals(other.killedPlayer)) {
			return false;
		}
		if (this.killer == null) {
			if (other.killer != null) {
				return false;
			}
		} else if (!this.killer.equals(other.killer)) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the location of the death.
	 * 
	 * @return The death location.
	 */
	public Location getDeathLocation() {
		return this.deathLocation;
	}

	/**
	 * Get the killer attached to this death location.
	 * 
	 * @return The Player that killed in this location.
	 */
	public Player getKiller() {
		Player player = Bukkit.getPlayerExact(this.killer);
		return player;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((this.deathLocation == null) ? 0 : this.deathLocation
						.hashCode());
		result = prime
				* result
				+ ((this.killedPlayer == null) ? 0 : this.killedPlayer
						.hashCode());
		result = prime * result
				+ ((this.killer == null) ? 0 : this.killer.hashCode());
		return result;
	}

}
