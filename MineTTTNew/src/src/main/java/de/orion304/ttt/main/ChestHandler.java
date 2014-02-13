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

	public ChestHandler(MineTTT instance) {
		this.plugin = instance;
	}

	private void despawnChest(Chest chest) {
		chest.getInventory().clear();
		chest.getBlock().setType(Material.AIR);
		chest.setType(Material.CHEST);
		this.chests.put(chest, -System.currentTimeMillis());
	}

	private ItemStack getAmmunition() {
		return new ItemStack(Material.ARROW, this.random.nextInt(64));
	}

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

	private ItemStack[] getRandomItems() {
		List<ItemStack> items = new ArrayList<ItemStack>();
		for (int i = 0; i < getNumberOfItems(); i++) {
			items.add(getRandomItem());
		}
		return items.toArray(new ItemStack[items.size()]);
	}

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

	public void handleChest(Chest chest) {
		if (this.chests.contains(chest)) {
			return;
		} else {
			this.chests.put(chest, System.currentTimeMillis());
			populateChest(chest);
		}
	}

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

	private boolean isEmpty(Inventory inventory) {
		for (ItemStack item : inventory.getContents()) {
			if (item != null) {
				return false;
			}
		}
		return true;
	}

	private void populateChest(Chest chest) {
		Inventory inventory = chest.getInventory();
		if (isEmpty(inventory)) {
			inventory.addItem(getRandomItems());
		}
	}

	public void resetChests() {
		for (Chest chest : this.chests.keySet()) {
			long lasttime = this.chests.get(chest);
			if (lasttime < 0) {
				respawnChest(chest);
			}
		}
		this.chests.clear();
	}

	private void respawnChest(Chest chest) {
		chest.update(true);
		populateChest(chest);
		this.chests.remove(chest);
	}

}
