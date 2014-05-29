package com.minecade.minettt.players;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.minecade.minettt.main.FileManager;

public class DetectiveCompass {

	private static ConcurrentHashMap<Player, DetectiveCompass> compasses = new ConcurrentHashMap<>();

	private static long duration = FileManager.compassDuration;

	/**
	 * Handles all DetectiveCompass objects in existence.
	 */
	public static void progressAll() {
		for (Player detective : compasses.keySet()) {
			DetectiveCompass compass = compasses.get(detective);
			compass.progress();
		}
	}

	private final Player detective, killer;

	private final long starttime;

	/**
	 * Creates an object which will handle the detective's compass pointing to
	 * the killer.
	 * 
	 * @param detective
	 *            The detective.
	 * @param killer
	 *            The killer the detective is tracking.
	 */
	public DetectiveCompass(Player detective, Player killer) {
		this.detective = detective;
		this.killer = killer;

		this.starttime = System.currentTimeMillis();
		duration = FileManager.compassDuration;
		compasses.put(detective, this);
	}

	/**
	 * Handles this detective compass - makes it point to the killer or
	 * deactivate after the correct time.
	 */
	private void progress() {
		if (System.currentTimeMillis() > this.starttime + duration) {
			compasses.remove(this.detective);
			return;
		}

		if (this.detective.isDead()) {
			compasses.remove(this.detective);
			return;
		}

		if (!this.detective.isOnline()) {
			return;
		}

		if (!this.detective.getWorld().equals(this.killer.getWorld())) {
			return;
		}

		this.detective.setCompassTarget(this.killer.getLocation());
	}

}
