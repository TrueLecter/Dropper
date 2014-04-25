package truelecter.dropper;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

import truelecter.dropper.game.Tube;
import truelecter.dropper.lobby.LocationGenerator;
import truelecter.dropper.player.DropperPlayer;
import truelecter.dropper.player.Gamemode;

public class Game {
	private Dropper plugin;
	private ArrayList<DropperPlayer> players;
	private ArrayList<Tube> tubes;
	private File gameFile;
	private File tubesFile;
	private LocationGenerator spawnLocation;
	private LocationGenerator tempLocation;
	
	
	public Game(Dropper plugin) {
		this.plugin = plugin;
		gameFile = new File(plugin.getDataFolder() + File.separator + "game.yml");
		tubesFile = new File(plugin.getDataFolder() + File.separator + "game.yml");
		loadSpawnFromConfig();
		loadTubesFromConfig();
	}
	
	public void addPlayer(DropperPlayer gamer){
		players.add(gamer);
		gamer.getPlayer().teleport(spawnLocation.getLocation());
		if (gamer.getGamemode() == Gamemode.SPECTATOR){
			gamer.getPlayer().setFlying(true);
		}
	}
	
	private void loadSpawnFromConfig() {
		FileConfiguration config = YamlConfiguration.loadConfiguration(gameFile);
		String worldname = config.getString("game.world", null);
		Vector vector = config.getVector("game.vector", null);
		float yaw = (float) config.getDouble("game.yaw", 0.0);
		float pitch = (float) config.getDouble("game.pitch", 0.0);
		if (worldname != null && vector != null) {
			spawnLocation = new LocationGenerator(worldname, vector, yaw, pitch);
		}
	}
	
	private void loadTubesFromConfig(){
		FileConfiguration config = YamlConfiguration.loadConfiguration(tubesFile);
		for (int iid : config.getIntegerList("game.tubes")){
			Vector vector = config.getVector("game.tube."+ String.valueOf(iid)+".vector", null);
			float yaw = (float) config.getDouble("game.tube."+ String.valueOf(iid)+".yaw", 0.0);
			float pitch = (float) config.getDouble("game.tube."+ String.valueOf(iid)+".pitch", 0.0);
			int points = config.getInt("game.tube."+ String.valueOf(iid)+".points", 0);
			int lowestPoint = config.getInt("game.tube."+ String.valueOf(iid)+".lowestPoint", 0);
			if (vector != null) {
				tempLocation = new LocationGenerator(spawnLocation.getLocation().getWorld().getName(), vector, yaw, pitch);
				tubes.add(new Tube(iid, tempLocation.getLocation(), points, lowestPoint));
			}
		}
		
	}
}
