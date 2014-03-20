package src.main.java.de.orion304.ttt.main;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import src.main.java.de.orion304.ttt.players.Rank;
import src.main.java.de.orion304.ttt.players.TTTPlayer;

public class CommandHandler {

	MineTTT plugin;

	/**
	 * Creates a new CommandHandler object, which handles all command processes
	 * for MineTTT.
	 * 
	 * @param instance
	 */
	public CommandHandler(MineTTT instance) {
		this.plugin = instance;
	}

	/**
	 * Adds coins to the player's Minecade account.
	 * 
	 * @param player
	 *            The player who sent the command.
	 * @param args
	 *            The arguments of the command.
	 */
	private void addCoins(Player player, String[] args) {
		if (args.length != 2) {
			sendMessage(player, "Usage: /addcoins <player> <numberOfCoins>");
			return;
		}

		long i = 0;
		try {
			i = Long.parseLong(args[1]);
		} catch (NumberFormatException e) {
			sendMessage(player, "That wasn't a number...");
			return;
		}
		if (this.plugin.minecade.addCoins(args[0], i)) {
			sendMessage(player, i + " coins have been added to " + args[0]
					+ "'s account.");
		} else {
			sendMessage(player, "There was an error.");
		}
		update(args[0], null);
	}

	/**
	 * Forces the game to end.
	 * 
	 * @param player
	 *            The player who sent the command.
	 * @param args
	 *            The arguments of the command.
	 */
	private void forceEnd(Player player, String[] args) {
		this.plugin.thread.endGame(true);
		Bukkit.broadcastMessage(ChatColor.RED + "An admin has ended the game.");
	}

	/**
	 * Forces the game to begin its preparation stage.
	 * 
	 * @param player
	 *            The player who sent the command.
	 * @param args
	 *            The arguments of the command.
	 */
	private void forceStart(Player player, String[] args) {
		this.plugin.thread.startPreparations();
	}

	/**
	 * Prints the status of the game.
	 * 
	 * @param player
	 *            The player who sent the command.
	 * @param args
	 *            The arguments of the command.
	 */
	private void getStatus(Player player, String[] args) {
		if (player != null) {
			// Location loc = player.getLocation();
			// Hologram hologram = new Hologram(this.plugin,
			// "AABCDEFGHIJKLM0NOPQRSTUVWXYZZ",
			// "                     0                     ");
			// hologram.show(loc.clone().add(0, 1.5, 0), 300L, null);
			// Hologram l1 = new Hologram(this.plugin, "LINE 1");
			// l1.show(loc.clone().add(1, -1.5 + .23, 0), 300L, null);
			// Hologram l2 = new Hologram(this.plugin, "LINE 2");
			// l2.show(loc.clone().add(1, -1.5, 0), 300L, null);
		}
		sendMessage(player, "The status of MineTTT is: "
				+ this.plugin.thread.getGameStatus().toString());
	}

	/**
	 * Handle the command sent by the sender.
	 * 
	 * @param sender
	 *            The command sender.
	 * @param cmd
	 *            The command.
	 * @param label
	 *            The command's label.
	 * @param args
	 *            The arguments of the command.
	 * @return True if the command was valid.
	 */
	public boolean handleCommand(CommandSender sender, Command cmd,
			String label, String[] args) {
		String name = cmd.getName();

		name.toLowerCase();

		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
			if (!player.hasPermission("minettt." + name)) {
				sendMessage(player, "You do not have permission to use the "
						+ name + " command.");
				return true;
			}
		}

