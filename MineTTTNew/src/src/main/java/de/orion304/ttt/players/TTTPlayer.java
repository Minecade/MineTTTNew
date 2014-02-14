package src.main.java.de.orion304.ttt.players;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import src.main.java.de.orion304.ttt.listeners.PlayerListener;
import src.main.java.de.orion304.ttt.main.Action;
import src.main.java.de.orion304.ttt.main.Claymore;
import src.main.java.de.orion304.ttt.main.FileManager;
import src.main.java.de.orion304.ttt.main.GameState;
import src.main.java.de.orion304.ttt.main.MineTTT;
import src.main.java.de.orion304.ttt.main.Tools;
import src.main.java.de.orion304.ttt.minecade.MinecadeAccount;

public class TTTPlayer {

	public class VoteInfo {
		public int voteCount = 0;
		public OfflinePlayer display;

		public VoteInfo(OfflinePlayer display) {
			this.display = display;
		}
	}

	private static MineTTT plugin;

	private static ConcurrentHashMap<String, TTTPlayer> players = new ConcurrentHashMap<>();
	private static int karmaThreshold = FileManager.karmaThreshold;
	private static long duration = FileManager.banDuration;
	private static long callCooldown = FileManager.chatItemCooldown;
	private static long speedBoostCooldown = 30 * 1000L;

	private static Random random = new Random();

	private static OfflinePlayer standardDetectiveLabel = Bukkit
			.getOfflinePlayer(FileManager.detectiveColor + "Detectives:"),
			standardTraitorLabel = Bukkit
					.getOfflinePlayer(FileManager.traitorColor + "Traitors:"),
			standardInnocentLabel = Bukkit
					.getOfflinePlayer(FileManager.innocentColor + "Innocents:");

	private static final String bold = ChatColor.BOLD.toString();

	private static OfflinePlayer boldDetectiveLabel = Bukkit
			.getOfflinePlayer(FileManager.detectiveColor + bold + "Detectives:"),
			boldTraitorLabel = Bukkit.getOfflinePlayer(FileManager.traitorColor
					+ bold + "Traitors:"), boldInnocentLabel = Bukkit
					.getOfflinePlayer(FileManager.innocentColor + bold
							+ "Innocents:");
	private static final OfflinePlayer karmaLabel = Bukkit
			.getOfflinePlayer(ChatColor.GREEN + "Karma");

	private static final OfflinePlayer butterCoinsLabel = Bukkit
			.getOfflinePlayer(ChatColor.GOLD + "Tokens");

	public static final String trustLabel = "Proclaim your trust",
			suspectLabel = "Express your suspiscion",
			claimLabel = "Call out a traitor";

	private static final String voteForAMap = "Vote for a map...";
	private static final String spectateGame = "Spectate this game";
	private static final String playGame = "Play this game";

	private static ItemStack trustItem, suspectItem, claimItem;

	private static ConcurrentHashMap<String, VoteInfo> votes = new ConcurrentHashMap<>();

	/**
	 * Makes all players register this player to their scoreboards.
	 * 
	 * @param player
	 *            The player to register.
	 */
	public static void allRegisterPlayer(Player player) {
		allRegisterPlayer(getTTTPlayer(player));
	}

