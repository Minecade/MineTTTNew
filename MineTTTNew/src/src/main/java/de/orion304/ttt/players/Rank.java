package src.main.java.de.orion304.ttt.players;

import org.bukkit.ChatColor;

public enum Rank {

	GM(ChatColor.AQUA, 3), MGM(ChatColor.GOLD, 3), Developer(ChatColor.RED, 3), Owner(
			ChatColor.DARK_RED, 4), VIP(ChatColor.GREEN, 1), PRO(
			ChatColor.BLUE, 2), NONE(ChatColor.WHITE, 0);

	public static Rank getRank(String string) {
		for (Rank rank : values()) {
			if (rank.name().equalsIgnoreCase(string)) {
				return rank;
			}
		}
		return null;
	}

	private ChatColor color;
	private int tier;

	Rank(ChatColor color, int tier) {
		this.color = color;
		this.tier = tier;
	}

	public int getTier() {
		return this.tier;
	}

	@Override
	public String toString() {
		if (this == Rank.NONE) {
			return "";
		}
		return this.color.toString() + ChatColor.BOLD.toString() + name()
				+ ChatColor.RESET.toString();
	}

}
