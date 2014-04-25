package truelecter.dropper.lobby;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class LocationGenerator {

	private String worldname;

	protected String getWorldName() {
		return worldname;
	}

	private double x;
	private double y;
	private double z;

	protected Vector getVector() {
		return new Vector(x, y, z);
	}

	private float yaw;

	protected float getYaw() {
		return yaw;
	}

	private float pitch;

	protected float getPitch() {
		return pitch;
	}

	public LocationGenerator(String worldname, Vector vector, float yaw, float pitch) {
		this.worldname = worldname;
		this.x = vector.getX();
		this.y = vector.getY();
		this.z = vector.getZ();
		this.yaw = yaw;
		this.pitch = pitch;
	}

	protected boolean isWorldAvailable() {
		return Bukkit.getWorld(worldname) != null;
	}

	public Location getLocation() {
		if (isWorldAvailable()) {
			return new Location(Bukkit.getWorld(worldname), x, y, z, yaw, pitch);
		}
		return null;
	}

}