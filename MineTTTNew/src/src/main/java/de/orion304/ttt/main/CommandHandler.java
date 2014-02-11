package src.main.java.de.orion304.ttt.main;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import src.main.java.de.orion304.ttt.players.TTTPlayer;

public class CommandHandler {

	MineTTT plugin;

	public CommandHandler(MineTTT instance) {
		this.plugin = instance;
	}

	private void forceEnd(Player player, String[] args) {
		this.plugin.thread.endGame(true);
	}

	private void forceStart(Player player, String[] args) {
		this.plugin.thread.startPreparations();
	}

	private void getStatus(Player player, String[] args) {
		for (Player player1 : Bukkit.getOnlinePlayers()) {
			for (Player player2 : Bukkit.getOnlinePlayers()) {
				Tools.verbose(player1.getName() + " can see "
						+ player2.getName() + ": " + player1.canSee(player2));
			}
		}
		sendMessage(player, "The status of MineTTT is: "
				+ this.plugin.thread.getGameStatus().toString());
	}

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
		}
		return false;
	}

	private void sendMessage(Player player, String message) {
		if (player == null) {
			Tools.verbose(message);
		} else {
			player.sendMessage(message);
		}
	}

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

	private void shop(Player player) {
		Tools.verbose("Openning shop");
		TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(player);
		Tplayer.openShop();
	}

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

	private void teleportToArena(Player player, String[] args) {
		if (args.length == 0) {
			sendMessage(player, "You must specific which arena.");
			return;
		}
		teleportTo(player, args, args[0]);
	}

	private void teleportToLobby(Player player, String[] args) {
		teleportTo(player, args, "Lobby");

	}

	private void update(Player player, String[] args) {
		if (player == null) {
			sendMessage(player, "This command cannot be used from the console.");
			return;
		}
		String username = player.getName();

		TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(username);
		Tplayer.loadMinecadeAccount();
		Tplayer.refreshScoreboard();
	}

}
