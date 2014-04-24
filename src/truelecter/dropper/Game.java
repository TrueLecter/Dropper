package truelecter.dropper;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import truelecter.dropper.game.Tube;
import truelecter.dropper.lobby.LocationGenerator;
import truelecter.dropper.player.DropperPlayer;
import truelecter.dropper.player.Gamemode;

public class Game implements Listener{
	private Dropper plugin;
	private ArrayList<DropperPlayer> players;
	private ArrayList<Tube> tubes;
	private File gameFile;
	private File tubesFile;
	private LocationGenerator spawnLocation;
	private LocationGenerator tempLocation;

	public Game(Dropper plugin) {
		this.plugin = plugin;
		gameFile = new File(plugin.getDataFolder() + File.separator
				+ "game.yml");
		tubesFile = new File(plugin.getDataFolder() + File.separator
				+ "game.yml");
		loadSpawnFromConfig();
		loadTubesFromConfig();
	}

	public void addPlayer(DropperPlayer gamer) {
		players.add(gamer);
		gamer.setTube(tubes.get(0));
		gamer.getPlayer().teleport(spawnLocation.getLocation());
		if (gamer.getGamemode() == Gamemode.SPECTATOR) {
			gamer.getPlayer().setFlying(true);
		}
	}

	private void loadSpawnFromConfig() {
		FileConfiguration config = YamlConfiguration
				.loadConfiguration(gameFile);
		String worldname = config.getString("game.world", null);
		Vector vector = config.getVector("game.vector", null);
		float yaw = (float) config.getDouble("game.yaw", 0.0);
		float pitch = (float) config.getDouble("game.pitch", 0.0);
		if (worldname != null && vector != null) {
			spawnLocation = new LocationGenerator(worldname, vector, yaw, pitch);
		}
	}
	
	@EventHandler(ignoreCancelled=true)
    public void onFall(EntityDamageEvent e){
        if(!(e.getEntity() instanceof Player)) return;
      if(e.getEntity() instanceof Player && e.getCause() == DamageCause.FALL){
          e.setCancelled(true);
          Player p = (Player) e.getEntity();
          if(p.getFallDistance() < spawnLocation.getLocation().getBlockX()){
              e.setCancelled(true);
              p.teleport(spawnLocation.getLocation());
              p.sendMessage(ChatColor.RED+"Ouch! -1");
              DropperPlayer tp = findByName(p.getName());
              if (tp != null){
            	  tp.setFails(tp.getFails()+1);
              }
    	  }
      }
    }
	
	private void loadTubesFromConfig() {
		FileConfiguration config = YamlConfiguration
				.loadConfiguration(tubesFile);
		for (int iid : config.getIntegerList("game.tubes")) {
			Vector vector = config.getVector("game.tube." + String.valueOf(iid)
					+ ".vector", null);
			float yaw = (float) config.getDouble(
					"game.tube." + String.valueOf(iid) + ".yaw", 0.0);
			float pitch = (float) config.getDouble(
					"game.tube." + String.valueOf(iid) + ".pitch", 0.0);
			int points = config.getInt("game.tube." + String.valueOf(iid)
					+ ".points", 0);
			int lowestPoint = config.getInt("game.tube." + String.valueOf(iid)
					+ ".lowestPoint", 0);
			if (vector != null) {
				tempLocation = new LocationGenerator(spawnLocation
						.getLocation().getWorld().getName(), vector, yaw, pitch);
				tubes.add(new Tube(iid, tempLocation.getLocation(), points,
						lowestPoint));
			}
		}

	}
	
	public DropperPlayer findByName(String name){
		for (DropperPlayer gamer : players){
			if (gamer.getPlayer().getName() == name){
				return gamer;
			}
		}
		return null;
	}
}
