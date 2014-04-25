package truelecter.dropper.lobby;

import java.io.File;
import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

import truelecter.dropper.Dropper;

public class Lobby {

	private File lobbyFile;

	public Lobby(Dropper plugin) {
		lobbyFile = new File(plugin.getDataFolder() + File.separator + "lobby.yml");
	}

	private LocationGenerator lobbyLocation;

	public boolean isLobbyLocationWorldAvailable() {
		if (isLobbyLocationSet()) {
			return lobbyLocation.isWorldAvailable();
		}
		return false;
	}

	public boolean isLobbyLocationSet() {
		return lobbyLocation != null;
	}

	public Location getLobbyLocation() {
		return lobbyLocation.getLocation();
	}

	public void setLobbyLocation(Location location) {
		this.lobbyLocation = new LocationGenerator(location.getWorld().getName(), location.toVector(), location.getYaw(), location.getPitch());
	}

	public void saveToConfig() {
		FileConfiguration config = new YamlConfiguration();
		if (isLobbyLocationSet()) {
			config.set("lobby.world", lobbyLocation.getWorldName());
			config.set("lobby.vector", lobbyLocation.getVector());
			config.set("lobby.yaw", lobbyLocation.getYaw());
			config.set("lobby.pitch", lobbyLocation.getPitch());
			try {
				config.save(lobbyFile);
			} catch (IOException e) {
			}
		}
	}

	public void loadFromConfig() {
		FileConfiguration config = YamlConfiguration.loadConfiguration(lobbyFile);
		String worldname = config.getString("lobby.world", null);
		Vector vector = config.getVector("lobby.vector", null);
		float yaw = (float) config.getDouble("lobby.yaw", 0.0);
		float pitch = (float) config.getDouble("lobby.pitch", 0.0);
		if (worldname != null && vector != null) {
			lobbyLocation = new LocationGenerator(worldname, vector, yaw, pitch);
		}
	}

}