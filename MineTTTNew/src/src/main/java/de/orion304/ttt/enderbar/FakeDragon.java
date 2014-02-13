package src.main.java.de.orion304.ttt.enderbar;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Location;

public class FakeDragon {
	public static final float MAX_HEALTH = 200;
	private int x;
	private int y;
	private int z;

	private int pitch = 0;
	private int yaw = 0;
	private byte xvel = 0;
	private byte yvel = 0;
	private byte zvel = 0;
	public float health = 0;
	private boolean visible = false;
	public String name;
	private Object world;

	private Object dragon;

	private int id;

	public FakeDragon(String name, Location loc) {
		this.name = name;
		this.x = loc.getBlockX();
		this.y = loc.getBlockY();
		this.z = loc.getBlockZ();
		this.world = Util.getHandle(loc.getWorld());
	}

	public FakeDragon(String name, Location loc, int percent) {
		this.name = name;
		this.x = loc.getBlockX();
		this.y = loc.getBlockY();
		this.z = loc.getBlockZ();
		this.health = percent / 100F * MAX_HEALTH;
		this.world = Util.getHandle(loc.getWorld());
	}

	public Object getDestroyPacket() {
		Class<?> PacketPlayOutEntityDestroy = Util
				.getCraftClass("PacketPlayOutEntityDestroy");

		Object packet = null;
		try {
			packet = PacketPlayOutEntityDestroy.newInstance();
			Field a = PacketPlayOutEntityDestroy.getDeclaredField("a");
			a.setAccessible(true);
			a.set(packet, new int[] { this.id });
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		return packet;
	}

	public float getMaxHealth() {
		return MAX_HEALTH;
	}

	public Object getMetaPacket(Object watcher) {
		Class<?> DataWatcher = Util.getCraftClass("DataWatcher");

		Class<?> PacketPlayOutEntityMetadata = Util
				.getCraftClass("PacketPlayOutEntityMetadata");

		Object packet = null;
		try {
			packet = PacketPlayOutEntityMetadata.getConstructor(
					new Class<?>[] { int.class, DataWatcher, boolean.class })
					.newInstance(this.id, watcher, true);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		return packet;
	}

	public int getPitch() {
		return this.pitch;
	}

	public Object getSpawnPacket() {
		Class<?> Entity = Util.getCraftClass("Entity");
		Class<?> EntityLiving = Util.getCraftClass("EntityLiving");
		Class<?> EntityEnderDragon = Util.getCraftClass("EntityEnderDragon");
		Object packet = null;
		try {
			this.dragon = EntityEnderDragon.getConstructor(
					Util.getCraftClass("World")).newInstance(getWorld());

			Method setLocation = Util.getMethod(EntityEnderDragon,
					"setLocation", new Class<?>[] { double.class, double.class,
							double.class, float.class, float.class });
			setLocation.invoke(this.dragon, getX(), getY(), getZ(), getPitch(),
					getYaw());

			Method setInvisible = Util.getMethod(EntityEnderDragon,
					"setInvisible", new Class<?>[] { boolean.class });
			setInvisible.invoke(this.dragon, isVisible());

			Method setCustomName = Util.getMethod(EntityEnderDragon,
					"setCustomName", new Class<?>[] { String.class });
			setCustomName.invoke(this.dragon, this.name);

			Method setHealth = Util.getMethod(EntityEnderDragon, "setHealth",
					new Class<?>[] { float.class });
			setHealth.invoke(this.dragon, this.health);

			Field motX = Util.getField(Entity, "motX");
			motX.set(this.dragon, getXvel());

			Field motY = Util.getField(Entity, "motX");
			motY.set(this.dragon, getYvel());

			Field motZ = Util.getField(Entity, "motX");
			motZ.set(this.dragon, getZvel());

			Method getId = Util.getMethod(EntityEnderDragon, "getId",
					new Class<?>[] {});
			this.id = (Integer) getId.invoke(this.dragon);

			Class<?> PacketPlayOutSpawnEntityLiving = Util
					.getCraftClass("PacketPlayOutSpawnEntityLiving");

			packet = PacketPlayOutSpawnEntityLiving.getConstructor(
					new Class<?>[] { EntityLiving }).newInstance(this.dragon);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		return packet;
	}

	public Object getTeleportPacket(Location loc) {
		Class<?> PacketPlayOutEntityTeleport = Util
				.getCraftClass("PacketPlayOutEntityTeleport");

		Object packet = null;

		try {
			packet = PacketPlayOutEntityTeleport.getConstructor(
					new Class<?>[] { int.class, int.class, int.class,
							int.class, byte.class, byte.class }).newInstance(
					this.id, loc.getBlockX() * 32, loc.getBlockY() * 32,
					loc.getBlockZ() * 32,
					(byte) ((int) loc.getYaw() * 256 / 360),
					(byte) ((int) loc.getPitch() * 256 / 360));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		return packet;
	}

	public Object getWatcher() {
		Class<?> Entity = Util.getCraftClass("Entity");
		Class<?> DataWatcher = Util.getCraftClass("DataWatcher");

		Object watcher = null;
		try {
			watcher = DataWatcher.getConstructor(new Class<?>[] { Entity })
					.newInstance(this.dragon);
			Method a = Util.getMethod(DataWatcher, "a", new Class<?>[] {
					int.class, Object.class });

			a.invoke(watcher, 0, isVisible() ? (byte) 0 : (byte) 0x20);
			a.invoke(watcher, 6, this.health);
			a.invoke(watcher, 7, 0);
			a.invoke(watcher, 8, (byte) 0);
			a.invoke(watcher, 10, this.name);
			a.invoke(watcher, 11, (byte) 1);
		} catch (IllegalArgumentException e) {

			e.printStackTrace();
		} catch (SecurityException e) {

			e.printStackTrace();
		} catch (InstantiationException e) {

			e.printStackTrace();
		} catch (IllegalAccessException e) {

			e.printStackTrace();
		} catch (InvocationTargetException e) {

			e.printStackTrace();
		} catch (NoSuchMethodException e) {

			e.printStackTrace();
		}
		return watcher;
	}

	public Object getWorld() {
		return this.world;
	}

	public int getX() {
		return this.x;
	}

	public byte getXvel() {
		return this.xvel;
	}

	public int getY() {
		return this.y;
	}

	public int getYaw() {
		return this.yaw;
	}

	public byte getYvel() {
		return this.yvel;
	}

	public int getZ() {
		return this.z;
	}

	public byte getZvel() {
		return this.zvel;
	}

	public boolean isVisible() {
		return this.visible;
	}

	public void setHealth(int percent) {
		this.health = percent / 100F * MAX_HEALTH;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPitch(int pitch) {
		this.pitch = pitch;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void setWorld(Object world) {
		this.world = world;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setXvel(byte xvel) {
		this.xvel = xvel;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setYaw(int yaw) {
		this.yaw = yaw;
	}

	public void setYvel(byte yvel) {
		this.yvel = yvel;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public void setZvel(byte zvel) {
		this.zvel = zvel;
	}
}