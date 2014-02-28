package src.main.java.de.orion304.ttt.main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import src.main.java.de.orion304.ttt.players.PlayerTeam;
import src.main.java.de.orion304.ttt.players.TTTPlayer;

public class ChatManager {

	private final MineTTT plugin;

	/**
	 * Creates a new ChatManager instance, which will handle the processing and
	 * formatting of all PlayerChatEvents.
	 * 
	 * @param instance
	 */
	public ChatManager(MineTTT instance) {
		this.plugin = instance;
	}

	/**
	 * The bread and butter of the ChatManager class. Takes the message that the
	 * player sent, then manually sends that to whomever is supposed to see it,
	 * while formatting the name properly.
	 * 
	 * @param player
	 *            The player who sent the message.
	 * @param message
	 *            The message sent.
	 */
	public void handleChat(Player player, String message) {
		TTTPlayer sender = TTTPlayer.getTTTPlayer(player);
		PlayerTeam senderTeam = sender.getTeam();
		String senderName;
		for (Player p : Bukkit.getOnlinePlayers()) {
			TTTPlayer receiver = TTTPlayer.getTTTPlayer(p);
			senderName = TTTPlayer.getDisplayedName(sender, receiver);
			PlayerTeam receiverTeam = receiver.getTeam();
			if (senderTeam == PlayerTeam.NONE) {
				if (receiverTeam == PlayerTeam.NONE) {
					p.sendMessage(senderName + ChatColor.RESET.toString()
							+ ChatColor.GRAY + ": " + message);
				}
				continue;
			}

			p.sendMessage(senderName + ChatColor.RESET.toString()
					+ ChatColor.GRAY + ": " + message);
		}

	}

}
