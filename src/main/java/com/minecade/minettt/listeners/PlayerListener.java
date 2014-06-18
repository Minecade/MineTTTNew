package com.minecade.minettt.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.server.v1_7_R3.EntityPlayer;
import net.minecraft.server.v1_7_R3.EnumClientCommand;
import net.minecraft.server.v1_7_R3.PacketPlayInClientCommand;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
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
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.minecade.minettt.main.ChatManager;
import com.minecade.minettt.main.FileManager;
import com.minecade.minettt.main.GameState;
import com.minecade.minettt.main.MineTTT;
import com.minecade.minettt.main.TempBlock;
import com.minecade.minettt.main.Tools;
import com.minecade.minettt.players.DeathLocation;
import com.minecade.minettt.players.PlayerTeam;
import com.minecade.minettt.players.TTTPlayer;
import org.orion304.utils.HologramOld;

public class PlayerListener implements Listener {

	public static final ItemStack nugget = new ItemStack(Material.GOLD_NUGGET,
			1);

	private final ArrayList<Player> deadPlayers = new ArrayList<>();
	private final List<HologramOld> holograms = new ArrayList<>();

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

	/**
	 * Cancels damage from cactii and falling, and any damage during
	 * celebrations.
	 * 
	 * @param event
	 */
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (!this.plugin.thread.isGameRunning()) {
			event.setCancelled(true);
		}
		if (this.plugin.thread.getGameStatus() == GameState.CELEBRATIONS) {
			event.setCancelled(true);
		}
		if (event.getEntity() instanceof Player && !event.isCancelled()) {
			Player player = (Player) event.getEntity();
			TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(player);
			if (Tplayer.getTeam() == PlayerTeam.NONE) {
				event.setCancelled(true);
			}
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
		    damageDealer = (Entity) ((Projectile) damageDealer).getShooter();
		}

		if (damageDealer instanceof Player && damageTaker instanceof Player) {
			Player damager = (Player) damageDealer;
			Player player = (Player) damageTaker;

			TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(player);
			TTTPlayer Tdealer = TTTPlayer.getTTTPlayer(damager);

			double damage = event.getDamage();

			if (Tdealer.getTeam() == PlayerTeam.NONE
					|| Tplayer.getTeam() == PlayerTeam.NONE) {
				event.setCancelled(true);
				return;
			}

			double newdamage = Tdealer.getDamage(player, damage,
					event.getCause());

			HologramOld damageDisplay = new HologramOld(this.plugin,
					ChatColor.RED + "-" + (int) newdamage + "HP");
			damageDisplay.show(player.getEyeLocation().add(0, -.5, 0), 30L,
					damager);

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
		// event.getNewItems().clear();
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
		// Tools.verbose("Openned!");
		if (this.plugin.thread.isGameRunning()) {
			InventoryHolder holder = event.getInventory().getHolder();
			if (!(event.getPlayer() instanceof Player)) {
				Tools.verbose("not a player?");
				return;
			}
			Player p = (Player) event.getPlayer();
			TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(p);
			if (Tplayer.getTeam() == PlayerTeam.NONE) {
				// Tplayer.giveSpectatorInventory();
				event.setCancelled(true);
				// p.closeInventory();
				return;
			}
			if (holder instanceof Chest) {
				if (Tplayer.getTeam() == PlayerTeam.NONE) {
					event.setCancelled(true);
					p.closeInventory();
					return;
				}
				this.plugin.chestHandler.handleChest((Chest) holder);
			}
		}

	}

	/**
	 * Sends the Chat event to the chat manager for nicer formatting.
	 * 
	 * @param event
	 *            The player chat event.
	 */
	@EventHandler
	public void onPlayerChat(final AsyncPlayerChatEvent event) {
		final Player player = event.getPlayer();
		new BukkitRunnable() {

			@Override
			public void run() {
				PlayerListener.this.chatManager.handleChat(player,
						event.getMessage());
			}

		}.runTaskLater(this.plugin, 1L);
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
		Player player = event.getPlayer();
		TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(player);
		if (Tplayer.getTeam() == PlayerTeam.NONE
				&& this.plugin.thread.isGameRunning()) {
			player.sendMessage(MineTTT.getPlugin().getMessage("playerlistener.no-command"));
			event.setCancelled(true);
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
					recentDamager.sendMessage(String.format(MineTTT.getPlugin().getMessage("playerlistener.one-gold"), 
				        FileManager.traitorColor + playerTeam.getName()));
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
					recentDamager.sendMessage(String.format(MineTTT.getPlugin().getMessage("playerlistener.one-gold"), roleName));
					TrecentDamager.giveSpeedBoost(2, 5);
				}

				if (playerTeam == PlayerTeam.TRAITOR
						&& damagerTeam == PlayerTeam.TRAITOR) {
					TrecentDamager.loseKarma();
				}
			}
			deathMessage = String.format(MineTTT.getPlugin().getMessage("playerlistener.slain"), player.getName());
			
//			if (playerTeam == PlayerTeam.INNOCENT) {
//				deathMessage += "n";
//			}
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
			deathMessage += " " + color + playerTeam.getName() + ".";

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

		Player player = event.getPlayer();

		TTTPlayer.handleJoin(player);

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
		if (this.plugin.thread.isOver()) {
			event.disallow(Result.KICK_OTHER, MineTTT.getPlugin().getMessage("playerlistener.restarting"));
			return;
		}
		UUID id = event.getPlayer().getUniqueId();
		String name = event.getPlayer().getName();
		TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(name);
		if (this.plugin.minecade.isPlayerBanned(id)) {
			event.disallow(Result.KICK_BANNED, MineTTT.getPlugin().getMessage("playerlistener.banned"));
			return;
		}
		if (TTTPlayer.isBanned(name)) {
			event.disallow(Result.KICK_BANNED, MineTTT.getPlugin().getMessage("playerlistener.karma"));
			return;
		}
		if (event.getResult() == Result.KICK_FULL
				&& Tplayer.canJoinFullServer()) {
			event.allow();
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

	@EventHandler(ignoreCancelled = true)
	public void onPlayerPortal(PlayerPortalEvent event) {
		event.setCancelled(true);
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
		Player player = event.getPlayer();
		if (this.plugin.thread.isGameRunning()) {
			TTTPlayer.giveLeaveItem(player);
		}
	}

	/**
	 * This method clears all the holograms of dead players.
	 */
	public void resetDeadPlayers() {
		for (Player player : this.deadPlayers) {
			// lookAlive(player);
		}
		this.deadPlayers.clear();
		for (HologramOld hologram : this.holograms) {
			hologram.destroy();
		}
		this.holograms.clear();
	}

	/**
	 * Spawns a skull and hologram of a dead player
	 * 
	 * @param player
	 *            The player's skull to spawn
	 * @param block
	 *            The block to spawn the skull on
	 */
	private void spawnSkull(Player player, Block block) {
		new TempBlock(block, Material.SKULL);
		BlockState state = block.getState();
		Skull skull = (Skull) state;
		skull.setOwner(player.getName());
		skull.setSkullType(SkullType.PLAYER);
		skull.update(true, true);

		HologramOld hologram = new HologramOld(this.plugin, "Here lies "
				+ player.getDisplayName());
		Location location = block.getLocation().clone();
		location.add(0, 1, 0);
		hologram.show(location);
		this.holograms.add(hologram);
	}
}
