package src.main.java.de.orion304.ttt.listeners;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.EnumClientCommand;
import net.minecraft.server.v1_7_R1.PacketPlayInClientCommand;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import src.main.java.de.orion304.ttt.main.ChatManager;
import src.main.java.de.orion304.ttt.main.FileManager;
import src.main.java.de.orion304.ttt.main.GameState;
import src.main.java.de.orion304.ttt.main.MineTTT;
import src.main.java.de.orion304.ttt.main.TempBlock;
import src.main.java.de.orion304.ttt.main.Tools;
import src.main.java.de.orion304.ttt.players.DeathLocation;
import src.main.java.de.orion304.ttt.players.PlayerTeam;
import src.main.java.de.orion304.ttt.players.TTTPlayer;
import src.main.java.org.orion304.utils.Hologram;

public class PlayerListener implements Listener {

	public static final ItemStack nugget = new ItemStack(Material.GOLD_NUGGET,
			1);

	private final ArrayList<Player> deadPlayers = new ArrayList<>();
	private final List<Hologram> holograms = new ArrayList<>();

	MineTTT plugin;
	ChatManager chatManager;

	/**
	 * Instantiates the main listener for MineTTT
	 * 
	 * @param instance
	 *            MineTTT plugin instance
	 * 
	 * @return PlayerListener instance
	 */
	public PlayerListener(MineTTT instance) {
		this.plugin = instance;
		this.chatManager = new ChatManager(instance);
	}

	/**
	 * Makes players automatically click the "Respawn" button 2 ticks after
	 * dying.
	 * 
	 * @param event
	 *            The player death event.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void autoRespawn(final PlayerDeathEvent event) {
		// auto-respawn
		new BukkitRunnable() {
			@Override
			public void run() {
				((CraftPlayer) event.getEntity()).getHandle().playerConnection
						.a(new PacketPlayInClientCommand(
								EnumClientCommand.PERFORM_RESPAWN));
			}
		}.runTaskLater(this.plugin, 2L);
	}

	/**
	 * Returns the CraftPlayer object of a given Player object.
	 * 
	 * @param player
	 *            The player to cast.
	 * @return The CraftPlayer object.
	 */
	private EntityPlayer getPlayer(Player player) {
		return ((CraftPlayer) player).getHandle();
	}

	/**
	 * Gives a golden nugget to a player.
	 * 
	 * @param player
	 */
	private void giveNugget(Player player, PlayerTeam cause) {
		player.getInventory().addItem(nugget);
		TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(player);
		Tplayer.logKill(cause);
	}

	//
	// private void lookAlive(Player player) {
	// PacketPlayOutAnimation alive = new PacketPlayOutAnimation(
	// getPlayer(player), 2);
	// for (Player other : player.getWorld().getPlayers()) {
	// getPlayer(other).playerConnection.sendPacket(alive);
	// }
	// }
	//
	// private void lookDead(final Player player, final Block block) {
	// PacketPlayOutBed bed = new PacketPlayOutBed(getPlayer(player),
	// block.getX(), block.getY(), block.getZ());
	// for (Player other : player.getWorld().getPlayers()) {
	// Tools.verbose("Sending packet to " + other.getName() + " with "
	// + player.getName() + "'s body.");
	// getPlayer(other).playerConnection.sendPacket(bed);
	// }
	// }

