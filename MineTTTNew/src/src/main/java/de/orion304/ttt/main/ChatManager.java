package src.main.java.de.orion304.ttt.main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import src.main.java.de.orion304.ttt.players.PlayerTeam;
import src.main.java.de.orion304.ttt.players.TTTPlayer;

public class ChatManager {

	private MineTTT plugin;

	public ChatManager(MineTTT instance) {
		plugin = instance;
	}

	public void handleChat(Player player, String message) {
		GameState state = plugin.thread.getGameStatus();
		TTTPlayer sender = TTTPlayer.getTTTPlayer(player);
		PlayerTeam senderTeam = sender.getTeam();
		String senderName = player.getDisplayName();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (state != GameState.GAME_RUNNING) {
				p.sendMessage(player.getDisplayName() + ": " + message);
				continue;
			}
			TTTPlayer receiver = TTTPlayer.getTTTPlayer(p);
			PlayerTeam receiverTeam = receiver.getTeam();
			if (senderTeam == PlayerTeam.NONE) {
				if (receiverTeam == PlayerTeam.NONE) {
					p.sendMessage(MainThread.spectatorColor + "<Spectator> "
							+ senderName + ChatColor.RESET + ": " + message);
				}
				continue;
			}

			if (senderTeam == PlayerTeam.TRAITOR
					&& (receiverTeam == PlayerTeam.NONE || receiverTeam == PlayerTeam.TRAITOR)) {
				p.sendMessage(MainThread.traitorColor + senderName
						+ ChatColor.RESET + ": " + message);
				continue;
			}

			if (senderTeam == PlayerTeam.DETECTIVE) {
				p.sendMessage(MainThread.detectiveColor + senderName
						+ ChatColor.RESET + ": " + message);
				continue;
			}

			p.sendMessage(senderName + ": " + message);

		}

	}

}
