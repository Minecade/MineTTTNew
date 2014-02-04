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
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import src.main.java.de.orion304.ttt.main.Action;
import src.main.java.de.orion304.ttt.main.GameState;
import src.main.java.de.orion304.ttt.main.MainThread;
import src.main.java.de.orion304.ttt.main.MineTTT;
import src.main.java.de.orion304.ttt.main.Tools;

public class TTTPlayer {

	private static MineTTT plugin;

	private static ConcurrentHashMap<String, TTTPlayer> players = new ConcurrentHashMap<>();

	private static final long karmaThreshold = 200L;
	private static final long duration = 5 * 60 * 1000L;
	private static final long callCooldown = 10 * 1000L;
	private static Random random = new Random();

	private static final OfflinePlayer standardDetectiveLabel = Bukkit
			.getOfflinePlayer(MainThread.detectiveColor + "Detectives:"),
			standardTraitorLabel = Bukkit
					.getOfflinePlayer(MainThread.traitorColor + "Traitors:"),
			standardInnocentLabel = Bukkit
					.getOfflinePlayer(MainThread.innocentColor + "Innocents:");

	private static final OfflinePlayer boldDetectiveLabel = Bukkit
			.getOfflinePlayer(MainThread.detectiveColor + "Detectives:"),
			boldTraitorLabel = Bukkit.getOfflinePlayer(MainThread.traitorColor
					+ "Traitors:"), boldInnocentLabel = Bukkit
					.getOfflinePlayer(MainThread.innocentColor + "Innocents:");

	private static final OfflinePlayer karmaLabel = Bukkit
			.getOfflinePlayer(ChatColor.GREEN + "Karma");

	private static final String trustLabel = "Proclaim your trust",
			suspectLabel = "Express your suspiscion",
			claimLabel = "Call out a traitor";

	private static ItemStack trustItem, suspectItem, claimItem;

	private static ConcurrentHashMap<String, VoteInfo> votes = new ConcurrentHashMap<>();

	public class VoteInfo {
		public int voteCount = 0;
		public OfflinePlayer display;

		public VoteInfo(OfflinePlayer display) {
			this.display = display;
		}
	}

	private String playerName;
	private PlayerTeam team = PlayerTeam.NONE;
	private Player recentDamager = null;
	private Player trackedPlayer = null;
	private Scoreboard scoreboard = Bukkit.getScoreboardManager()
			.getNewScoreboard();
	private Map<PlayerTeam, Team> teams = new HashMap<>();
	private int karma = 1000;
	private long banDate = 0, banLength = 0;
	private transient boolean hasVoted = false;
	private ConcurrentHashMap<String, Long> calls = new ConcurrentHashMap<>();

	public TTTPlayer(String name, int karma, long banDate, long banLength) {
		TTTPlayer player = new TTTPlayer(name);
		player.karma = karma;
		player.banDate = banDate;
		player.banLength = banLength;
	}

	public TTTPlayer(String name) {
		playerName = name;
		players.put(playerName, this);
	}

	public static TTTPlayer getTTTPlayer(Player player) {
		if (player == null)
			return null;
		String name = player.getName();
		if (players.containsKey(name))
			return players.get(name);
		return new TTTPlayer(name);
	}

	public void sendMessage(String string) {
		Player p = getPlayer();
		if (p != null)
			p.sendMessage(string);
	}

	public static Collection<TTTPlayer> getPlayers() {
		return players.values();
	}

	public Player getPlayer() {
		return Bukkit.getPlayer(playerName);
	}

	public void setTeam(PlayerTeam team) {
		this.team = team;
	}

	public PlayerTeam getTeam() {
		return team;
	}

	public void setRecentDamager(Player player) {
		recentDamager = player;
	}

	public Player getRecentDamager() {
		return recentDamager;
	}

	public void setTrackedPlayer(Player player) {
		trackedPlayer = player;
	}

	public Player getTrackedPlayer() {
		return trackedPlayer;
	}

	public String getName() {
		return playerName;
	}

	public long getBanDate() {
		return banDate;
	}

	public long getBanLength() {
		return banLength;
	}

	public void ban(long duration) {
		banDate = System.currentTimeMillis();
		banLength = duration;
	}

	public boolean isBanned() {
		return System.currentTimeMillis() < banDate + banLength;
	}