	/**
	 * Makes all players register this TTTPlayer to their scoreboards.
	 * 
	 * @param player
	 *            The TTTPlayer to register.
	 */
	public static void allRegisterPlayer(TTTPlayer player) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(p);
			Tplayer.registerPlayer(player);
		}
	}

	/**
	 * Deals karma to players when the game ends.
	 */
	public static void dealKarma() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			if (Tplayer.team != PlayerTeam.NONE) {
				Tplayer.addKarma();
			}
		}
	}

	/**
	 * Distributes butter coins to all players, converting their nuggets to
	 * coins.
	 */
	public static void distributeCoinsToAll() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			Tplayer.distributeCoins();
		}
	}

	/**
	 * Get the name of the player, to be read by the receiver, including
	 * team-specific colors if applicable.
	 * 
	 * @param player
	 *            The player to find the color-coded name of.
	 * @param receiver
	 *            The player who will be seeing this name.
	 * @return The properly colored name of the player
	 */
	private static String getDisplayedName(TTTPlayer player, TTTPlayer receiver) {
		GameState state = plugin.thread.getGameStatus();

		Player p = player.getPlayer();
		Player r = receiver.getPlayer();

		if (p == null || r == null) {
			return player.playerName;
		}

		PlayerTeam team = player.team;
		PlayerTeam receiverTeam = receiver.team;

		String playerName = p.getName();

		if (state != GameState.GAME_RUNNING) {
			return playerName;
		}

		if (team == PlayerTeam.NONE) {
			if (receiverTeam == PlayerTeam.NONE) {
				playerName = FileManager.spectatorColor + "<Spectator> "
						+ playerName + ChatColor.RESET;
				return playerName;
			}
		}

		if (team == PlayerTeam.TRAITOR
				&& (receiverTeam == PlayerTeam.NONE || receiverTeam == PlayerTeam.TRAITOR)) {
			playerName = FileManager.traitorColor + playerName
					+ ChatColor.RESET;
			return playerName;
		}

		if (team == PlayerTeam.DETECTIVE) {
			playerName = FileManager.detectiveColor + playerName
					+ ChatColor.RESET;
			return playerName;
		}

		return playerName;
	}

	/**
	 * Gets the number of detectives.
	 * 
	 * @return The number of detectives.
	 */
	public static int getNumberOfDetectives() {
		return getNumberOfPlayers(PlayerTeam.DETECTIVE);
	}

	/**
	 * Gets the number of innocents.
	 * 
	 * @return The number of innocents.
	 */
	public static int getNumberOfInnocents() {
		return getNumberOfPlayers(PlayerTeam.INNOCENT);
	}

	/**
	 * Gets the number of players on the specified team.
	 * 
	 * @param team
	 *            The team to check for.
	 * @return The number of players on that team.
	 */
	private static int getNumberOfPlayers(PlayerTeam team) {
		int i = 0;
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			if (Tplayer.team == team) {
				i++;
			}
		}
		return i;
	}

	/**
	 * Gets the number of traitors.
	 * 
	 * @return The number of traitors
	 */
	public static int getNumberOfTraitors() {
		return getNumberOfPlayers(PlayerTeam.TRAITOR);
	}

	/**
	 * Returns the collection of TTTPlayers.
	 * 
	 * @return
	 */
	public static Collection<TTTPlayer> getPlayers() {
		return players.values();
	}

	/**
	 * Gets the collection of players who are spectating.
	 * 
	 * @return The spectators.
	 */
	public static Collection<Player> getSpectators() {
		List<Player> players = new ArrayList<>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			if (Tplayer.isSpectating()) {
				players.add(player);
			}
		}
		return players;
	}

	/**
	 * Gets the TTTPlayer associated with this Player.
	 * 
	 * @param player
	 *            The player to look for.
	 * @return The TTTPlayer.
	 */
	public static TTTPlayer getTTTPlayer(Player player) {
		if (player == null) {
			return null;
		}
		String name = player.getName();
		return getTTTPlayer(name);
	}

	/**
	 * Gets the TTTPlayer associated with this player name.
	 * 
	 * @param playerName
	 *            The name of the player to look for.
	 * @return The TTTPlayer.
	 */
	public static TTTPlayer getTTTPlayer(String playerName) {
		if (players.containsKey(playerName)) {
			return players.get(playerName);
		}
		return new TTTPlayer(playerName);
	}

	/**
	 * Gets the list of voting items, with their proper lore and all.
	 * 
	 * @return An ArrayList of ItemStacks which represent the maps players will
	 *         vote for.
	 */
	private static ArrayList<ItemStack> getVotingItems() {
		ArrayList<ItemStack> items = new ArrayList<>();
		int i = 0;
		for (String key : plugin.thread.getArenaLocations().keySet()) {
			ItemStack stack = new ItemStack(Material.DIAMOND, 1);
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName("Vote for " + key);
			List<String> lore = plugin.thread.getArenaLore(key);
			meta.setLore(lore);
			stack.setItemMeta(meta);
			items.add(i, stack);
			i++;
		}
		return items;
	}

	/**
	 * Goes through the votes and finds the location that won. NOTE: Unlike a
	 * simple majority system, the vote tally is weighted. If Map A has 3 votes
	 * and Map B has 1 vote, and that is all, then there is a 3:1 chance that
	 * Map A will be chosen over Map B.
	 * 
	 * @return The winning arena location.
	 */
	public static Location getWinningLocation() {
		ConcurrentHashMap<String, Location> locations = plugin.thread
				.getArenaLocations();
		Random random = new Random();
		if (locations.isEmpty()) {
			return null;
		}
		if (votes.isEmpty()) {
			int size = locations.size();
			int choice = random.nextInt(size);
			String[] choices = locations.keySet().toArray(new String[size]);
			return locations.get(choices[choice]);
		}
		ArrayList<Location> choices = new ArrayList<>();
		for (String key : votes.keySet()) {
			VoteInfo info = votes.get(key);
			Location location = locations.get(key);
			if (location == null) {
				continue;
			}
			int i = 1;
			while (i <= info.voteCount) {
				choices.add(location);
				i++;
			}
		}
		int choice = random.nextInt(choices.size());
		return choices.get(choice);

	}

	/**
	 * Gives all players the chat items.
	 */
	public static void giveChatItemsToAll() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			Tplayer.giveChatItems();
		}
	}

	/**
	 * Handles a player placing a block - used only for Claymores.
	 * 
	 * @param player
	 *            The player placing the block.
	 * @param block
	 *            The block that was placed to.
	 */
	public static void handleBlockPlace(Player player, Block block) {
		ItemStack item = player.getItemInHand();
		if (item == null) {
			return;
		}
		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			return;
		}
		String name = meta.getDisplayName();
		if (name.equalsIgnoreCase("Claymore")) {
			PlayerInventory inventory = player.getInventory();
			inventory.clear(inventory.getHeldItemSlot());
			getTTTPlayer(player).placeClaymore(block);
		}
	}

	/**
	 * Handles a player dying. It distributes coins, checks game conditions, and
	 * updates all other players' scoreboards.
	 * 
	 * @param player
	 *            The player that died.
	 */
	public static void handleDeath(Player player) {
		player.setGameMode(GameMode.ADVENTURE);
		TTTPlayer Tplayer = getTTTPlayer(player);
		Tplayer.team = PlayerTeam.NONE;
		if (plugin.thread.isGameRunning()) {
			Tplayer.distributeCoins();
		}
		if (plugin.teamHandler.isGameOver()) {
			plugin.thread.endGame(false);
		} else {
			Tools.clearInventory(player);
			showAllGameScoreboards();
		}
	}

	/**
	 * Handles the interact action. Used for voting, chat items, and opening the
	 * traitor shop.
	 * 
	 * @param player
	 *            The player who interacted.
	 * @return True if the event needs to be canceled.
	 */
	public static boolean handleInteract(Player player) {
		ItemStack item = player.getItemInHand();
		GameState state = plugin.thread.getGameStatus();
		TTTPlayer Tplayer = getTTTPlayer(player);
		switch (state) {
		case OFF:
			break;
		case GAME_PREPARING:
			return Tplayer.vote(item);
		case GAME_RUNNING:
			if (Tplayer.getTeam() == PlayerTeam.NONE) {
				return true;
			}

			if (item != null) {
				if (item.getType() == Material.GOLD_NUGGET) {
					Tplayer.openShop();
				}
				ItemMeta meta = item.getItemMeta();
				if (meta != null) {
					String name = item.getItemMeta().getDisplayName();
					if (name != null) {
						Player target = Tools.getTargetPlayer(player, 20);
						if (target != null) {
							switch (name) {
							case trustLabel:
								Tplayer.callOut(Action.TRUST, target);
								return true;
							case suspectLabel:
								Tplayer.callOut(Action.SUSPISCION, target);
								return true;
							case claimLabel:
								Tplayer.callOut(Action.CLAIM, target);
								return true;
							default:
								break;
							}
						}
					}
				}
			}

			if (Tplayer.getTeam() == PlayerTeam.DETECTIVE) {
				Player target = Tools.getKiller(player);
				if (target != null) {
					plugin.thread.findKiller(player, target);
				}
			}
			break;
		}
		return false;
	}

	/**
	 * Handles a player clicking on an inventory slot. This is for voting and
	 * shopping.
	 * 
	 * @param player
	 *            The player that clicked the inventory.
	 * @param item
	 *            The item they clicked on.
	 * @param slotType
	 *            The slot type of the inventory that was clicked.
	 * @return True if the event needs to be canceled.
	 */
	public static boolean handleInventoryClick(Player player, ItemStack item,
			SlotType slotType) {
		GameState state = plugin.thread.getGameStatus();
		TTTPlayer Tplayer = getTTTPlayer(player);
		switch (state) {
		case OFF:
			break;
		case GAME_PREPARING:
			return Tplayer.vote(item);
		case GAME_RUNNING:
			if (Tplayer.getTeam() == PlayerTeam.NONE) {
				return true;
			}
			if (slotType == SlotType.CONTAINER) {
				return Tplayer.handleShopClick(item);
			}
		}
		return false;
	}

	/**
	 * Handles a player picking up an item. Mostly simply cancels the event
	 * unless the game is off.
	 * 
	 * @param player
	 *            The player who picked up the item.
	 * @return True if the event needs to be canceled.
	 */
	public static boolean handleItemPickup(Player player) {
		GameState state = plugin.thread.getGameStatus();
		TTTPlayer Tplayer = getTTTPlayer(player);
		switch (state) {
		case OFF:
			break;
		case GAME_PREPARING:
			if (!Tplayer.hasVoted) {
				return true;
			}
			break;
		case GAME_RUNNING:
			if (Tplayer.team == PlayerTeam.NONE) {
				return true;
			}
			break;
		}
		return false;
	}

	/**
	 * Handles a player joining the game. It shows their relevant scoreboard,
	 * teleports them to the lobby, sets their game mode, and loads their
	 * minecade account.If the game is off, checks if there are enough players
	 * to start the game and starts it. If there are not enough players,
	 * broadcast how many more players are necessary. If the game is going is
	 * preparing, it gives the player their voting and spectating tools. If the
	 * game is in progress, it forces the player to not be in the game, and then
	 * the main thread will handle spectating if necessary.
	 * 
	 * @param player
	 *            That player that joined.
	 */
	public static void handleJoin(Player player) {
		if (player == null) {
			return;
		}
		player.teleport(plugin.thread.getLobbyLocation());
		GameState state = plugin.thread.getGameStatus();
		TTTPlayer Tplayer = getTTTPlayer(player);
		Tplayer.loadMinecadeAccount();
		Tplayer.team = PlayerTeam.NONE;
		player.setGameMode(GameMode.ADVENTURE);
		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		switch (state) {
		case OFF:
			Tplayer.showPreGameScoreboard();
			int need = plugin.thread.playerThreshold
					- Bukkit.getOnlinePlayers().length;
			ChatColor color = ChatColor.AQUA;
			if (need == 1) {
				Bukkit.broadcastMessage(color
						+ "1 more player must join before the game can start.");
			} else if (need > 1) {
				Bukkit.broadcastMessage(color.toString() + need
						+ " more players must join before the game can start.");
			}
			break;
		case GAME_PREPARING:
			Tplayer.showPrepScoreboard();
			Tplayer.installVoteTools();
			break;
		case GAME_RUNNING:
			allRegisterPlayer(player);
			Tplayer.showGameScoreboard();
			Claymore.showClaymores(Tplayer);
			break;
		}
	}

	/**
	 * Handles a player leaving the server. If the game is running, it updates
	 * the players' scoreboards and ends the game if necessary.
	 * 
	 * @param player
	 *            The player that left.
	 */
	public static void handleLeave(Player player) {
		TTTPlayer Tplayer = getTTTPlayer(player);
		Tplayer.team = PlayerTeam.NONE;
		player.setGameMode(GameMode.ADVENTURE);
		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		Tplayer.resetPlayer();
		if (plugin.thread.isGameRunning()) {
			if (plugin.teamHandler.isGameOver()) {
				plugin.thread.endGame(false);
			} else {
				showAllGameScoreboards();
			}
		}
	}

	/**
	 * Give all players their voting implements.
	 */
	public static void installAllVoteTools() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			Tplayer.installVoteTools();
		}
	}

	/**
	 * Checks to see if a player is banned from low karma.
	 * 
	 * @param name
	 *            The name of the player.
	 * @return True if the player is karma-banned.
	 */
	public static boolean isBanned(String name) {
		if (players.containsKey(name)) {
			TTTPlayer player = players.get(name);
			return player.isBanned();
		}
		return false;
	}

	/**
	 * Loads the colors of the teams.
	 */
	public static void loadColors() {
		standardDetectiveLabel = Bukkit
				.getOfflinePlayer(FileManager.detectiveColor + "Detectives:");
		standardTraitorLabel = Bukkit.getOfflinePlayer(FileManager.traitorColor
				+ "Traitors:");
		standardInnocentLabel = Bukkit
				.getOfflinePlayer(FileManager.innocentColor + "Innocents:");
		boldDetectiveLabel = Bukkit.getOfflinePlayer(FileManager.detectiveColor
				+ bold + "Detectives:");
		boldTraitorLabel = Bukkit.getOfflinePlayer(FileManager.traitorColor
				+ bold + "Traitors:");
		boldInnocentLabel = Bukkit.getOfflinePlayer(FileManager.innocentColor
				+ bold + "Innocents:");
	}

	/**
	 * Gives all players new scoreboards.
	 */
	public static void newScoreboards() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			Tplayer.newScoreboard();
		}
	}

	/**
	 * Makes all players register each other player.
	 */
	public static void registerAllScoreboards() {
		Player[] players = Bukkit.getOnlinePlayers();
		for (Player player1 : players) {
			for (Player player2 : players) {
				TTTPlayer Tplayer1 = getTTTPlayer(player1);
				if (Tplayer1 != null) {
					Tplayer1.registerPlayer(player2);
				}
			}
		}
	}

	/**
	 * Resets all players and votes.
	 */
	public static void reset() {
		for (TTTPlayer player : players.values()) {
			player.resetPlayer();
		}
		votes.clear();
	}

	/**
	 * Resets all player scoreboards.
	 */
	public static void resetScoreboards() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			Tplayer.resetScoreboard();
		}
	}

	/**
	 * Sets the level of all players - used in countdown timers.
	 * 
	 * @param level
	 *            The new level.
	 */
	public static void setAllLevel(int level) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.setExp(0);
			player.setLevel(level);
		}
	}

	/**
	 * Sets the plugin which is running the game.
	 * 
	 * @param instance
	 *            The MineTTT instance.
	 */
	public static void setPlugin(MineTTT instance) {
		plugin = instance;

		ItemMeta meta;

		if (trustItem == null) {
			trustItem = new ItemStack(Material.RED_ROSE, 1);
			meta = trustItem.getItemMeta();
			meta.setDisplayName(trustLabel);
			trustItem.setItemMeta(meta);
		}

		if (suspectItem == null) {
			suspectItem = new ItemStack(Material.BLAZE_POWDER, 1);
			meta = suspectItem.getItemMeta();
			meta.setDisplayName(suspectLabel);
			suspectItem.setItemMeta(meta);
		}

		if (claimItem == null) {
			claimItem = new ItemStack(Material.REDSTONE_TORCH_ON, 1);
			meta = claimItem.getItemMeta();
			meta.setDisplayName(claimLabel);
			claimItem.setItemMeta(meta);
		}

		karmaThreshold = FileManager.karmaThreshold;
		duration = FileManager.banDuration;
		callCooldown = FileManager.chatItemCooldown;
		speedBoostCooldown = 30 * 1000L;
	}

	/**
	 * Updates all players' scoreboards to have the number of detectives,
	 * traitors and innocents on them.
	 */
	public static void showAllGameScoreboards() {

		int detectives = TTTPlayer.getNumberOfDetectives();
		int traitors = TTTPlayer.getNumberOfTraitors();
		int innocents = TTTPlayer.getNumberOfInnocents();

		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			Tplayer.showGameScoreboard(detectives, traitors, innocents);
		}
	}

	/**
	 * Show all players the pre-game scoreboard (their stats).
	 */
	public static void showAllPreGameScoreboards() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			Tplayer.showPreGameScoreboard();
		}
	}

	/**
	 * Shows all players the preparation scoreboards (the map vote).
	 */
	public static void showAllPrepScoreboards() {
		HashMap<OfflinePlayer, Integer> numberOfVotes = new HashMap<>();
		for (String key : votes.keySet()) {
			VoteInfo info = votes.get(key);
			if (info.voteCount > 0) {
				numberOfVotes.put(info.display, info.voteCount);
			}
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			Tplayer.showPrepScoreboard(numberOfVotes);
		}
	}

	/**
	 * Shows the hidden player to the toShow player.
	 * 
	 * @param hidden
	 *            The player that is hidden.
	 * @param toShow
	 *            The player to reveal the hidden player to.
	 */
	public static void showPlayer(Player hidden, Player toShow) {
		if (hidden.equals(toShow)) {
			return;
		}
		if (!toShow.canSee(hidden)) {
			toShow.showPlayer(hidden);
		}
	}

	private final String playerName;

	private PlayerTeam team = PlayerTeam.NONE;

	private Player recentDamager = null;

	private Player trackedPlayer = null;

	private Scoreboard scoreboard = Bukkit.getScoreboardManager()
			.getNewScoreboard();

	private final Map<PlayerTeam, Team> teams = new HashMap<>();

	private int karma = 1000;

	private long banDate = 0, banLength = 0;

	private transient boolean hasVoted = false;

	public MinecadeAccount account;

	private final ConcurrentHashMap<String, Long> calls = new ConcurrentHashMap<>();

	private long lastSpeedBoostTime = 0;

	private final List<SpecialItem> traitorShop = new ArrayList<>();

	private final List<SpecialItem> ownedItems = new ArrayList<>();

	private boolean spectating = false;

	/**
	 * Creats a new TTTPlayer with this player's name.
	 * 
	 * @param name
	 *            The player's name.
	 */
	public TTTPlayer(String name) {
		this.playerName = name;
		players.put(this.playerName, this);
	}

	/**
	 * Creates a new TTTPlayer with this name, karma, ban data and ban length.
	 * 
	 * @param name
	 *            The player name.
	 * @param karma
	 *            The player's karma.
	 * @param banDate
	 *            The ban date of the player.
	 * @param banLength
	 *            The ban length of the player.
	 */
	public TTTPlayer(String name, int karma, long banDate, long banLength) {
		this(name);
		this.karma = karma;
		this.banDate = banDate;
		this.banLength = banLength;
	}

	/**
	 * Adds karma to the player, a random number between 40 and 60. (Karma caps
	 * at 1000).
	 */
	public void addKarma() {
		addKarma((int) (Math.random() * 20 + 40));
	}

	/**
	 * Adds karma to the player. (Karma is always between 0 and 1000).
	 * 
	 * @param value
	 *            The amount of karma to add.
	 */
	public void addKarma(int value) {
		this.karma += value;
		if (this.karma > 1000) {
			this.karma = 1000;
		}
		if (this.karma < 0) {
			this.karma = 0;
		}
		showKarma();
	}

	/**
	 * Bans the player for the duration.
	 * 
	 * @param duration
	 *            The duration (in ms) of the ban.
	 */
	public void ban(long duration) {
		this.banDate = System.currentTimeMillis();
		this.banLength = duration;
	}

	/**
	 * Calls out a chat action.
	 * 
	 * @param action
	 *            The action to call out.
	 * @param target
	 *            The target to call out about.
	 */
	private void callOut(Action action, Player target) {
		Player me = getPlayer();
		if (me == null) {
			return;
		}
		long lastTime = 0;
		if (this.calls.containsKey(target.getName())) {
			lastTime = this.calls.get(target.getName());
		}

		long time = System.currentTimeMillis();
		if (time < lastTime + callCooldown) {
			return;
		}

		this.calls.put(target.getName(), time);

		TTTPlayer Ttarget = getTTTPlayer(target);

		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			String myName = getDisplayedName(this, Tplayer);
			String targetName = getDisplayedName(Ttarget, Tplayer);

			String message;

			switch (action) {
			case TRUST:
				message = myName + " trusts " + targetName + ".";
				break;
			case SUSPISCION:
				message = myName + " suspects " + targetName + " of treachery.";
				break;
			case CLAIM:
				message = myName + " claims that " + targetName
						+ " is a traitor!";
				break;
			default:
				message = "";
				break;
			}

			player.sendMessage(message);

		}
	}

	/**
	 * Gets whether the player can spectate or not.
	 * 
	 * @return True if the player can spectate.
	 */
	public boolean canSpectate() {
		if (this.account.isVip() || this.account.isAdmin()
				|| this.account.isCm() || this.account.isGm()) {
			return true;
		}
		return false;
	}

	/**
	 * Checks the karma of the player, and bans if it falls below 200.
	 */
	public void checkKarma() {
		Player p = getPlayer();
		if (p == null) {
			return;
		}
		if (this.karma < karmaThreshold) {
			p.kickPlayer("Your karma has dropped below 200! You've been banned!");
			setKarma(500);
			this.banDate = System.currentTimeMillis();
			this.banLength = duration;
		}
	}

	/**
	 * Distributes coins to the player, changing nuggets into butter coins.
	 */
	public void distributeCoins() {
		Player player = getPlayer();
		if (player == null) {
			return;
		}
		long coins = 0;
		ArrayList<ItemStack> toRemove = new ArrayList<>();
		for (ItemStack item : player.getInventory().getContents()) {
			if (item == null) {
				continue;
			}
			if (item.getType() == Material.GOLD_NUGGET) {
				coins += item.getAmount();
				toRemove.add(item);
			}
		}
		player.getInventory().removeItem(
				toRemove.toArray(new ItemStack[toRemove.size()]));
		plugin.minecade.addCoins(this.playerName, coins);
		loadMinecadeAccount();
		refreshScoreboard();
	}

	/**
	 * Gets the date (system time in ms) that the player was banned at.
	 * 
	 * @return Returns the ban date, 0 if the player has never been banned.
	 */
	public long getBanDate() {
		return this.banDate;
	}

	/**
	 * Gets the length of the ban, 0 if the player has never been banned.
	 * 
	 * @return The length of the ban (in ms).
	 */
	public long getBanLength() {
		return this.banLength;
	}

	/**
	 * Called in the listener, this gets the damage done by a player. Unless the
	 * player is a traitor and using a traitor item, nothing happens. Otherwise,
	 * this handles one-hit-kill damages.
	 * 
	 * @param player
	 *            The player which dealt the damage.
	 * @param damage
	 *            The damage dealt.
	 * @param cause
	 *            The cause of the damage.
	 * @return The changed or unchanged damage value.
	 */
	public double getDamage(Player player, double damage, DamageCause cause) {
		Player me = getPlayer();
		if (me == null) {
			return 0;
		}
		// Damageable d = (Damageable) player;
		double maxdamage = Double.MAX_VALUE;
		ItemStack itemInHand = me.getItemInHand();
		if (itemInHand == null) {
			return damage;
		}
		if (itemInHand.getItemMeta() == null) {
			return damage;
		}
		for (SpecialItem specialItem : this.ownedItems) {
			if (specialItem.getDisplayName().equals(
					itemInHand.getItemMeta().getDisplayName())) {
				if (!specialItem.isCauseApplicable(cause)) {
					continue;
				}

				if (specialItem.getUses() == 0) {
					me.getInventory()
							.clear(me.getInventory().getHeldItemSlot());
					this.ownedItems.remove(specialItem);
					return 0;
				}
				Power power = specialItem.getPower();
				boolean use = false;
				switch (power) {
				case ONE_HIT_KILL:
					damage = maxdamage;
					use = true;
					break;
				case ONE_HIT_KILL_FROM_BEHIND:
					if (Tools.isBehind(me, player)) {
						damage = maxdamage;
						use = true;
					} else {
						damage = 0;
					}
					break;
				default:
					damage = 0;
					break;
				}

				if (use) {
					specialItem.use();
				}
				if (specialItem.getUses() == 0) {

					me.getInventory()
							.clear(me.getInventory().getHeldItemSlot());
					this.ownedItems.remove(specialItem);
				}
				return damage;
			}
		}
		return damage;
	}

	/**
	 * Gets the karma of the player (a value between 0 and 1000).
	 * 
	 * @return The karma of the player.
	 */
	public int getKarma() {
		return this.karma;
	}

	/**
	 * Gets the smallest multiple of nine which is larger than the integer.
	 * 
	 * @param integer
	 *            The integer to check.
	 * @return A multiple of 9.
	 */
	private int getMultipleOfNine(int integer) {
		return 9 * (int) Math.ceil(integer / 9.0);
	}

	/**
	 * Gets the name of the player.
	 * 
	 * @return The player name.
	 */
	public String getName() {
		return this.playerName;
	}

	/**
	 * Gets the player.
	 * 
	 * @return The player by this name, or null if there is no player by this
	 *         name online.
	 */
	public Player getPlayer() {
		return Bukkit.getPlayer(this.playerName);
	}

	/**
	 * Gets the player which most recently damaged this player.
	 * 
	 * @return The player that most recently damaged this player.
	 */
	public Player getRecentDamager() {
		return this.recentDamager;
	}

	/**
	 * Gets the team this player is on.
	 * 
	 * @return The team.
	 */
	public PlayerTeam getTeam() {
		return this.team;
	}

	/**
	 * Gets the player being tracked by this player.
	 * 
	 * @return The player being tracked.
	 */
	public Player getTrackedPlayer() {
		return this.trackedPlayer;
	}

	/**
	 * Gives the chat items to this player.
	 */
	private void giveChatItems() {
		Player player = getPlayer();
		if (player == null) {
			return;
		}
		Inventory inventory = player.getInventory();
		if (this.team != PlayerTeam.NONE) {
			inventory.setItem(5, trustItem);
			inventory.setItem(6, suspectItem);
			inventory.setItem(7, claimItem);
		}
		player.updateInventory();
	}

	/**
	 * Gives the player a speed boost unless the player has had one recently.
	 * 
	 * @param amplitude
	 *            The amplitude of the speed boost
	 * 
	 * @param duration
	 *            The duration of the boost, in seconds
	 */
	public void giveSpeedBoost(int amplitude, int duration) {
		Player player = getPlayer();
		if (player == null) {
			return;
		}
		long time = System.currentTimeMillis();
		if (time < this.lastSpeedBoostTime + speedBoostCooldown) {
			return;
		}

		this.lastSpeedBoostTime = time;
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
				duration * 20, amplitude));
	}

	/**
	 * Handles the player clicking the item in a shop.
	 * 
	 * @param item
	 *            The item the player clicked on.
	 * @return True if the event needs to be canceled.
	 */
	private boolean handleShopClick(ItemStack item) {
		if (item == null) {
			return false;
		}
		if (item.getItemMeta() == null) {
			return false;
		}

		Player p = getPlayer();
		if (p == null) {
			return false;
		}

		if (item.getType() == Material.GOLD_NUGGET) {
			openShop();
			return true;
		}

		Inventory inventory = p.getInventory();
		InventoryView view = p.getOpenInventory();
		if (view.getType() != InventoryType.CHEST) {
			return false;
		}
		if (item.getItemMeta() == null) {
			return false;
		}
		for (SpecialItem shopItem : this.traitorShop) {
			if (item.getItemMeta().getDisplayName()
					.equals(shopItem.getDisplayName())) {
				int cost = shopItem.getCost();
				if (!inventory.containsAtLeast(PlayerListener.nugget, cost)) {
					p.sendMessage(ChatColor.RED
							+ "You cannot afford this item!");
					// clearCursorItemLater(p);
					ItemStack oldItem = p.getItemOnCursor();
					p.closeInventory();
					openTraitorShop();
					p.setItemOnCursor(oldItem);
					return true;
				}
				if (shopItem.alreadyHas(inventory)) {
					p.sendMessage(ChatColor.RED
							+ "You can only own one of these items!");
					ItemStack oldItem = p.getItemOnCursor();
					p.closeInventory();
					openTraitorShop();
					p.setItemOnCursor(oldItem);
					return true;
				}
				p.sendMessage(ChatColor.GREEN + "You've purchased "
						+ ChatColor.RESET + shopItem.getDisplayName() + "!");
				spendNuggets(cost);
				SpecialItem specialItem = shopItem;
				this.ownedItems.add(specialItem);
				inventory.addItem(specialItem.getItemInInventory());
				// clearCursorItemLater(p);
				ItemStack oldItem = p.getItemOnCursor();
				p.closeInventory();
				openTraitorShop();
				p.setItemOnCursor(oldItem);
				return true;
			}
		}
		return false;
	}

	/**
	 * Hooks the player's scoreboard with the local scoreboard object.
	 */
	private void hookScoreboard() {
		Player player = getPlayer();
		if (player != null) {
			this.scoreboard = player.getScoreboard();
		}
	}

	/**
	 * Initializes the player's scoreboard - for use when a game begins.
	 */
	private void initializeScoreboard() {
		Player player = Bukkit.getPlayer(this.playerName);

		if (player != null) {
			this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

			Team detectives = this.scoreboard.registerNewTeam("Detectives");
			this.teams.put(PlayerTeam.DETECTIVE, detectives);
			detectives.setPrefix(FileManager.detectiveColor.toString());
			detectives.setSuffix(" (Detective)");

			Team traitors = this.scoreboard.registerNewTeam("Traitors");
			this.teams.put(PlayerTeam.TRAITOR, traitors);
			traitors.setPrefix(FileManager.traitorColor.toString());
			traitors.setSuffix(" (Traitor)");

			Team innocents = this.scoreboard.registerNewTeam("Innocents");
			this.teams.put(PlayerTeam.INNOCENT, innocents);
			innocents.setPrefix(FileManager.innocentColor.toString());
			innocents.setSuffix(" (Innocent)");

			Team spectators = this.scoreboard.registerNewTeam("Spectators");
			this.teams.put(PlayerTeam.NONE, spectators);
			spectators.setPrefix(FileManager.spectatorColor.toString());
			spectators.setSuffix(" (Spectator)");

			player.setScoreboard(this.scoreboard);

		}
	}

	/**
	 * Gives the player all the tools they need to vote or spectate.
	 */
	private void installVoteTools() {
		Player p = getPlayer();
		if (p == null) {
			return;
		}
		Inventory inventory = p.getInventory();
		Tools.clearInventory(p);
		ItemStack vote = new ItemStack(Material.DIAMOND, 1);
		ItemMeta voteMeta = vote.getItemMeta();
		voteMeta.setDisplayName(voteForAMap);
		List<String> lore = new ArrayList<>();
		lore.add("You cannot choose to spectate after you've voted.");
		voteMeta.setLore(lore);
		vote.setItemMeta(voteMeta);
		inventory.setItem(0, vote);

		if (canSpectate()) {
			ItemStack spectate = new ItemStack(Material.SKULL_ITEM, 1,
					(short) 3);
			ItemMeta spectateMeta = spectate.getItemMeta();
			spectateMeta.setDisplayName(spectateGame);
			spectate.setItemMeta(spectateMeta);
			inventory.setItem(1, spectate);
		}

	}

	/**
	 * Checks if the player is karma-banned.
	 * 
	 * @return True if the player is banned.
	 */
	public boolean isBanned() {
		return System.currentTimeMillis() < this.banDate + this.banLength;
	}

	/**
	 * Checks if the player is currently spectating.
	 * 
	 * @return True if the player is spectating.
	 */
	private boolean isSpectating() {
		return this.spectating;
	}

	/**
	 * Loads the MinecadeAccount for this player.
	 */
	public void loadMinecadeAccount() {
		this.account = plugin.minecade.getMinecadeAccount(this.playerName);
		Player p = getPlayer();
		if (p != null) {
			p.setOp(this.account.isAdmin());
		}
	}

	/**
	 * Makes the player lose an amount of karma between 120 and 180.
	 */
	public void loseKarma() {
		addKarma(-(int) (random.nextDouble() * 60 + 120));
	}

	/**
	 * Gives the player a new scoreboard.
	 */
	private void newScoreboard() {
		Player p = getPlayer();
		if (p == null) {
			return;
		}
		p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}

	/**
	 * Opens the shop for the player.
	 */
	public void openShop() {
		Player player = getPlayer();
		if (player == null) {
			return;
		}
		player.closeInventory();
		switch (this.team) {
		case TRAITOR:
			openTraitorShop();
			break;
		case DETECTIVE:
			break;
		case INNOCENT:
			break;
		default:
			break;
		}
	}

	/**
	 * Opens the traitor shop for the player.
	 */
	private void openTraitorShop() {
		Player p = getPlayer();
		if (p == null) {
			return;
		}

		this.traitorShop.clear();
		Inventory inventory = p.getInventory();

		ArrayList<ItemStack> traitorItems = new ArrayList<>();

		DamageCause[] causes = { DamageCause.CONTACT };
		SpecialItem theBackstaber = new SpecialItem(Material.DIAMOND_SWORD,
				"The Backstabber", 1, Power.ONE_HIT_KILL_FROM_BEHIND, causes,
				1, 0, "Instantly kills someone if used from behind.",
				"Destroyed after 1 use.");
		traitorItems.add(theBackstaber.getItemInShop(inventory));
		this.traitorShop.add(theBackstaber);

		causes = new DamageCause[] { DamageCause.PROJECTILE };
		SpecialItem theBow = new SpecialItem(Material.BOW, "The Huntsman", 1,
				Power.ONE_HIT_KILL, causes, 1, 0,
				"Fires an arrow which instantly kills its target.",
				"Destroyed after 1 use.");
		traitorItems.add(theBow.getItemInShop(inventory));
		this.traitorShop.add(theBow);

		causes = new DamageCause[] {};
		SpecialItem claymore = new SpecialItem(
				Material.TNT,
				"Claymore",
				1,
				Power.MINE,
				causes,
				1,
				0,
				"Plants a mine that explodes when someone steps on it.",
				"Only you and other traitors will see the Claymore once placed.",
				"Arms after 5 seconds.");
		traitorItems.add(claymore.getItemInShop(inventory));
		this.traitorShop.add(claymore);

		Inventory shopInventory = Bukkit.getServer().createInventory(null,
				getMultipleOfNine(traitorItems.size()), "Traitor shop");
		shopInventory.addItem(traitorItems.toArray(new ItemStack[traitorItems
				.size()]));

		p.openInventory(shopInventory);
	}

	/**
	 * Places a claymore on the block.
	 * 
	 * @param block
	 *            The block to place the claymore in.
	 */
	private void placeClaymore(Block block) {
		new Claymore(block);
	}

	/**
	 * Refreshes the scoreboard, updating its information.
	 */
	public void refreshScoreboard() {
		// Tools.verbose("Refreshing " + playerName + "'s scoreboard");
		GameState state = plugin.thread.getGameStatus();
		switch (state) {
		case OFF:
			showPreGameScoreboard();
			break;
		case GAME_PREPARING:
			showPrepScoreboard();
			break;
		case GAME_RUNNING:
			showGameScoreboard();
			break;
		}
	}

	// private void installVoteTools(List<ItemStack> items) {
	// Player p = getPlayer();
	// if (p == null)
	// return;
	// Inventory inventory = p.getInventory();
	// inventory.clear();
	// for (int i = 0; i < items.size(); i++) {
	// inventory.setItem(i, items.get(i));
	// }
	// p.updateInventory();
	// }

	/**
	 * Registers all players for this player.
	 */
	public void registerAllPlayers() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			registerPlayer(Tplayer);
		}
	}

	/**
	 * Registers player for this player.
	 * 
	 * @param player
	 *            The player to register.
	 */
	public void registerPlayer(Player player) {
		registerPlayer(getTTTPlayer(player));
	}

	/**
	 * Registers player for this player.
	 * 
	 * @param player
	 *            The TTTPlayer to register.
	 */
	public void registerPlayer(TTTPlayer player) {
		switch (this.team) {
		case NONE:
			registerPlayer(player, PlayerTeam.values());
			break;
		case DETECTIVE:
			registerPlayer(player, PlayerTeam.DETECTIVE);
			break;
		case TRAITOR:
			registerPlayer(player, PlayerTeam.TRAITOR, PlayerTeam.INNOCENT,
					PlayerTeam.DETECTIVE);
			break;
		case INNOCENT:
			registerPlayer(player, PlayerTeam.DETECTIVE);
			break;
		}
	}

	/**
	 * Registers player for this player, coloring their name if the player's
	 * team is in the allowed teams.
	 * 
	 * @param player
	 *            The TTTPlayer to register.
	 * @param allowedTeams
	 *            The teams this player is allowed to see.
	 */
	private void registerPlayer(TTTPlayer player, PlayerTeam... allowedTeams) {
		if (player == null) {
			return;
		}
		if (this.teams.isEmpty()) {
			initializeScoreboard();
		}
		PlayerTeam hisTeam = player.team;
		Player p = Bukkit.getPlayer(player.playerName);
		Player me = getPlayer();

		if (p == null || me == null) {
			return;
		}

		// Tools.verbose("Registering " + player.playerName + " (team " +
		// hisTeam
		// + ") for " + playerName + " (team " + team + ")");

		if (Arrays.asList(allowedTeams).contains(hisTeam)) {
			// Tools.verbose("Registering " + player.playerName + "(Team: "
			// + hisTeam + ") for " + playerName);
			Team hisScoreboardTeam = this.teams.get(hisTeam);
			hisScoreboardTeam.addPlayer(p);
			// Tools.verbose("Registration successful");
		}

		if (hisTeam == PlayerTeam.NONE && this.team != PlayerTeam.NONE) {
			if (me.canSee(p)) {
				me.hidePlayer(p);
			}

		} else {
			showPlayer(p, me);

		}

	}

	/**
	 * Resets all the info and scoreboard for this player.
	 */
	public void resetPlayer() {
		this.team = PlayerTeam.NONE;
		this.recentDamager = null;
		this.trackedPlayer = null;
		this.teams.clear();
		this.hasVoted = false;
		resetScoreboard();
	}

	/**
	 * Resets the scoreboard for this player.
	 */
	private void resetScoreboard() {
		Scoreboard newboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Player p = getPlayer();
		if (p != null) {
			p.setScoreboard(newboard);
		}
		this.scoreboard = newboard;
	}

	/**
	 * Sends a message to this player, if the player is online.
	 * 
	 * @param string
	 *            The message to send.
	 */
	public void sendMessage(String string) {
		Player p = getPlayer();
		if (p != null) {
			p.sendMessage(string);
		}
	}

	/**
	 * Sets the karma for this player (a value between 0 and 1000).
	 * 
	 * @param karma
	 *            The new karma value.
	 */
	public void setKarma(int karma) {
		if (karma > 1000) {
			karma = 1000;
		}
		if (karma < 0) {
			karma = 0;
		}
		this.karma = karma;
		showKarma();
	}

	/**
	 * Sets the player who most recently damaged this player.
	 * 
	 * @param player
	 *            The player who dealt the damage.
	 */
	public void setRecentDamager(Player player) {
		this.recentDamager = player;
	}

	/**
	 * Sets the team of this player.
	 * 
	 * @param team
	 *            The team to set.
	 */
	public void setTeam(PlayerTeam team) {
		this.team = team;
	}

	/**
	 * Sets the player this player is tracking.
	 * 
	 * @param player
	 *            The player to track.
	 */
	public void setTrackedPlayer(Player player) {
		this.trackedPlayer = player;
	}

	/**
	 * Shows the scoreboard for the player during a game.
	 */
	private void showGameScoreboard() {
		int detectives = TTTPlayer.getNumberOfDetectives();
		int traitors = TTTPlayer.getNumberOfTraitors();
		int innocents = TTTPlayer.getNumberOfInnocents();

		showGameScoreboard(detectives, traitors, innocents);
	}

	/**
	 * Shows the scoreboard for a player in a game, with the number of
	 * detectives, innocents and traitors in it.
	 * 
	 * @param detectives
	 *            The number of detectives.
	 * @param traitors
	 *            The number of traitors.
	 * @param innocents
	 *            The number of innocents.
	 */
	private void showGameScoreboard(int detectives, int traitors, int innocents) {
		hookScoreboard();
		Objective objective = this.scoreboard.getObjective("livingPlayers");
		if (objective == null) {
			objective = this.scoreboard.registerNewObjective("livingPlayers",
					"dummy");
		}

		if (!objective.getDisplayName().equals(
				ChatColor.GREEN + "Living Players")) {
			objective.setDisplayName(ChatColor.GREEN + "Living Players");
		}

		if (objective.getDisplaySlot() != DisplaySlot.SIDEBAR) {
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		}

		Score detectiveCount = objective.getScore(standardDetectiveLabel);
		Score traitorCount = objective.getScore(standardTraitorLabel);
		Score innocentCount = objective.getScore(standardInnocentLabel);

		switch (this.team) {
		case DETECTIVE:
			this.scoreboard.resetScores(standardDetectiveLabel);
			detectiveCount = objective.getScore(boldDetectiveLabel);
			break;
		case TRAITOR:
			this.scoreboard.resetScores(standardTraitorLabel);
			traitorCount = objective.getScore(boldTraitorLabel);
			break;
		case INNOCENT:
			this.scoreboard.resetScores(standardInnocentLabel);
			innocentCount = objective.getScore(boldInnocentLabel);
			break;
		default:
			this.scoreboard.resetScores(boldDetectiveLabel);
			this.scoreboard.resetScores(boldTraitorLabel);
			this.scoreboard.resetScores(boldInnocentLabel);
			break;
		}

		detectiveCount.setScore(detectives);
		traitorCount.setScore(traitors);
		innocentCount.setScore(innocents);
	}

	/**
	 * Displays the karma for this player on their XP bar.
	 */
	public void showKarma() {
		Player p = getPlayer();
		if (p != null) {
			p.setExp(0);
			p.setLevel(this.karma);
		}
		checkKarma();
	}

	/**
	 * Shows the pre-game scoreboard for this player (the map vote).
	 */
	private void showPreGameScoreboard() {
		loadMinecadeAccount();
		hookScoreboard();
		Objective objective = this.scoreboard.getObjective("stats");
		if (objective == null) {
			objective = this.scoreboard.registerNewObjective("stats", "dummy");
		}

		if (!objective.getDisplayName().equals(
				ChatColor.GOLD + ChatColor.BOLD.toString() + this.playerName
						+ "'s Stats")) {
			objective.setDisplayName(ChatColor.GOLD + ChatColor.BOLD.toString()
					+ this.playerName + "'s Stats");
		}

		if (objective.getDisplaySlot() != DisplaySlot.SIDEBAR) {
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		}

		Score karmaScore = objective.getScore(karmaLabel);
		Score butterCoinScore = objective.getScore(butterCoinsLabel);

		karmaScore.setScore(this.karma);
		int coins = (int) this.account.getButterCoins();
		butterCoinScore.setScore(coins);
	}

	/**
	 * Shows the preparation scoreboard for the player (the stats).
	 */
	private void showPrepScoreboard() {
		HashMap<OfflinePlayer, Integer> numberOfVotes = new HashMap<>();
		for (String key : votes.keySet()) {
			VoteInfo info = votes.get(key);
			if (info.voteCount > 0) {
				numberOfVotes.put(info.display, info.voteCount);
			}
		}
		showPrepScoreboard(numberOfVotes);
	}

	/**
	 * Shows the preparation scoreboard, given the map of the number of votes.
	 * 
	 * @param numberOfVotes
	 *            The map of the number of votes and the arena name they
	 *            correspond to.
	 */
	private void showPrepScoreboard(
			HashMap<OfflinePlayer, Integer> numberOfVotes) {
		hookScoreboard();
		Objective objective = this.scoreboard.getObjective("votes");
		if (objective == null) {
			objective = this.scoreboard.registerNewObjective("votes", "dummy");
		}

		if (!objective.getDisplayName().equals(ChatColor.GOLD + "Map Vote")) {
			objective.setDisplayName(ChatColor.GOLD + "Map Vote");
		}

		if (objective.getDisplaySlot() != DisplaySlot.SIDEBAR) {
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		}

		for (OfflinePlayer key : numberOfVotes.keySet()) {
			Score score = objective.getScore(key);
			score.setScore(numberOfVotes.get(key));
		}
	}

	/**
	 * Removes the amountOfNuggets from the player's inventory.
	 * 
	 * @param amountOfNuggets
	 *            The number of gold nuggets to remove.
	 */
	private void spendNuggets(int amountOfNuggets) {
		Player player = getPlayer();
		if (player == null) {
			return;
		}
		Inventory inventory = player.getInventory();
		for (int i = 0; i < inventory.getSize(); i++) {
			ItemStack item = inventory.getItem(i);
			if (item == null) {
				continue;
			}
			if (item.getType() == PlayerListener.nugget.getType()) {
				int current = item.getAmount();
				if (current < amountOfNuggets) {
					inventory.clear(i);
					amountOfNuggets -= current;
				} else if (current > amountOfNuggets) {
					item.setAmount(current - amountOfNuggets);
					break;
				} else {
					inventory.clear(i);
					break;
				}
			}
		}
		player.updateInventory();
	}

	/**
	 * Makes the player vote for whatever arena corresponds to the item.
	 * 
	 * @param item
	 *            The item the player clicked on.
	 * @return True if the event needs to be canceled.
	 */
	private boolean vote(ItemStack item) {
		Player player = getPlayer();
		if (player == null) {
			return false;
		}
		if (item == null) {
			return false;
		}
		if (item.getItemMeta() == null) {
			return false;
		}
		String key = item.getItemMeta().getDisplayName();
		if (key == null) {
			return false;
		}
		if (key.equals(playGame)) {
			this.spectating = false;
			installVoteTools();
			return true;
		}
		if (this.hasVoted) {
			return false;
		}
		if (key.equals(spectateGame)) {
			this.spectating = true;
			Tools.clearInventory(player);
			Inventory inventory = player.getInventory();
			ItemStack playItem = new ItemStack(Material.GOLD_SWORD, 1);
			ItemMeta playMeta = playItem.getItemMeta();
			playMeta.setDisplayName(playGame);
			playItem.setItemMeta(playMeta);
			inventory.setItem(0, playItem);
			return true;
		}
		if (key.equals(voteForAMap)) {
			Inventory voteInventory = Bukkit.getServer()
					.createInventory(
							null,
							getMultipleOfNine(plugin.thread.getArenaLocations()
									.size()), "Which map will you vote for?");
			ArrayList<ItemStack> votingItems = getVotingItems();
			for (int i = 0; i < votingItems.size(); i++) {
				voteInventory.setItem(i, votingItems.get(i));
			}
			player.openInventory(voteInventory);

			return true;
		}
		key = key.substring(9);
		ConcurrentHashMap<String, Location> locations = plugin.thread
				.getArenaLocations();
		if (locations.containsKey(key)) {
			this.hasVoted = true;
			VoteInfo info;
			if (votes.containsKey(key)) {
				info = votes.get(key);
			} else {
				info = new VoteInfo(Bukkit.getOfflinePlayer(key));
			}
			info.voteCount++;
			votes.put(key, info);
			showAllPrepScoreboards();
			Tools.clearInventory(player);
			player.closeInventory();
			player.updateInventory();
			player.sendMessage(ChatColor.GREEN + "You have voted for the "
					+ key + ChatColor.GREEN + " map.");
			return true;
		}
		return false;
	}

}
