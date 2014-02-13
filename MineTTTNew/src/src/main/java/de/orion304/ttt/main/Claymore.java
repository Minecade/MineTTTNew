package src.main.java.de.orion304.ttt.main;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import src.main.java.de.orion304.ttt.players.PlayerTeam;
import src.main.java.de.orion304.ttt.players.TTTPlayer;

public class Claymore {

	private static ConcurrentHashMap<Block, Claymore> claymores = new ConcurrentHashMap<>();
	private static final long prepTime = 5 * 1000L;
	private static final double radius = 4;

	public static void handleClaymores() {
		long time = System.currentTimeMillis();
		Player[] players = Bukkit.getOnlinePlayers();
		for (Block block : claymores.keySet()) {
			Claymore claymore = claymores.get(block);
			if (!claymore.displayed) {
				claymore.showClaymore();
			}
			if (claymore.armed) {
				for (Player player : players) {
					if (block.getWorld() != player.getWorld()) {
						Tools.verbose(player.getName()
								+ " isn't in the same world?");
						continue;
					}
					if (block.getLocation().distance(player.getLocation()) < radius) {
						claymore.explode();
						break;
					}
				}
			} else {
				if (time < claymore.placedTime + prepTime) {
					continue;
				} else {
					claymore.arm();
				}
			}

		}
	}

	public static void reset() {
		Player[] players = Bukkit.getOnlinePlayers();
		for (Block block : claymores.keySet()) {
			for (Player player : players) {
				player.sendBlockChange(block.getLocation(), Material.AIR,
						(byte) 0);
			}
		}
		claymores.clear();
	}

	public static void showClaymores(TTTPlayer Tplayer) {
		PlayerTeam team = Tplayer.getTeam();
		Player player = Tplayer.getPlayer();
		if (player == null) {
			return;
		}
		if (team == PlayerTeam.NONE || team == PlayerTeam.TRAITOR) {
			for (Block block : claymores.keySet()) {
				player.sendBlockChange(block.getLocation(), Material.TNT,
						(byte) 0);
			}
		}
	}

	private final Block block;

	private final long placedTime;

	private boolean armed = false;
	private boolean displayed = false;

	public Claymore(Block block) {
		this.block = block;
		this.placedTime = System.currentTimeMillis();
		claymores.put(block, this);
	}

	private void arm() {
		showClaymore();
		this.armed = true;
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(player);
			if (Tplayer.getTeam() == PlayerTeam.NONE
					|| Tplayer.getTeam() == PlayerTeam.TRAITOR) {
				player.playSound(this.block.getLocation(), Sound.CREEPER_HISS,
						1, 1);
			}
		}
	}

	private void explode() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getWorld() == this.block.getWorld()) {
				if (player.getLocation().distance(this.block.getLocation()) < radius) {
					player.damage(Double.MAX_VALUE);
				}
			}
			player.sendBlockChange(this.block.getLocation(), Material.AIR,
					(byte) 0);
		}
		this.block.getWorld().createExplosion(this.block.getLocation(), 1);
		claymores.remove(this.block);
	}

	private void showClaymore() {
		this.displayed = true;
		for (Player player : Bukkit.getOnlinePlayers()) {
			TTTPlayer Tplayer = TTTPlayer.getTTTPlayer(player);
			if (Tplayer.getTeam() == PlayerTeam.TRAITOR
					|| Tplayer.getTeam() == PlayerTeam.NONE) {
				player.sendBlockChange(this.block.getLocation(), Material.TNT,
						(byte) 0);
			}
		}
	}

}
