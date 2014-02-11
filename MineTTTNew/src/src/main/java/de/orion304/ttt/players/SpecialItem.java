package src.main.java.de.orion304.ttt.players;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import src.main.java.de.orion304.ttt.listeners.PlayerListener;

public class SpecialItem {

	private Material material;
	private String displayName;
	private int cost, numberOfUses;
	private List<String> lore = new ArrayList<>();
	private Power power;
	private DamageCause[] sources;
	private long duration;

	private int uses;
	private long starttime;

	/**
	 * Creates a special item, with these properties.
	 * 
	 * @param material
	 *            Material of the item.
	 * @param displayName
	 *            Its display name.
	 * @param cost
	 *            The cost of the item in buttercoins.
	 * @param power
	 *            Its special power.
	 * @param sources
	 *            DamageCauses that trigger the power (if applicable)
	 * @param numberOfUses
	 *            The number of uses it has before it is destroyed, -1 means it
	 *            is never destroyed.
	 * @param duration
	 *            How long the item lasts, in ms, before it disappears. -1 means
	 *            infinite duration.
	 * @param itemLore
	 *            Its lore.
	 */
	public SpecialItem(Material material, String displayName, int cost,
			Power power, DamageCause[] sources, int numberOfUses,
			long duration, String... itemLore) {
		this.material = material;
		this.displayName = displayName;
		this.cost = cost;
		this.sources = sources;
		lore.addAll(Arrays.asList(itemLore));
		this.power = power;
		this.numberOfUses = numberOfUses;
		this.duration = duration;
	}

	public ItemStack getItemInShop(Inventory inventory) {
		ItemStack item = new ItemStack(material, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayName);

		List<String> itemLore = new ArrayList<>();
		itemLore.addAll(lore);
		String string = "Costs " + cost + " Golden Nuggets";
		String color = ChatColor.RED.toString();
		if (alreadyHas(inventory)) {
			string += ChatColor.RESET + color
					+ " (You may only have 1 at a time)";
			color = color.toString() + ChatColor.STRIKETHROUGH;
		} else if (inventory.containsAtLeast(PlayerListener.nugget, cost))
			color = ChatColor.GREEN.toString();

		itemLore.add(0, color + string);
		meta.setLore(itemLore);
		item.setItemMeta(meta);
		return item;
	}

	public ItemStack getItemInInventory() {
		ItemStack item = new ItemStack(material, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayName);

		List<String> itemLore = new ArrayList<>();
		itemLore.addAll(lore);
		meta.setLore(itemLore);
		item.setItemMeta(meta);
		return item;
	}

	public String getDisplayName() {
		return displayName;
	}

	public int getCost() {
		return cost;
	}

	public int getUses() {
		return numberOfUses;
	}

	public void use() {
		if (numberOfUses > 0)
			numberOfUses -= 1;
	}

	public Power getPower() {
		return power;
	}

	public boolean alreadyHas(Inventory inventory) {
		for (ItemStack item : inventory.getContents()) {
			if (item == null)
				continue;
			if (item.getItemMeta() == null)
				continue;
			if (item.getItemMeta().getDisplayName() == getDisplayName())
				return true;
		}
		return false;
	}

	public boolean isCauseApplicable(DamageCause cause) {
		return Arrays.asList(sources).contains(cause);
	}

}
