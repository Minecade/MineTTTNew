package src.main.java.de.orion304.ttt.main;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import src.main.java.de.orion304.ttt.players.DeathLocation;

public class Tools {

	private static final Integer[] nonOpaque = { 0, 6, 8, 9, 10, 11, 27, 28,
			30, 31, 32, 37, 38, 39, 40, 50, 51, 55, 59, 66, 68, 69, 70, 72, 75,
			76, 77, 78, 83, 90, 93, 94, 104, 105, 106, 111, 115, 119, 127, 131,
			132 };

	private static final BlockFace[] axis = { BlockFace.NORTH, BlockFace.EAST,
			BlockFace.SOUTH, BlockFace.WEST };
	private static final BlockFace[] radial = { BlockFace.NORTH,
			BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST,
			BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST,
			BlockFace.NORTH_WEST };

	private static final double detectiveRange = 5;

	public static <T> void verbose(T something) {
		if (something == null)
			System.out.println("null");
		else
			System.out.println(something.toString());
	}

	public static Player getKiller(Player player) {
		double longestr = detectiveRange + 1;
		Player target = null;
		Location origin = player.getEyeLocation();
		Vector direction = player.getEyeLocation().getDirection().normalize();
		for (DeathLocation location : DeathLocation.getDeathLocations()) {
			Location loc = location.getDeathLocation();
			if (!loc.getWorld().equals(player.getWorld()))
				continue;
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
				if (!player.canSee((Player) entity))
					continue;
				target = (Player) entity;

				longestr = entity.getLocation().distance(origin);
			}
		}
		return target;
	}

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
					solidblock = block;
				}
			} else {
				solidblock = block;
			}
		}
		return null;
	}

	public static boolean isSolid(Block block) {
		return !isTransparent(block);
	}

	public static boolean isTransparent(Block block) {
		return Arrays.asList(nonOpaque).contains(block.getTypeId());
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
