package com.minecade.minettt.main;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.minecade.minettt.players.Rank;
import com.minecade.minettt.players.TTTPlayer;

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
			sendMessage(player, MineTTT.getPlugin().getMessage("commandhandler.coins"));
			return;
		}

		long i = 0;
		try {
			i = Long.parseLong(args[1]);
		} catch (NumberFormatException e) {
			sendMessage(player, MineTTT.getPlugin().getMessage("commandhandler.coinsnumber"));
			return;
		}
		OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(args[0]);
		if (this.plugin.minecade.addCoins(oPlayer.getUniqueId(), i)) {
			sendMessage(player, String.format(MineTTT.getPlugin().getMessage("commandhandler.coinsadded"), i, args[0])); 
		} else {
			sendMessage(player, MineTTT.getPlugin().getMessage("commandhandler.coinserror"));
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
		Bukkit.broadcastMessage(MineTTT.getPlugin().getMessage("commandhandler.end"));
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
		sendMessage(player, String.format(MineTTT.getPlugin().getMessage("commandhandler.status"), this.plugin.thread.getGameStatus().getName()));
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
				sendMessage(player, String.format(MineTTT.getPlugin().getMessage("commandhandler.handle"), name));
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
		String commandLineUsage = MineTTT.getPlugin().getMessage("commandhandler.arena");
		String playerUsage = MineTTT.getPlugin().getMessage("commandhandler.arenaplayer");

		if (args.length == 1) {
			if (player == null) {
				sendMessage(player, commandLineUsage);
				return;
			}

			Location location = player.getLocation();
			this.plugin.thread.setArenaLocation(args[0], location);
			sendMessage(player, MineTTT.getPlugin().getMessage("commandhandler.arenaplayerlocation"));
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
			sendMessage(player, MineTTT.getPlugin().getMessage("commandhandler.arenalocation"));
			return;
		}

		if (args.length == 5) {
			String worldname = args[1];
			World world = Bukkit.getWorld(worldname);
			if (world == null) {
				sendMessage(player, MineTTT.getPlugin().getMessage("commandhandler.arenaworld"));
			}

			double x = Double.parseDouble(args[2]);
			double y = Double.parseDouble(args[3]);
			double z = Double.parseDouble(args[4]);

			Location location = new Location(world, x, y, z);
			this.plugin.thread.setArenaLocation(args[0], location);
			sendMessage(player, MineTTT.getPlugin().getMessage("commandhandler.arenaset"));
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
		String commandLineUsage = MineTTT.getPlugin().getMessage("commandhandler.lobbylocationcommand");
		String playerUsage = MineTTT.getPlugin().getMessage("commandhandler.lobbylocationplayer");

		if (args.length == 0) {
			if (player == null) {
				sendMessage(player, commandLineUsage);
				return;
			}

			Location location = player.getLocation();
			this.plugin.thread.setLobbyLocation(location);
			sendMessage(player, MineTTT.getPlugin().getMessage("commandhandler.lobbylocationcurrent"));
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
			sendMessage(player, MineTTT.getPlugin().getMessage("commandhandler.lobbylocationcurrentworld"));
			return;
		}

		if (args.length == 4) {
			String worldname = args[0];
			World world = Bukkit.getWorld(worldname);
			if (world == null) {
				sendMessage(player, MineTTT.getPlugin().getMessage("commandhandler.lobbylocationworld"));
			}

			double x = Double.parseDouble(args[1]);
			double y = Double.parseDouble(args[2]);
			double z = Double.parseDouble(args[3]);

			Location location = new Location(world, x, y, z);
			this.plugin.thread.setLobbyLocation(location);
			sendMessage(player, MineTTT.getPlugin().getMessage("commandhandler.lobbylocationset"));
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
			sendMessage(player, MineTTT.getPlugin().getMessage("commandhandler.rank"));
			return;
		}

		TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(args[0]);

		Rank rank = Rank.getRank(args[1]);
		if (rank == null) {
			sendMessage(player, MineTTT.getPlugin().getMessage("commandhandler.rankinvalid"));
			return;
		}

		Tplayer.setRank(rank);
		if (rank == Rank.NONE) {
			sendMessage(player, String.format(MineTTT.getPlugin().getMessage("commandhandler.rankcleared"), args[0]));
		} else {
			sendMessage(player, String.format(MineTTT.getPlugin().getMessage("commandhandler.rankset"), args[0], rank));
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
					sendMessage(player, String.format(MineTTT.getPlugin().getMessage("commandhandler.teleporttogoto"), location));
					return;
				}
				player.teleport(loc);
				sendMessage(player, MineTTT.getPlugin().getMessage("commandhandler.teleporttolobbyplayer"));
				return;
			}

			if (players.isEmpty()) {
				sendMessage(player, MineTTT.getPlugin().getMessage("commandhandler.teleporttonames"));
				return;
			}

			sendMessage(player, String.format(MineTTT.getPlugin().getMessage("commandhandler.teleporttolobby"), playerlist));
			
		} else {
			loc = this.plugin.thread.getArenaLocation(location);
			if (loc == null) {
				sendMessage(player, MineTTT.getPlugin().getMessage("commandhandler.teleporttodestination"));
				return;
			}
		}

		if (args.length == 1) {
			if (player == null) {
				sendMessage(player, String.format(MineTTT.getPlugin().getMessage("commandhandler.teleportto"), location));
				return;
			}
			player.teleport(loc);
			sendMessage(player, String.format(MineTTT.getPlugin().getMessage("commandhandler.teleporttoarena"), location));
			return;
		}

		if (players.isEmpty()) {
			sendMessage(player, MineTTT.getPlugin().getMessage("commandhandler.teleporttoonline"));
			return;
		}

		sendMessage(player, String.format(MineTTT.getPlugin().getMessage("commandhandler.teleporttoplayers"), location, playerlist));
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
			sendMessage(player, MineTTT.getPlugin().getMessage("commandhandler.teleporttowich"));
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
			sendMessage(player, MineTTT.getPlugin().getMessage("commandhandler.update"));
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
