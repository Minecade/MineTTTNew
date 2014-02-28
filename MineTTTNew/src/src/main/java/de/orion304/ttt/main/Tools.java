package src.main.java.de.orion304.ttt.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import src.main.java.de.orion304.ttt.players.DeathLocation;

public class Tools {

	private static final Integer[] nonOpaque = { 0, 6, 8, 9, 27, 28, 30, 31,
			32, 37, 38, 39, 40, 50, 55, 59, 66, 70, 72, 75, 76, 77, 78, 83, 90,
			93, 94, 104, 105, 106, 111, 115, 119, 127, 131, 132 };

	private static final BlockFace[] axis = { BlockFace.NORTH, BlockFace.EAST,
			BlockFace.SOUTH, BlockFace.WEST };
	private static final BlockFace[] radial = { BlockFace.NORTH,
			BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST,
			BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST,
			BlockFace.NORTH_WEST };

	private static final double detectiveRange = 5;
	private static final Random random = new Random();

	/**
	 * Returns a random object from the list.
	 * 
	 * @param list
	 *            The list.
	 * @return The random object, or null if the list is empty.
	 */
	public static <T> T chooseFromList(List<T> list) {
		int i = chooseIndexFromList(list);
		if (i >= 0) {
			return list.get(i);
		}
		return null;
	}

	public static <T> T chooseFromSet(Set<T> set) {
		List<T> list = new ArrayList<T>();
		list.addAll(set);
		return chooseFromList(list);
	}

	/**
	 * Chooses a random index from the given list.
	 * 
	 * @param list
	 *            The list.
	 * @return The random index, from 0 to the list's size. Returns -1 if the
	 *         list is empty.
	 */
	public static int chooseIndexFromList(List<?> list) {
		int size = list.size();
		if (size == 0) {
			return -1;
		}
		if (size == 1) {
			return 0;
		}
		return random.nextInt(size);
	}

	/**
	 * Clears a player's inventory, including their armoring.
	 * 
	 * @param player
	 *            The player's inventory to clear.
	 */
	public static void clearInventory(Player player) {
		PlayerInventory inventory = player.getInventory();
		inventory.clear();
		inventory.setArmorContents(null);
	}

	/**
	 * Gets the distance of the point from a line specified by a vector and a
	 * point on that line.
	 * 
	 * @param line
	 *            The vector showing the direction of the line.
	 * @param pointonline
	 *            Any point on that line.
	 * @param point
	 *            The point of interest to find the distance of.
	 * @return The distance from the point to the line.
	 */
	public static double getDistanceFromLine(Vector line, Location pointonline,
			Location point) {

		Vector AP = new Vector();
		double Ax, Ay, Az;
		Ax = pointonline.getX();
		Ay = pointonline.getY();
		Az = pointonline.getZ();

		double Px, Py, Pz;
		Px = point.getX();
		Py = point.getY();
		Pz = point.getZ();

		AP.setX(Px - Ax);
		AP.setY(Py - Ay);
		AP.setZ(Pz - Az);

		return (AP.crossProduct(line).length()) / (line.length());
	}

	/**
	 * Returns the a block that is no farther than maxdistance away from the
	 * given location that a player can teleport to without suffocating or
	 * falling.
	 * 
	 * @param location
	 *            The location to search.
	 * @param maxdistance
	 *            The max distance from the location to search (vertically).
	 * @return The block a player can be teleported to, or null if there are
	 *         none.
	 */
	public static Block getFloor(Location location, int maxdistance) {
		Block startblock = location.getBlock();
		Block solidblock = null;
		boolean air = false;
		for (int i = -maxdistance; i < maxdistance; i++) {
			Block block = startblock.getRelative(BlockFace.UP, i);
			if (isTransparent(block)) {
				if (solidblock != null) {
					if (air) {
						return block.getRelative(BlockFace.DOWN);
					}
					air = true;
				} else {
					air = false;
				}
			} else {
				solidblock = block;
				air = false;
			}
		}
		return null;
	}

	/**
	 * Get the player which killed this player.
	 * 
	 * @param player
	 *            The player that died.
	 * @return The player that killed them.
	 */
	public static Player getKiller(Player player) {
		double longestr = detectiveRange + 1;
		Player target = null;
		Location origin = player.getEyeLocation();
		Vector direction = player.getEyeLocation().getDirection().normalize();
		for (DeathLocation location : DeathLocation.getDeathLocations()) {
			Location loc = location.getDeathLocation();
			if (!loc.getWorld().equals(player.getWorld())) {
				continue;
			}
			if (loc.distance(origin) < detectiveRange
					&& loc.distance(origin) < longestr
					&& getDistanceFromLine(direction, origin, loc) < 2
					&& loc.distance(origin.clone().add(direction)) < loc
							.distance(origin.clone().add(
									direction.clone().multiply(-1)))) {
				target = location.getKiller();
				longestr = loc.distance(origin);
			}
		}
		return target;
	}

