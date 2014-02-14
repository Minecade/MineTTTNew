package src.main.java.de.orion304.ttt.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ChestHandler {

	private final MineTTT plugin;

	private final ConcurrentHashMap<Chest, Long> chests = new ConcurrentHashMap<>();
	private final Random random = new Random();
	private final long respawnDuration = 15 * 1000L;
	private final long duration = 5 * 1000L;

	/**
	 * Creats a new ChestHandler object, for handling the opening, closing,
	 * populating and respawning of chests in the game.
	 * 
	 * @param instance
	 *            The MineTTT instance.
	 */
	public ChestHandler(MineTTT instance) {
		this.plugin = instance;
	}

	/**
	 * Sets the chest to despawn, and allows it to respawn at a set time later.
	 * 
	 * @param chest
	 *            The chest to despawn.
	 */
	private void despawnChest(Chest chest) {
		chest.getInventory().clear();
		chest.getBlock().setType(Material.AIR);
		chest.setType(Material.CHEST);
		this.chests.put(chest, -System.currentTimeMillis());
	}

	/**
	 * Returns a random amount of Arrows.
	 * 
	 * @return A random stack of arrows.
	 */
	private ItemStack getAmmunition() {
		return new ItemStack(Material.ARROW, this.random.nextInt(64));
	}

	/**
	 * Gets the number of items to be generated inside the chest.
	 * 
	 * @return The number of generated items.
	 */
	private int getNumberOfItems() {
		double choice = this.random.nextDouble();
		if (choice < .1) {
			return 4;
		} else if (choice < .3) {
			return 3;
		} else if (choice < .7) {
			return 2;
		} else if (choice < .95) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * Gets a random armor, was originally all types, now only leather.
	 * 
	 * @return A random piece of armor with some durability.
	 */
	private ItemStack getRandomArmor() {
		String[] slots = { "CHESTPLATE", "HELMET", "LEGGINGS", "BOOTS" };
		String slot = slots[this.random.nextInt(slots.length)];
		double choice = this.random.nextDouble();
		String type;
		if (choice < .05) {
			type = "CHAINMAIL";
		} else if (choice < .15) {
			type = "DIAMOND";
		} else if (choice < .45) {
			type = "IRON";
		} else {
			type = "LEATHER";
		}
		type = "LEATHER";
		Material material = Material.getMaterial(type + "_" + slot);
		ItemStack item = new ItemStack(material, 1);
		short durability = item.getType().getMaxDurability();
		durability = (short) (this.random.nextInt(durability) + 1);
		item.setDurability(durability);
		return item;
	}

	/**
	 * Gets a random item.
	 * 
	 * @return A random item for the chest to have.
	 */
	private ItemStack getRandomItem() {
		double choice = this.random.nextDouble();
		if (choice < .5) {
			return getRandomArmor();
		} else if (choice < .8) {
			return getRandomWeapon();
		} else {
			return getAmmunition();
		}
	}

	/**
	 * Gets all random items the chest will be populated with.
	 * 
	 * @return An array of items.
	 */
	private ItemStack[] getRandomItems() {
		List<ItemStack> items = new ArrayList<ItemStack>();
		for (int i = 0; i < getNumberOfItems(); i++) {
			items.add(getRandomItem());
		}
		return items.toArray(new ItemStack[items.size()]);
	}

	/**
	 * Returns a random weapon with some durability.
	 * 
	 * @return A random weapon.
	 */
	private ItemStack getRandomWeapon() {
		double choice = this.random.nextDouble();
		ItemStack item;
		if (choice < .75) {
			item = new ItemStack(Material.IRON_SWORD, 1);
		} else if (choice < .85) {
			item = new ItemStack(Material.IRON_SWORD, 1);
		} else {
			item = new ItemStack(Material.BOW, 1);
		}
		short durability = item.getType().getMaxDurability();
		durability = (short) (this.random.nextInt(durability) + 1);
		item.setDurability(durability);
		return item;
	}

	/**
	 * Called by the listener, this handles the players' opening of chests.
	 * 
	 * @param chest
	 */
	public void handleChest(Chest chest) {
		if (this.chests.contains(chest)) {
			return;
		} else {
			this.chests.put(chest, System.currentTimeMillis());
			populateChest(chest);
		}
	}

	/**
	 * Called by the main thread, this handles all time-sensitive things about
	 * the chests (despawning, respawning, and waiting).
	 */
	public void handleChests() {
		long time = System.currentTimeMillis();
		for (Chest chest : this.chests.keySet()) {
			long lasttime = this.chests.get(chest);
			if (lasttime < 0) {
				if (time > this.respawnDuration - lasttime) {
					respawnChest(chest);
				}
				continue;
			}
			if (!chest.getInventory().getViewers().isEmpty()) {
				this.chests.put(chest, time);
				continue;
			}
			if (time > lasttime + this.duration) {
				despawnChest(chest);
				continue;
			}
		}
	}

	/**
	 * As Bukkit doesn't have an isEmpty() method on inventories, this is the
	 * one I made. Checks if the inventory is empty (all item in it are null).
	 * 
	 * @param inventory
	 *            The inventory to check.
	 * @return True if all contents are null.
	 */
	private boolean isEmpty(Inventory inventory) {
		for (ItemStack item : inventory.getContents()) {
			if (item != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Populates a chest with random items.
	 * 
	 * @param chest
	 *            The chest to populate.
	 */
	private void populateChest(Chest chest) {
		Inventory inventory = chest.getInventory();
		if (isEmpty(inventory)) {
			inventory.addItem(getRandomItems());
		}
	}

	/**
	 * Called when a server reload or restarts, forces all chests to respawn.
	 */
	public void resetChests() {
		for (Chest chest : this.chests.keySet()) {
			long lasttime = this.chests.get(chest);
			if (lasttime < 0) {
				respawnChest(chest);
			}
		}
		this.chests.clear();
	}

	/**
	 * Respawns a chest and populates it with items.
	 * 
	 * @param chest
	 *            The chest to respawn.
	 */
	private void respawnChest(Chest chest) {
		chest.update(true);
		populateChest(chest);
		this.chests.remove(chest);
	}

}