		switch (name) {
		case "setarenalocation":
			setArenaLocation(player, args);
			return true;
		case "setlobbylocation":
			setLobbyLocation(player, args);
			return true;
		case "gotolobby":
			teleportToLobby(player, args);
			return true;
		case "goto":
			teleportToArena(player, args);
			return true;
		case "forcestart":
			forceStart(player, args);
			return true;
		case "forceend":
			forceEnd(player, args);
			return true;
		case "getstatus":
			getStatus(player, args);
			return true;
		case "update":
			update(player, args);
			return true;
		case "shop":
			shop(player);
			return true;
		case "load":
			load();
			return true;
		case "setrank":
			setRank(player, args);
			return true;
		case "addcoins":
			addCoins(player, args);
			return true;
		}
		return false;
	}

	/**
	 * Forces the server to load the config and location files.
	 */
	private void load() {
		this.plugin.fileManager.load();
	}

	/**
	 * Sends a message to the player if it is a player, and prints the message
	 * to the console if player is null.
	 * 
	 * @param player
	 *            The player.
	 * @param message
	 *            The message to send/print.
	 */
	private void sendMessage(Player player, String message) {
		if (player == null) {
			Tools.verbose(message);
		} else {
			player.sendMessage(message);
		}
	}

	/**
	 * Sets an arena location.
	 * 
	 * @param player
	 *            The player who sent the command.
	 * @param args
	 *            The arguments of the command.
	 */
	private void setArenaLocation(Player player, String[] args) {
		String commandLineUsage = "Command line usage: setarenalocation <arenaname> <worldname> <x> <y> <z>";
		String playerUsage = "Usage: setarenalocation <arenaname> [worldname] [x] [y] [z]";

		if (args.length == 1) {
			if (player == null) {
				sendMessage(player, commandLineUsage);
				return;
			}

			Location location = player.getLocation();
			this.plugin.thread.setArenaLocation(args[0], location);
			sendMessage(player, "Arena location set to your current location.");
			return;
		}

		if (args.length == 4) {
			if (player == null) {
				sendMessage(player, commandLineUsage);
				return;
			}
			World world = player.getWorld();

			double x = Double.parseDouble(args[1]);
			double y = Double.parseDouble(args[2]);
			double z = Double.parseDouble(args[3]);

			Location location = new Location(world, x, y, z);
			this.plugin.thread.setArenaLocation(args[0], location);
			sendMessage(player,
					"Arena location set to those coordinates in current world.");
			return;
		}

		if (args.length == 5) {
			String worldname = args[1];
			World world = Bukkit.getWorld(worldname);
			if (world == null) {
				sendMessage(player, "That world is not recognized.");
			}

			double x = Double.parseDouble(args[2]);
			double y = Double.parseDouble(args[3]);
			double z = Double.parseDouble(args[4]);

			Location location = new Location(world, x, y, z);
			this.plugin.thread.setArenaLocation(args[0], location);
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

	/**
	 * Sets the lobby's location.
	 * 
	 * @param player
	 *            The player who sent the command.
	 * @param args
	 *            The arguments of the command.
	 */
	private void setLobbyLocation(Player player, String[] args) {
		String commandLineUsage = "Command line usage: setlobbylocation <worldname> <x> <y> <z>";
		String playerUsage = "Usage: setlobbylocation [worldname] [x] [y] [z]";

		if (args.length == 0) {
			if (player == null) {
				sendMessage(player, commandLineUsage);
				return;
			}

			Location location = player.getLocation();
			this.plugin.thread.setLobbyLocation(location);
			sendMessage(player, "Lobby location set to your current location.");
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
			this.plugin.thread.setLobbyLocation(location);
			sendMessage(player,
					"Lobby location set to those coordinates in current world.");
			return;
		}

		if (args.length == 4) {
			String worldname = args[0];
			World world = Bukkit.getWorld(worldname);
			if (world == null) {
				sendMessage(player, "That world is not recognized.");
			}

			double x = Double.parseDouble(args[1]);
			double y = Double.parseDouble(args[2]);
			double z = Double.parseDouble(args[3]);

			Location location = new Location(world, x, y, z);
			this.plugin.thread.setLobbyLocation(location);
			sendMessage(player,
					"Lobby location set to specified coordinates and world.");
			return;
		}

		if (player == null) {
			sendMessage(player, commandLineUsage);
			return;
		}
		sendMessage(player, playerUsage);
	}

	private void setRank(Player player, String[] args) {
		if (args.length == 0 || args.length > 2) {
			sendMessage(player, "Usage: /set rank <player> <rank>");
			return;
		}

		TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(args[0]);

		Rank rank = Rank.getRank(args[1]);
		if (rank == null) {
			sendMessage(player, "That is not a valid rank.");
			return;
		}

		Tplayer.setRank(rank);
		if (rank == Rank.NONE) {
			sendMessage(player, args[0] + " was cleared of all ranks.");
		} else {
			sendMessage(player, args[0] + " was set to rank: " + rank);
		}

		update(args[0], null);
	}

	/**
	 * Opens the shop for the player.
	 * 
	 * @param player
	 *            The player to open the shop for.
	 */
	private void shop(Player player) {
		TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(player);
		Tplayer.openShop();
	}

	/**
	 * Teleports the player to the location.
	 * 
	 * @param player
	 *            The player who sent the command.
	 * @param args
	 *            The arguments of the command.
	 * @param location
	 *            The name of the location.
	 */
	private void teleportTo(Player player, String[] args, String location) {
		ArrayList<Player> players = new ArrayList<>();
		String playerlist = "";
		Player p;
		for (String arg : args) {
			p = Bukkit.getPlayer(arg);
			if (p != null) {
				players.add(p);
				playerlist += p.getName() + ", ";
			}
		}
		if (playerlist.length() > 0) {
			playerlist = playerlist.substring(0, playerlist.length() - 2);
		}

		Location loc;
		if (location.equalsIgnoreCase("lobby")) {
			loc = this.plugin.thread.getLobbyLocation();
			if (args.length == 0) {
				if (player == null) {
					sendMessage(player, "Command line usage: gotolobby"
							+ location + " <player1> [player2] [player3] ...");
					return;
				}
				player.teleport(loc);
				sendMessage(player, "Teleported you to the lobby.");
				return;
			}

			if (players.isEmpty()) {
				sendMessage(player,
						"None of the names given are online players.");
				return;
			}

			sendMessage(player, "Teleported players to the lobby: "
					+ playerlist);
		} else {
			loc = this.plugin.thread.getArenaLocation(location);
			if (loc == null) {
				sendMessage(player, "That destination doesn't exist!");
				return;
			}
		}

		if (args.length == 1) {
			if (player == null) {
				sendMessage(player, "Command line usage: goto" + location
						+ " <player1> [player2] [player3] ...");
				return;
			}
			player.teleport(loc);
			sendMessage(player, "Teleported you to " + location + " Arena.");
			return;
		}

		if (players.isEmpty()) {
			sendMessage(player, "None of the names given are online players.");
			return;
		}

		sendMessage(player, "Teleported players to " + location + ": "
				+ playerlist);
	}

	/**
	 * Teleports the player to an arena.
	 * 
	 * @param player
	 *            The player who sent the command.
	 * @param args
	 *            The arguments of the command.
	 */
	private void teleportToArena(Player player, String[] args) {
		if (args.length == 0) {
			sendMessage(player, "You must specific which arena.");
			return;
		}
		teleportTo(player, args, args[0]);
	}

	/**
	 * Teleports the player to the lobby.
	 * 
	 * @param player
	 *            The player who sent the command.
	 * @param args
	 *            The arguments of the command.
	 */
	private void teleportToLobby(Player player, String[] args) {
		teleportTo(player, args, "Lobby");

	}

	/**
	 * Forces the server to update the player's account from Minecade.
	 * 
	 * @param player
	 *            The player who sent the command.
	 * @param args
	 *            The arguments of the command.
	 */
	private void update(Player player, String[] args) {
		if (player == null) {
			sendMessage(player, "This command cannot be used from the console.");
			return;
		}
		update(player.getName(), args);
	}

	/**
	 * Forces the server to update the player's account from Minecade.
	 * 
	 * @param player
	 *            The player who sent the command.
	 * @param args
	 *            The arguments of the command.
	 */
	private void update(String playerName, String[] args) {
		TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(playerName);
		Tplayer.loadMinecadeAccount();
		Tplayer.refreshScoreboard();
	}

}
