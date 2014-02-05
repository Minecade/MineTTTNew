package src.main.java.de.orion304.ttt.listeners;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_7_R1.EnumClientCommand;
import net.minecraft.server.v1_7_R1.PacketPlayInClientCommand;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
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
		plugin = instance;
		chatManager = new ChatManager(instance);
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		chatManager.handleChat(player, event.getMessage());
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		String name = event.getPlayer().getName();
		if (plugin.minecade.isPlayerBanned(name)) {
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
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		TTTPlayer.handleJoin(player);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		TTTPlayer.handleInteract(player);
	}

	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		Tools.verbose(event);
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		event.setRespawnLocation(plugin.thread.getLobbyLocation());
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!plugin.thread.isGameRunning()) {
			event.setCancelled(true);
			return;
		}

		Entity damageDealer = event.getDamager();
		Entity damageTaker = event.getEntity();

		if (damageDealer instanceof Player && damageTaker instanceof Player) {
			Player damager = (Player) damageDealer;
			Player player = (Player) damageTaker;

			TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(player);
			TTTPlayer Tdealer = TTTPlayer.getTTTPlayer(damager);

			if (Tdealer.getTeam() == PlayerTeam.NONE) {
				event.setCancelled(true);
				return;
			}

			Tplayer.setRecentDamager(damager);
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		boolean cancel = TTTPlayer.handleItemPickup(event.getPlayer());
		if (cancel)
			event.setCancelled(true);
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		if (plugin.thread.isGameRunning()) {
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
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			boolean cancel = TTTPlayer.handleInventoryClick(
					(Player) event.getWhoClicked(), event.getSlot());
			if (cancel)
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (plugin.thread.isGameRunning()) {
			Player p = event.getPlayer();
			TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(p);
			if (Tplayer.getTeam() == PlayerTeam.NONE)
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockDamage(BlockDamageEvent event) {
		if (plugin.thread.isGameRunning()) {
			Player p = event.getPlayer();
			TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(p);
			if (Tplayer.getTeam() == PlayerTeam.NONE)
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void autoRespawn(final PlayerDeathEvent event) {
		// auto-respawn
		new BukkitRunnable() {
			public void run() {
				((CraftPlayer) event.getEntity()).getHandle().playerConnection
						.a(new PacketPlayInClientCommand(
								EnumClientCommand.PERFORM_RESPAWN));
			}
		}.runTaskLater(plugin, 1L);
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {

		Player player = event.getEntity();
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
				if (block == null)
					block = player.getEyeLocation().getBlock();
				new TempBlock(block, Material.SKULL);
				BlockState state = block.getState();
				Skull skull = (Skull) state;
				skull.setOwner(player.getDisplayName());
				skull.setSkullType(SkullType.PLAYER);
				skull.update(true, true);

			}

			if ((damagerTeam == PlayerTeam.INNOCENT || damagerTeam == PlayerTeam.DETECTIVE)
					&& playerTeam != PlayerTeam.TRAITOR) {
				TrecentDamager.loseKarma();
			}

			if (playerTeam != PlayerTeam.TRAITOR
					|| playerTeam != PlayerTeam.NONE) {
				Tplayer.addKarma();
			}

			if (playerTeam == PlayerTeam.TRAITOR
					&& damagerTeam != PlayerTeam.TRAITOR) {
				TrecentDamager.addKarma();
			}

			if (playerTeam == PlayerTeam.TRAITOR
					&& damagerTeam == PlayerTeam.TRAITOR) {
				TrecentDamager.loseKarma();
			}

			deathMessage = player.getName()
					+ " was slain, and revealed to be a";
			if (playerTeam == PlayerTeam.INNOCENT)
				deathMessage += "n";
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
		ArrayList<ItemStack> toRemove = new ArrayList<>();
		for (int i = 0; i < drops.size(); i++) {

		}
		drops.clear();

		TTTPlayer.handleDeath(player);

	}

	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		TTTPlayer.handleLeave(player);
	}
}
