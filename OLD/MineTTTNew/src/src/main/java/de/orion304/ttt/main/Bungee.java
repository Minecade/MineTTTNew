package src.main.java.de.orion304.ttt.main;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Bungee {

	static {
		Bukkit.getServer()
				.getMessenger()
				.registerOutgoingPluginChannel(MineTTT.getPlugin(),
						"BungeeCord");
	}

	public static void disconnect(Player player) {
		player.sendMessage("[c]Connecting to lobby..");
		Bungee.send(player, "Connect", "lobby");
	}

	private static void send(Player player, String... stuff) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);

		try {
			for (String string : stuff) {
				out.writeUTF(string);
			}
		} catch (Exception e) {
			// impossibro
			e.printStackTrace();
		}
		player.sendPluginMessage(MineTTT.getPlugin(), "BungeeCord",
				b.toByteArray());
	}

}
