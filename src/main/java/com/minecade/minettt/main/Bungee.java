package com.minecade.minettt.main;

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
		player.sendMessage(MineTTT.getPlugin().getMessage("bungee.connecting"));
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