	public void setKarma(int karma) {
		if (karma > 1000)
			karma = 1000;
		if (karma < 0)
			karma = 0;
		this.karma = karma;
		showKarma();
	}

	public int getKarma() {
		return karma;
	}

	public void addKarma(int value) {
		karma += value;
		if (karma > 1000)
			karma = 1000;
		if (karma < 0)
			karma = 0;
		showKarma();
	}

	public void addKarma() {
		addKarma((int) (Math.random() * 20 + 40));
	}

	private void hookScoreboard() {
		Player player = getPlayer();
		if (player != null)
			scoreboard = player.getScoreboard();
	}

	private void initializeScoreboard() {
		Player player = Bukkit.getPlayer(playerName);

		if (player != null) {
			scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

			Team detectives = scoreboard.registerNewTeam("Detectives");
			teams.put(PlayerTeam.DETECTIVE, detectives);
			detectives.setPrefix(MainThread.detectiveColor.toString());

			Team traitors = scoreboard.registerNewTeam("Traitors");
			teams.put(PlayerTeam.TRAITOR, traitors);
			traitors.setPrefix(MainThread.traitorColor.toString());

			Team innocents = scoreboard.registerNewTeam("Innocents");
			teams.put(PlayerTeam.INNOCENT, innocents);

			Team spectators = scoreboard.registerNewTeam("Spectators");
			teams.put(PlayerTeam.NONE, spectators);
			spectators.setPrefix(MainThread.spectatorColor.toString());

			player.setScoreboard(scoreboard);

		}
	}

	public static void registerAllScoreboards() {
		Player[] players = Bukkit.getOnlinePlayers();
		for (Player player1 : players) {
			for (Player player2 : players) {
				TTTPlayer Tplayer1 = getTTTPlayer(player1);
				if (Tplayer1 != null)
					Tplayer1.registerPlayer(player2);
			}
		}
	}

	public static void allRegisterPlayer(Player player) {
		allRegisterPlayer(getTTTPlayer(player));
	}

