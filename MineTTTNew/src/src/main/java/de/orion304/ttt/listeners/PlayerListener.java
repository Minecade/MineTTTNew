package src.main.java.de.orion304.ttt.listeners;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.EnumClientCommand;
import net.minecraft.server.v1_7_R1.PacketPlayInClientCommand;

import org.bukkit.ChatColor;
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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
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
import src.main.java.de.orion304.ttt.main.MineTTT;
import src.main.java.de.orion304.ttt.main.TempBlock;
import src.main.java.de.orion304.ttt.main.Tools;
import src.main.java.de.orion304.ttt.players.DeathLocation;
import src.main.java.de.orion304.ttt.players.PlayerTeam;
import src.main.java.de.orion304.ttt.players.TTTPlayer;

public class PlayerListener implements Listener {

	public static final ItemStack nugget = new ItemStack(Material.GOLD_NUGGET,
			1);

	private final ArrayList<Player> deadPlayers = new ArrayList<Player>();

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

	private EntityPlayer getPlayer(Player player) {
		return ((CraftPlayer) player).getHandle();
	}

	private void giveNugget(Player player) {
		player.getInventory().addItem(nugget);
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

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		TTTPlayer.handleBlockPlace(player, event.getBlock());
	}

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

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		event.getNewItems().clear();
		event.setCancelled(true);
	}

	@EventHandler
	public void onInventoryInteract(InventoryInteractEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onInventoryMoveItem(InventoryMoveItemEvent event) {
		event.setCancelled(true);
		event.setItem(null);
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		InventoryHolder holder = event.getInventory().getHolder();
		if (holder instanceof Chest) {
			this.plugin.chestHandler.handleChest((Chest) holder);
		}
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
			}
		}
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		this.chatManager.handleChat(player, event.getMessage());
		event.setCancelled(true);
	}

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
				// lookDead(player, block);
				this.deadPlayers.add(player);
				new TempBlock(block, Material.SKULL);
				BlockState state = block.getState();
				Skull skull = (Skull) state;
				skull.setOwner(player.getDisplayName());
				skull.setSkullType(SkullType.PLAYER);
				skull.update(true, true);

			}

			if (playerTeam != PlayerTeam.NONE && damagerTeam != PlayerTeam.NONE) {
				if ((damagerTeam == PlayerTeam.INNOCENT || damagerTeam == PlayerTeam.DETECTIVE)
						&& playerTeam != PlayerTeam.TRAITOR) {
					TrecentDamager.loseKarma();
				}

				if ((damagerTeam != PlayerTeam.TRAITOR && playerTeam == PlayerTeam.TRAITOR)) {
					Tplayer.addKarma();
					giveNugget(recentDamager);
				}

				if (damagerTeam == PlayerTeam.TRAITOR
						&& playerTeam != PlayerTeam.TRAITOR) {
					TrecentDamager.addKarma();
					giveNugget(recentDamager);
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

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		TTTPlayer.handleInteract(player);
	}

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

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		boolean cancel = TTTPlayer.handleItemPickup(event.getPlayer());
		if (cancel) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		TTTPlayer.handleLeave(player);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		event.setRespawnLocation(this.plugin.thread.getLobbyLocation());
	}

	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent event) {

	}

	public void resetDeadPlayers() {
		for (Player player : this.deadPlayers) {
			// lookAlive(player);
		}
		this.deadPlayers.clear();
	}
}
