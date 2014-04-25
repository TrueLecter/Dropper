package truelecter.dropper;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import truelecter.dropper.lobby.Lobby;
import truelecter.dropper.player.DropperPlayer;
import truelecter.dropper.player.Gamemode;

public class Dropper extends JavaPlugin implements Listener {

	private FileConfiguration conf;
	private Lobby lobby;
	private boolean running = false;
	private int requiredPlayerToStart;
	private Game game;
	
	public void onEnable() {
		/*Bukkit.getServer().getPluginManager().registerEvents(this, this);
		lobby = new Lobby(this);
		conf = getConfig();
		requiredPlayerToStart = conf.getInt("minPlayers", 0);
		this.getServer().getScheduler()
				.scheduleSyncDelayedTask(this, new Runnable() {
					@Override
					public void run() {
						if (!running
								&& (getServer().getOnlinePlayers().length > requiredPlayerToStart)) {
							startGame();
						}
					}
				}, 20);*/
		this.getDataFolder().mkdirs();
		Vector vector = new Vector(1,2,3);
		getConfig().set("path.to.vector", vector);
		this.saveConfig();
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onPlayerJoin(PlayerJoinEvent event){
		if (running){
			game.addPlayer(new DropperPlayer(event.getPlayer(),Gamemode.SPECTATOR));
		} else {
			event.getPlayer().teleport(lobby.getLobbyLocation());
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event){
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled=true)
	public void onBlockPlace(BlockPlaceEvent event){
		event.setCancelled(true);
	}
	
	private void startGame() {
		this.running = true;
		game = new Game(this);
	}

	//public FileConfiguration getConfig() {
	//	return this.conf;
	//}

	public Lobby getLobby() {
		return this.lobby;
	}
}