	public static void allRegisterPlayer(TTTPlayer player) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(p);
			Tplayer.registerPlayer(player);
		}
	}

	public void registerAllPlayers() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			registerPlayer(Tplayer);
		}
	}

	public void registerPlayer(Player player) {
		registerPlayer(getTTTPlayer(player));
	}

	public void registerPlayer(TTTPlayer player) {
		switch (team) {
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

	private void registerPlayer(TTTPlayer player, PlayerTeam... allowedTeams) {
		if (player == null)
			return;
		if (teams.isEmpty())
			initializeScoreboard();
		PlayerTeam hisTeam = player.team;
		Player p = Bukkit.getPlayer(player.playerName);
		Player me = getPlayer();

		if (p == null || me == null)
			return;

		if (Arrays.asList(allowedTeams).contains(hisTeam)) {
			// Tools.verbose("Registering " + player.playerName + "(Team: "
			// + hisTeam + ") for " + playerName);
			teams.get(hisTeam).addPlayer(p);
			me.showPlayer(p);
		}

		if (hisTeam == PlayerTeam.NONE && team != PlayerTeam.NONE) {
			me.hidePlayer(p);
		}

	}

	private static int getNumberOfPlayers(PlayerTeam team) {
		int i = 0;
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			if (Tplayer.team == team)
				i++;
		}
		return i;
	}

	public static int getNumberOfDetectives() {
		return getNumberOfPlayers(PlayerTeam.DETECTIVE);
	}

	public static int getNumberOfTraitors() {
		return getNumberOfPlayers(PlayerTeam.TRAITOR);
	}

	public static int getNumberOfInnocents() {
		return getNumberOfPlayers(PlayerTeam.INNOCENT);
	}

	public void resetPlayer() {
		team = PlayerTeam.NONE;
		recentDamager = null;
		trackedPlayer = null;
		teams = new HashMap<>();
		hasVoted = false;
		resetScoreboard();
	}

	public static void resetScoreboards() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			Tplayer.resetScoreboard();
		}
	}

	private void resetScoreboard() {
		Scoreboard newboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Player p = getPlayer();
		if (p != null) {
			p.setScoreboard(newboard);
		}
		scoreboard = newboard;
	}

	public static void reset() {
		for (TTTPlayer player : players.values()) {
			player.resetPlayer();
		}
		votes.clear();
	}

	public static void setAllLevel(int level) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.setExp(0);
			player.setLevel(level);
		}
	}

	public void loseKarma() {
		addKarma(-(int) (random.nextDouble() * 20 + 40));
	}

	public void showKarma() {
		Player p = getPlayer();
		if (p != null) {
			p.setExp(0);
			p.setLevel(karma);
		}
		checkKarma();
	}

	public void checkKarma() {
		Player p = getPlayer();
		if (p == null)
			return;
		if (karma < karmaThreshold) {
			p.kickPlayer("Your karma has dropped below 200! You've been banned!");
			setKarma(500);
			banDate = System.currentTimeMillis();
			banLength = duration;
		}
	}

	public static boolean isBanned(String name) {
		if (players.containsKey(name)) {
			TTTPlayer player = players.get(name);
			return player.isBanned();
		}
		return false;
	}

	private void showGameScoreboard() {
		int detectives = TTTPlayer.getNumberOfDetectives();
		int traitors = TTTPlayer.getNumberOfTraitors();
		int innocents = TTTPlayer.getNumberOfInnocents();

		showGameScoreboard(detectives, traitors, innocents);
	}

	private void showGameScoreboard(int detectives, int traitors, int innocents) {
		hookScoreboard();
		Objective objective = scoreboard.getObjective("livingPlayers");
		if (objective == null) {
			objective = scoreboard.registerNewObjective("livingPlayers",
					"dummy");
		}

		if (!objective.getDisplayName().equals(
				ChatColor.GREEN + "Living Players"))
			objective.setDisplayName(ChatColor.GREEN + "Living Players");

		if (objective.getDisplaySlot() != DisplaySlot.SIDEBAR)
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		Score detectiveCount = objective.getScore(standardDetectiveLabel);
		Score traitorCount = objective.getScore(standardTraitorLabel);
		Score innocentCount = objective.getScore(standardInnocentLabel);

		switch (team) {
		case DETECTIVE:
			scoreboard.resetScores(standardDetectiveLabel);
			detectiveCount = objective.getScore(boldDetectiveLabel);
			break;
		case TRAITOR:
			scoreboard.resetScores(standardTraitorLabel);
			traitorCount = objective.getScore(boldTraitorLabel);
			break;
		case INNOCENT:
			scoreboard.resetScores(standardInnocentLabel);
			innocentCount = objective.getScore(boldInnocentLabel);
			break;
		default:
			scoreboard.resetScores(boldDetectiveLabel);
			scoreboard.resetScores(boldTraitorLabel);
			scoreboard.resetScores(boldInnocentLabel);
			break;
		}

		detectiveCount.setScore(detectives);
		traitorCount.setScore(traitors);
		innocentCount.setScore(innocents);
	}

	private void showPreGameScoreboard() {
		hookScoreboard();
		Objective objective = scoreboard.getObjective("stats");
		if (objective == null) {
			objective = scoreboard.registerNewObjective("stats", "dummy");
		}

		if (!objective.getDisplayName().equals("Stats"))
			objective.setDisplayName("Stats");

		if (objective.getDisplaySlot() != DisplaySlot.SIDEBAR)
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		Score karmaScore = objective.getScore(karmaLabel);

		karmaScore.setScore(karma);
	}

	private void showPrepScoreboard(
			HashMap<OfflinePlayer, Integer> numberOfVotes) {
		hookScoreboard();
		Objective objective = scoreboard.getObjective("votes");
		if (objective == null) {
			objective = scoreboard.registerNewObjective("votes", "dummy");
		}

		if (!objective.getDisplayName().equals("Map Vote"))
			objective.setDisplayName("Map Vote");

		if (objective.getDisplaySlot() != DisplaySlot.SIDEBAR)
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		for (OfflinePlayer key : numberOfVotes.keySet()) {
			Score score = objective.getScore(key);
			score.setScore(numberOfVotes.get(key));
		}
	}

	private void showPrepScoreboard() {
		HashMap<OfflinePlayer, Integer> numberOfVotes = new HashMap<>();
		for (String key : votes.keySet()) {
			VoteInfo info = votes.get(key);
			if (info.voteCount > 0)
				numberOfVotes.put(info.display, info.voteCount);
		}
		showPrepScoreboard(numberOfVotes);
	}

	public static void showAllPreGameScoreboards() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			Tplayer.showPreGameScoreboard();
		}
	}

	public static void showAllPrepScoreboards() {
		HashMap<OfflinePlayer, Integer> numberOfVotes = new HashMap<>();
		for (String key : votes.keySet()) {
			VoteInfo info = votes.get(key);
			if (info.voteCount > 0)
				numberOfVotes.put(info.display, info.voteCount);
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			Tplayer.showPrepScoreboard(numberOfVotes);
		}
	}

	public static void showAllGameScoreboards() {

		int detectives = TTTPlayer.getNumberOfDetectives();
		int traitors = TTTPlayer.getNumberOfTraitors();
		int innocents = TTTPlayer.getNumberOfInnocents();

		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			Tplayer.showGameScoreboard(detectives, traitors, innocents);
		}
	}

	public static void dealKarma() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			if (Tplayer.team == PlayerTeam.INNOCENT
					|| Tplayer.team == PlayerTeam.DETECTIVE) {
				Tplayer.addKarma();
			}
		}
	}

	public static void handleJoin(Player player) {
		GameState state = plugin.thread.getGameStatus();
		TTTPlayer Tplayer = getTTTPlayer(player);
		Tplayer.team = PlayerTeam.NONE;
		player.setGameMode(GameMode.ADVENTURE);
		switch (state) {
		case OFF:
			Tplayer.showPreGameScoreboard();
			break;
		case GAME_PREPARING:
			Tplayer.showPrepScoreboard();
			Tplayer.installVoteTools();
			break;
		case GAME_RUNNING:
			Tplayer.showGameScoreboard();
			allRegisterPlayer(player);
			break;
		}
	}

	public static void handleLeave(Player player) {
		TTTPlayer Tplayer = getTTTPlayer(player);
		Tplayer.team = PlayerTeam.NONE;
		player.setGameMode(GameMode.ADVENTURE);
		if (plugin.teamHandler.isGameOver()) {
			plugin.thread.endGame(false);
		} else {
			showAllGameScoreboards();
		}
	}

	public static boolean handleInventoryClick(Player player, int slot) {
		GameState state = plugin.thread.getGameStatus();
		TTTPlayer Tplayer = getTTTPlayer(player);
		switch (state) {
		case OFF:
			break;
		case GAME_PREPARING:
			return Tplayer.vote(slot);
		case GAME_RUNNING:
			if (Tplayer.getTeam() == PlayerTeam.NONE)
				return true;
			break;
		}
		return false;
	}

	public static boolean handleInteract(Player player) {
		int slot = player.getInventory().getHeldItemSlot();
		GameState state = plugin.thread.getGameStatus();
		TTTPlayer Tplayer = getTTTPlayer(player);
		switch (state) {
		case OFF:
			break;
		case GAME_PREPARING:
			return Tplayer.vote(slot);
		case GAME_RUNNING:
			if (Tplayer.getTeam() == PlayerTeam.NONE)
				return true;

			ItemStack item = player.getItemInHand();
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

			if (Tplayer.getTeam() == PlayerTeam.DETECTIVE) {
				Player target = Tools.getKiller(player);
				if (target != null)
					plugin.thread.findKiller(player, target);
			}
			break;
		}
		return false;
	}

	private boolean vote(int slot) {
		if (hasVoted)
			return false;
		Player player = getPlayer();
		if (player == null)
			return false;
		Inventory inventory = player.getInventory();
		ItemStack item = inventory.getItem(slot);
		if (item == null)
			return false;
		String key = item.getItemMeta().getDisplayName();
		if (key == null)
			return false;
		key = key.substring(9);
		ConcurrentHashMap<String, Location> locations = plugin.thread
				.getArenaLocations();
		if (locations.containsKey(key)) {
			hasVoted = true;
			VoteInfo info;
			if (votes.containsKey(key)) {
				info = votes.get(key);
			} else {
				info = new VoteInfo(Bukkit.getOfflinePlayer(key));
			}
			info.voteCount++;
			votes.put(key, info);
			showAllPrepScoreboards();
			inventory.clear();
			player.sendMessage(ChatColor.GREEN + "You have voted for the "
					+ key + " map.");
			return true;
		}
		return false;
	}

	public static void handleDeath(Player player) {
		player.setGameMode(GameMode.ADVENTURE);
		TTTPlayer Tplayer = getTTTPlayer(player);
		Tplayer.team = PlayerTeam.NONE;
		if (plugin.teamHandler.isGameOver()) {
			plugin.thread.endGame(false);
		} else {
			player.getInventory().clear();
			showAllGameScoreboards();
		}
	}

	public static boolean handleItemPickup(Player player) {
		GameState state = plugin.thread.getGameStatus();
		TTTPlayer Tplayer = getTTTPlayer(player);
		switch (state) {
		case OFF:
			break;
		case GAME_PREPARING:
			if (!Tplayer.hasVoted)
				return true;
			break;
		case GAME_RUNNING:
			if (Tplayer.team == PlayerTeam.NONE)
				return true;
			break;
		}
		return false;
	}

	public static void installAllVoteTools() {
		ArrayList<ItemStack> items = getVotingItems();

		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			Tplayer.installVoteTools(items);
		}
	}

	private void installVoteTools(List<ItemStack> items) {
		Player p = getPlayer();
		if (p == null)
			return;
		Inventory inventory = p.getInventory();
		inventory.clear();
		for (int i = 0; i < items.size(); i++) {
			inventory.setItem(i, items.get(i));
		}
	}

	private void installVoteTools() {
		installVoteTools(getVotingItems());
	}

	private static ArrayList<ItemStack> getVotingItems() {
		ArrayList<ItemStack> items = new ArrayList<>();
		int i = 0;
		for (String key : plugin.thread.getArenaLocations().keySet()) {
			ItemStack stack = new ItemStack(Material.DIAMOND, 1);
			ItemMeta meta = stack.getItemMeta();
			meta.setDisplayName("Vote for " + key);
			stack.setItemMeta(meta);
			items.add(i, stack);
			i++;
		}
		return items;
	}

	public static Location getWinningLocation() {
		ConcurrentHashMap<String, Location> locations = plugin.thread
				.getArenaLocations();
		Random random = new Random();
		if (locations.isEmpty())
			return null;
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
			if (location == null)
				continue;
			int i = 1;
			while (i <= info.voteCount) {
				choices.add(location);
				i++;
			}
		}
		int choice = random.nextInt(choices.size());
		return choices.get(choice);

	}

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
	}

	private void callOut(Action action, Player target) {
		Player me = getPlayer();
		if (me == null)
			return;
		long lastTime = 0;
		if (calls.containsKey(target.getName())) {
			lastTime = calls.get(target.getName());
		}

		long time = System.currentTimeMillis();
		if (time < lastTime + callCooldown) {
			return;
		}

		calls.put(target.getName(), time);

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

	private static String getDisplayedName(TTTPlayer player, TTTPlayer receiver) {
		GameState state = plugin.thread.getGameStatus();

		Player p = player.getPlayer();
		Player r = receiver.getPlayer();

		if (p == null || r == null)
			return player.playerName;

		PlayerTeam team = player.team;
		PlayerTeam receiverTeam = receiver.team;

		String playerName = p.getName();

		if (state != GameState.GAME_RUNNING) {
			return playerName;
		}

		if (team == PlayerTeam.NONE) {
			if (receiverTeam == PlayerTeam.NONE) {
				playerName = MainThread.spectatorColor + "<Spectator> "
						+ playerName + ChatColor.RESET;
				return playerName;
			}
		}

		if (team == PlayerTeam.TRAITOR
				&& (receiverTeam == PlayerTeam.NONE || receiverTeam == PlayerTeam.TRAITOR)) {
			playerName = MainThread.traitorColor + playerName + ChatColor.RESET;
			return playerName;
		}

		if (team == PlayerTeam.DETECTIVE) {
			playerName = MainThread.detectiveColor + playerName
					+ ChatColor.RESET;
			return playerName;
		}

		return playerName;
	}

	public static void giveChatItemsToAll() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = getTTTPlayer(player);
			Tplayer.giveChatItems();
		}
	}

	private void giveChatItems() {
		Player player = getPlayer();
		if (player == null)
			return;
		Inventory inventory = player.getInventory();
		if (team != PlayerTeam.NONE) {
			inventory.setItem(5, trustItem);
			inventory.setItem(6, suspectItem);
			inventory.setItem(7, claimItem);
		}
	}

}
