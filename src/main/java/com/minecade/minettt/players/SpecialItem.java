package com.minecade.minettt.players;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.minecade.minettt.listeners.PlayerListener;
import com.minecade.minettt.main.MineTTT;

public class SpecialItem {

	private final Material material;
	private final String displayName;
	private final int cost;
	private int numberOfUses;
	private final List<String> lore = new ArrayList<>();
	private final Power power;
	private final DamageCause[] sources;
	private final long duration;

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
		this.lore.addAll(Arrays.asList(itemLore));
		this.power = power;
		this.numberOfUses = numberOfUses;
		this.duration = duration;
	}

	/**
	 * Checks if an inventory already has an item of this type.
	 * 
	 * @param inventory
	 *            The inventory to check.
	 * @return True if the inventory has an item of this type.
	 */
	public boolean alreadyHas(Inventory inventory) {
		for (ItemStack item : inventory.getContents()) {
			if (item == null) {
				continue;
			}
			if (item.getItemMeta() == null) {
				continue;
			}
			if (item.getItemMeta().getDisplayName() == getDisplayName()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the cost of this item (in gold nuggets).
	 * 
	 * @return The cost of the item.
	 */
	public int getCost() {
		return this.cost;
	}

	/**
	 * Gets the display name of the special item.
	 * 
	 * @return The display name.
	 */
	public String getDisplayName() {
		return this.displayName;
	}

	/**
	 * Gets the ItemStack that the will go into the player's inventory once the
	 * SpecialItem is purchased.
	 * 
	 * @return The item to go in the player's inventory.
	 */
	public ItemStack getItemInInventory() {
		ItemStack item = new ItemStack(this.material, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(this.displayName);

		List<String> itemLore = new ArrayList<>();
		itemLore.addAll(this.lore);
		meta.setLore(itemLore);
		item.setItemMeta(meta);
		return item;
	}

	/**
	 * Gets the ItemStack that represents this special item in a player's
	 * inventory.
	 * 
	 * @param inventory
	 *            The inventory of the player looking at the shop.
	 * @return The approriate item to place in the shop.
	 */
	public ItemStack getItemInShop(Inventory inventory) {
		ItemStack item = new ItemStack(this.material, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(this.displayName);

		List<String> itemLore = new ArrayList<>();
		itemLore.addAll(this.lore);
		String string = String.format(MineTTT.getPlugin().getMessage("specialitem.cost"), this.cost); 
		String color = ChatColor.RED.toString();
		if (alreadyHas(inventory)) {
			string += ChatColor.RESET + color
					+ MineTTT.getPlugin().getMessage("specialitem.item");
			color = color.toString() + ChatColor.STRIKETHROUGH;
		} else if (inventory.containsAtLeast(PlayerListener.nugget, this.cost)) {
			color = ChatColor.GREEN.toString();
		}

		itemLore.add(0, color + string);
		meta.setLore(itemLore);
		item.setItemMeta(meta);
		return item;
	}

	/**
	 * Gets the Power of the special item.
	 * 
	 * @return The power.
	 */
	public Power getPower() {
		return this.power;
	}

	/**
	 * Gets the maximum number of uses of this item.
	 * 
	 * @return The maximum number of uses.
	 */
	public int getUses() {
		return this.numberOfUses;
	}

	/**
	 * Checks to see if a DamageCause was a trigger for this item.
	 * 
	 * @param cause
	 *            The damage cause to check.
	 * @return True if the damage cause is applicable to this item.
	 */
	public boolean isCauseApplicable(DamageCause cause) {
		return Arrays.asList(this.sources).contains(cause);
	}

	/**
	 * Uses the item, setting its number of uses to be 1 lower than it is.
	 */
	public void use() {
		if (this.numberOfUses > 0) {
			this.numberOfUses -= 1;
		}
	}

}
