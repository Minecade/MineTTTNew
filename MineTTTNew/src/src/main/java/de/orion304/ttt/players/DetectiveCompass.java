package src.main.java.de.orion304.ttt.players;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import src.main.java.de.orion304.ttt.main.FileManager;

public class DetectiveCompass {

	private static ConcurrentHashMap<Player, DetectiveCompass> compasses = new ConcurrentHashMap<>();

	private static long duration = FileManager.compassDuration;

	private Player detective, killer;
	private long starttime;

	public DetectiveCompass(Player detective, Player killer) {
		this.detective = detective;
		this.killer = killer;

		starttime = System.currentTimeMillis();
		duration = FileManager.compassDuration;
		compasses.put(detective, this);
	}

	public static void progressAll() {
		for (Player detective : compasses.keySet()) {
			DetectiveCompass compass = compasses.get(detective);
			compass.progress();
		}
	}

	private void progress() {
		if (System.currentTimeMillis() > starttime + duration) {
			compasses.remove(detective);
			return;
		}

		if (detective.isDead()) {
			compasses.remove(detective);
			return;
		}

		if (!detective.isOnline()) {
			return;
		}

		if (!detective.getWorld().equals(killer.getWorld()))
			return;

		detective.setCompassTarget(killer.getLocation());
	}

}