	/**
	 * Cancels the BlockBreak event if the player is spectating.
	 * 
	 * @param event
	 *            The block break event.
	 */
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (this.plugin.thread.isGameRunning()) {
			Player p = event.getPlayer();
			TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(p);
			if (Tplayer.getTeam() == PlayerTeam.NONE) {
				event.setCancelled(true);
			}
		}
	}

	/**
	 * Cancels the BlockDamage event if the player is spectating.
	 * 
	 * @param event
	 *            The block damage event.
	 */
	@EventHandler
	public void onBlockDamage(BlockDamageEvent event) {
		if (this.plugin.thread.isGameRunning()) {
			Player p = event.getPlayer();
			TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(p);
			if (Tplayer.getTeam() == PlayerTeam.NONE) {
				event.setCancelled(true);
			}
		}
	}

	/**
	 * Sends the block place event's parameters to the TTTPlayer's handler.
	 * 
	 * @param event
	 *            The block place event.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		TTTPlayer.handleBlockPlace(player, event.getBlock());
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause() == DamageCause.THORNS
				|| event.getCause() == DamageCause.FALL) {
			event.setCancelled(true);
		}
		if (this.plugin.thread.getGameStatus() == GameState.CELEBRATIONS) {
			event.setCancelled(true);
		}
	}

	/**
	 * Handles all the things that go along with a player attacking another
	 * player.
	 * 
	 * @param event
	 *            The entity damage event.
	 */
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!this.plugin.thread.isGameRunning()) {
			event.setCancelled(true);
			return;
		}

		Entity damageDealer = event.getDamager();
		Entity damageTaker = event.getEntity();

		if (damageDealer instanceof Projectile) {
			damageDealer = ((Projectile) damageDealer).getShooter();
		}

		if (damageDealer instanceof Player && damageTaker instanceof Player) {
			Player damager = (Player) damageDealer;
			Player player = (Player) damageTaker;

			TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(player);
			TTTPlayer Tdealer = TTTPlayer.getTTTPlayer(damager);

			double damage = event.getDamage();

			if (Tdealer.getTeam() == PlayerTeam.NONE) {
				event.setCancelled(true);
				return;
			}

			double newdamage = Tdealer.getDamage(player, damage,
					event.getCause());

			if (newdamage != event.getDamage()) {
				event.setCancelled(true);
				if (newdamage > 0) {
					player.damage(newdamage);
				}
			}

			Tplayer.setRecentDamager(damager);
		}
	}

	/**
	 * Passes most of the InventoryClick event to TTTPlayer to handle the shop,
	 * voting, and other cases. Cancels the event if need be.
	 * 
	 * @param event
	 *            The inventory click event.
	 */
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {

			boolean cancel = TTTPlayer.handleInventoryClick(
					(Player) event.getWhoClicked(), event.getCurrentItem(),
					event.getSlotType());
			// if (cancel)
			event.setCancelled(cancel);
		}
	}

	/**
	 * Cancels the InventoryDrag event - though I'm not sure that this event is
	 * ever even called.
	 * 
	 * @param event
	 *            The inventory drag event.
	 */
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		event.getNewItems().clear();
		event.setCancelled(true);
	}

	/**
	 * Cancels people interacting with their inventory - though I'm not sure
	 * that this event is ever called.
	 * 
	 * @param event
	 *            The inventory interact event.
	 */
	@EventHandler
	public void onInventoryInteract(InventoryInteractEvent event) {
		event.setCancelled(true);
	}

	/**
	 * Cancels people moving inventory items - though I'm not sure this event is
	 * ever called.
	 * 
	 * @param event
	 *            The inventory move event.
	 */
	@EventHandler
	public void onInventoryMoveItem(InventoryMoveItemEvent event) {
		event.setCancelled(true);
		event.setItem(null);
	}

	/**
	 * Handles people opening the chests managed by ChestHandler and prevents
	 * spectators from doing anything with them.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		InventoryHolder holder = event.getInventory().getHolder();
		if (this.plugin.thread.isGameRunning()) {
			if (!(event.getPlayer() instanceof Player)) {
				Tools.verbose("not a player?");
				return;
			}
			Player p = (Player) event.getPlayer();
			TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(p);
			if (Tplayer.getTeam() == PlayerTeam.NONE) {
				event.setCancelled(true);
				p.closeInventory();
				return;
			}
		}
		if (holder instanceof Chest) {
			this.plugin.chestHandler.handleChest((Chest) holder);
		}

	}

	/**
	 * Sends the Chat event to the chat manager for nicer formatting.
	 * 
	 * @param event
	 *            The player chat event.
	 */
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		this.chatManager.handleChat(player, event.getMessage());
		event.setCancelled(true);
	}

	/**
	 * Insures that no players can send /tells while spectating (an abusable
	 * thing).
	 * 
	 * @param event
	 *            The command preprocess event.
	 */
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		String command = event.getMessage();
		if (command.contains("tell")) {
			Player player = event.getPlayer();
			TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(player);
			if (Tplayer.getTeam() == PlayerTeam.NONE
					&& this.plugin.thread.isGameRunning()) {
				player.sendMessage("You cannot send tells while spectating.");
				event.setCancelled(true);
			}
		}
	}

	/**
	 * Handles all the specifics with a player dying, like dropping the head,
	 * sending the messages, adding karma, giving nuggets, clearing drops and
	 * sending the event to TTTPlayer.
	 * 
	 * @param event
	 *            The player death event.
	 */
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();

		// Block block2 = Tools.getFloor(player.getEyeLocation(), 4);
		// if (block2 == null)
		// block2 = player.getLocation().getBlock();
		// lookDead(player, block2);
		// deadPlayers.add(player);
		// Tools.verbose("Player looks dead now...");

		String deathMessage = event.getDeathMessage();
		TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(player);

		Player recentDamager = Tplayer.getRecentDamager();
		TTTPlayer TrecentDamager = TTTPlayer.getTTTPlayer(recentDamager);

		if (TrecentDamager != null) {

			PlayerTeam damagerTeam = TrecentDamager.getTeam();
			PlayerTeam playerTeam = Tplayer.getTeam();
			if (damagerTeam == PlayerTeam.TRAITOR) {
				new DeathLocation(player.getName(), recentDamager.getName(),
						player.getLocation());
				Block block = Tools.getFloor(player.getEyeLocation(), 4);
				if (block == null) {
					block = player.getLocation().getBlock();
				}
				this.deadPlayers.add(player);
				spawnSkull(player, block);
			}

			if (playerTeam != PlayerTeam.NONE && damagerTeam != PlayerTeam.NONE) {
				if ((damagerTeam == PlayerTeam.INNOCENT || damagerTeam == PlayerTeam.DETECTIVE)
						&& playerTeam != PlayerTeam.TRAITOR) {
					TrecentDamager.loseKarma();
				}

				if ((damagerTeam != PlayerTeam.TRAITOR && playerTeam == PlayerTeam.TRAITOR)) {
					Tplayer.addKarma();
					giveNugget(recentDamager, PlayerTeam.TRAITOR);
					recentDamager.sendMessage(ChatColor.GOLD.toString()
							+ ChatColor.ITALIC
							+ "You got 1 gold nugget for taking down a "
							+ FileManager.traitorColor + playerTeam
							+ ChatColor.RESET + "!");
				}

				if (damagerTeam == PlayerTeam.TRAITOR
						&& playerTeam != PlayerTeam.TRAITOR) {
					TrecentDamager.addKarma();
					giveNugget(recentDamager, playerTeam);
					String roleName = FileManager.innocentColor
							+ playerTeam.toString() + ChatColor.RESET;
					if (playerTeam == PlayerTeam.DETECTIVE) {
						roleName = FileManager.detectiveColor
								+ playerTeam.toString() + ChatColor.RESET;
					}
					recentDamager.sendMessage(ChatColor.GOLD.toString()
							+ ChatColor.ITALIC
							+ "You got 1 gold nugget for taking down a "
							+ roleName + "!");
					TrecentDamager.giveSpeedBoost(2, 5);
				}

				if (playerTeam == PlayerTeam.TRAITOR
						&& damagerTeam == PlayerTeam.TRAITOR) {
					TrecentDamager.loseKarma();
				}
			}
			deathMessage = player.getName()
					+ " was slain, and revealed to be a";
			if (playerTeam == PlayerTeam.INNOCENT) {
				deathMessage += "n";
			}
			ChatColor color = ChatColor.WHITE;
			switch (playerTeam) {
			case INNOCENT:
				color = FileManager.innocentColor;
				break;
			case DETECTIVE:
				color = FileManager.detectiveColor;
				break;
			case TRAITOR:
				color = FileManager.traitorColor;
				break;
			default:
				break;
			}
			deathMessage += " " + color + playerTeam + ".";

			event.setDeathMessage(deathMessage);

		}

		event.setDroppedExp(0);

		List<ItemStack> drops = event.getDrops();
		// ArrayList<ItemStack> toRemove = new ArrayList<>();
		// for (int i = 0; i < drops.size(); i++) {
		// ItemStack item = drops.get(i);
		// if (item == null)
		// continue;
		// if (item.getItemMeta() == null)
		// continue;
		// if (item.getType() == Material.GOLD_NUGGET)
		// toRemove.add(item);
		// if (item.getType() == Material.COMPASS)
		// toRemove.add(item);
		// String name = item.getItemMeta().getDisplayName();
		// if (name.equals(TTTPlayer.claimLabel)
		// || name.equals(TTTPlayer.suspectLabel)
		// || name.equals(TTTPlayer.trustLabel))
		// toRemove.add(item);
		// }
		// drops.removeAll(toRemove);
		drops.clear();

		if (this.plugin.thread.isGameRunning()) {
			TTTPlayer.handleDeath(player);
		}

	}

	/**
	 * Sends the event to TTTPlayer for processing.
	 * 
	 * @param event
	 *            The player interact event.
	 */
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		TTTPlayer.handleInteract(player);
	}

	/**
	 * Sends the event 2 ticks later to TTTPlayer for processing.
	 * 
	 * @param event
	 *            The player join event.
	 */
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		new BukkitRunnable() {
			@Override
			public void run() {
				Player player = event.getPlayer();

				TTTPlayer.handleJoin(player);
			}
		}.runTaskLater(this.plugin, 2L);

	}

	/**
	 * Checks the player first against the Minecade database to respect those
	 * bans, then against the locally stored file with karma, to see if the
	 * player is temporarily banned for karma reasons.
	 * 
	 * @param event
	 *            The player login event.
	 */
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		String name = event.getPlayer().getName();
		if (this.plugin.minecade.isPlayerBanned(name)) {
			event.disallow(Result.KICK_BANNED,
					"You have been banned from all Minecade servers.");
			return;
		}
		if (TTTPlayer.isBanned(name)) {
			event.disallow(Result.KICK_BANNED,
					"Your karma dropped too low, you are banned for 5 minutes!");
		}
	}

	/**
	 * Sends the event to TTTPlayer and cancels it if need be.
	 * 
	 * @param event
	 *            The player pickup item event.
	 */
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		boolean cancel = TTTPlayer.handleItemPickup(event.getPlayer());
		if (cancel) {
			event.setCancelled(true);
		}
	}

	/**
	 * Sends the event to TTTPlayer to monitor the number of players in a game,
	 * team changes by leaving, scoreboards, etc.
	 * 
	 * @param event
	 *            The player quit event.
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		TTTPlayer.handleLeave(player);
	}

	/**
	 * Forcibly sets the respawn location to the Lobby.
	 * 
	 * @param event
	 *            The player respawn event.
	 */
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		event.setRespawnLocation(this.plugin.thread.getLobbyLocation());
	}

	/**
	 * This method does nothing since having players be "asleep" while dead
	 * didn't work out.
	 */
	public void resetDeadPlayers() {
		for (Player player : this.deadPlayers) {
			// lookAlive(player);
		}
		this.deadPlayers.clear();
		for (Hologram hologram : this.holograms) {
			hologram.destroy();
		}
		this.holograms.clear();
	}

	private void spawnSkull(Player player, Block block) {
		new TempBlock(block, Material.SKULL);
		BlockState state = block.getState();
		Skull skull = (Skull) state;
		skull.setOwner(player.getName());
		skull.setSkullType(SkullType.PLAYER);
		skull.update(true, true);

		Hologram hologram = new Hologram(this.plugin, "Here lies "
				+ player.getDisplayName());
		Location location = block.getLocation().clone();
		location.add(0, 2, 0);
		hologram.show(location);
		this.holograms.add(hologram);
	}
}
