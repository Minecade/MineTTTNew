package src.main.java.de.orion304.ttt.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import src.main.java.de.orion304.ttt.main.MineTTT;

public class PlayerListener implements Listener {

	MineTTT plugin;

	public PlayerListener(MineTTT instance) {
		plugin = instance;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (plugin.thread.isGameRunning()) {

		}
	}
}