	/**
	 * Gets the Living Entity that is being targeted by the player.
	 * 
	 * @param player
	 *            The player doing the targeting.
	 * @param range
	 *            The max range of the targeting.
	 * @return The targeted entity.
	 */
	public static Entity getTarget(Player player, double range) {
		double longestr = range + 1;
		Entity target = null;
		Location origin = player.getEyeLocation();
		Vector direction = player.getEyeLocation().getDirection().normalize();
		for (Entity entity : origin.getWorld().getEntities()) {
			if (entity.getLocation().distance(origin) < longestr
					&& getDistanceFromLine(direction, origin,
							entity.getLocation()) < 2
					&& (entity instanceof LivingEntity)
					&& entity.getEntityId() != player.getEntityId()
					&& entity.getLocation().distance(
							origin.clone().add(direction)) < entity
							.getLocation().distance(
									origin.clone().add(
											direction.clone().multiply(-1)))) {
				target = entity;
				longestr = entity.getLocation().distance(origin);
			}
		}
		return target;
	}

	/**
	 * Gets the Player that is being targeted by the player.
	 * 
	 * @param player
	 *            The player doing the targeting.
	 * @param range
	 *            The max range of the targeting.
	 * @return The targeted player.
	 */
	public static Player getTargetPlayer(Player player, int range) {
		double longestr = range + 1;
		Player target = null;
		Location origin = player.getEyeLocation();
		Vector direction = player.getEyeLocation().getDirection().normalize();
		for (Entity entity : origin.getWorld().getEntities()) {
			if (entity.getLocation().distance(origin) < longestr
					&& getDistanceFromLine(direction, origin,
							entity.getLocation()) < 2
					&& (entity instanceof Player)
					&& entity.getEntityId() != player.getEntityId()
					&& entity.getLocation().distance(
							origin.clone().add(direction)) < entity
							.getLocation().distance(
									origin.clone().add(
											direction.clone().multiply(-1)))) {
				if (!player.canSee((Player) entity)) {
					continue;
				}
				target = (Player) entity;

				longestr = entity.getLocation().distance(origin);
			}
		}
		return target;
	}

	/**
	 * Returns true if player1 is behind player2
	 * 
	 * @param player1
	 *            The player to check their position.
	 * @param player2
	 *            The player to check which direction they're facing.
	 * @return True if player1 is behind player2.
	 */
	public static boolean isBehind(Player player1, Player player2) {
		Vector v1 = player1.getEyeLocation().getDirection();
		v1.setY(0);
		v1.normalize();

		Vector v2 = player2.getEyeLocation().getDirection();
		v2.setY(0);
		v2.normalize();

		double dot = v1.dot(v2);
		if (dot > 0) {
			return true;
		}

		return false;
	}

	/**
	 * Checks if the block is opaque (Players cannot see through it).
	 * 
	 * @param block
	 *            Block to check.
	 * @return True if players cannot see through the block, false otherwise.
	 */
	public static boolean isSolid(Block block) {
		return !isTransparent(block);
	}

	/**
	 * Checks if the block is transparent (Players can see through it).
	 * 
	 * @param block
	 *            Block to check.
	 * @return True if players can see through the block, false otherwise.
	 */
	public static boolean isTransparent(Block block) {
		return Arrays.asList(nonOpaque).contains(block.getTypeId());
	}

	/**
	 * Prints the object to the console.
	 * 
	 * @param something
	 *            The object to print to the console.
	 */
	public static <T> void verbose(T something) {
		if (something == null) {
			System.out.println("null");
		} else {
			System.out.println(something.toString());
		}
	}

	/**
	 * Gets the horizontal Block Face from a given yaw angle<br>
	 * This includes the NORTH_WEST faces
	 * 
	 * @param yaw
	 *            angle
	 * @return The Block Face of the angle
	 */
	public static BlockFace yawToFace(float yaw) {
		return yawToFace(yaw, true);
	}

	/**
	 * Gets the horizontal Block Face from a given yaw angle
	 * 
	 * @param yaw
	 *            angle
	 * @param useSubCardinalDirections
	 *            setting, True to allow NORTH_WEST to be returned
	 * @return The Block Face of the angle
	 */
	public static BlockFace yawToFace(float yaw,
			boolean useSubCardinalDirections) {
		if (useSubCardinalDirections) {
			return radial[Math.round(yaw / 45f) & 0x7];
		} else {
			return axis[Math.round(yaw / 90f) & 0x3];
		}
	}

}
