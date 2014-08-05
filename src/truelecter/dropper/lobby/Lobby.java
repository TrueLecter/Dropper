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
	private LocationGenerator lobbyLocation;

	public Lobby(Dropper plugin) {
		lobbyFile = new File(plugin.getDataFolder() + File.separator
				+ "lobby.yml");
		loadFromConfig();
	}

	public boolean isLobbyLocationSet() {
		return lobbyLocation != null;
	}

	public Location getLobbyLocation() {
		return lobbyLocation.getLocation();
	}

	public void setLobbyLocation(Location location){
		this.lobbyLocation = new LocationGenerator(location.getWorld()
				.getName(), location.toVector(), location.getYaw(),
				location.getPitch());
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

	public void saveToConfig(Vector vec, String worldname, float yaw,
			float pitch) {
		FileConfiguration config = new YamlConfiguration();
		config.set("lobby.world", worldname);
		config.set("lobby.vector", vec);
		config.set("lobby.yaw", yaw);
		config.set("lobby.pitch", pitch);
		try {
			config.save(lobbyFile);
		} catch (IOException e) {
		}
	}

	public void loadFromConfig() {
		FileConfiguration config = YamlConfiguration
				.loadConfiguration(lobbyFile);
		String worldname = config.getString("lobby.world", "world");
		Vector vector = config.getVector("lobby.vector", new Vector(0, 80, 0));
		float yaw = (float) config.getDouble("lobby.yaw", 0.0);
		float pitch = (float) config.getDouble("lobby.pitch", 0.0);
		lobbyLocation = new LocationGenerator(worldname, vector, yaw, pitch);
	}

}