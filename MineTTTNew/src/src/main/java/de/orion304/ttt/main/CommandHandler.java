package src.main.java.de.orion304.ttt.main;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler {

	MineTTT plugin;

	public CommandHandler(MineTTT instance) {
		plugin = instance;
	}

	public boolean handleCommand(CommandSender sender, Command cmd,
			String label, String[] args) {
		String name = cmd.getName();

		name.toLowerCase();

		switch (name) {
		case "setarenalocation":
			setArenaLocation(sender, args);
			return true;
		case "setlobbylocation":
			return true;
		}
		return false;
	}

	private void setArenaLocation(CommandSender sender, String[] args) {
		String commandLineUsage = "Command line usage: setarenalocation <worldname> <x> <y> <z>";
		String playerUsage = "Usage: setarenalocation [worldname] [x] [y] [z]";

		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		if (args.length == 0) {
			if (player == null) {
				sendMessage(player, commandLineUsage);
				return;
			}

			Location location = player.getLocation();
			plugin.thread.setArenaLocation(location);
			sendMessage(player, "Arena location set to your current location.");
			return;
		}

		if (args.length == 3) {
			if (player == null) {
				sendMessage(player, commandLineUsage);
				return;
			}
			World world = player.getWorld();

			double x = Double.parseDouble(args[0]);
			double y = Double.parseDouble(args[1]);
			double z = Double.parseDouble(args[2]);

			Location location = new Location(world, x, y, z);
			plugin.thread.setArenaLocation(location);
			sendMessage(player,
					"Arena location set to those coordinates in current world.");
			return;
		}

		if (args.length == 4) {
			String worldname = args[0];
			World world = plugin.server.getWorld(worldname);
			if (world == null) {
				sendMessage(player, "That world is not recognized.");
			}

			double x = Double.parseDouble(args[1]);
			double y = Double.parseDouble(args[2]);
			double z = Double.parseDouble(args[3]);

			Location location = new Location(world, x, y, z);
			plugin.thread.setArenaLocation(location);
			sendMessage(player,
					"Arena location set to specified coordinates and world.");
			return;
		}

		if (player == null) {
			sendMessage(player, commandLineUsage);
			return;
		}
		sendMessage(player, playerUsage);
	}

	private void sendMessage(Player player, String message) {
		if (player == null) {
			Tools.verbose(message);
		} else {
			player.sendMessage(message);
		}
	}

}
