package src.main.java.de.orion304.ttt.main;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import src.main.java.de.orion304.ttt.players.DeathLocation;

public class Tools {

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
