package truelecter.dropper;

import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import truelecter.dropper.lobby.Lobby;
import truelecter.dropper.player.DropperPlayer;
import truelecter.dropper.player.Gamemode;

public class Dropper extends JavaPlugin implements Listener {

	private FileConfiguration conf;
	private Lobby lobby;
	private boolean running = false;
	private int requiredPlayerToStart;
	private Game game;
	private boolean ticking = false;
	private int counter = 180;
	private Timer time = new Timer();

	public void timer() {
		time.schedule(new TimerTask() {
			public void run() {
				if (ticking)
					counter = counter - 1;
				if (counter == 0 && !running) {
					running = true;
					startGame();
				}
			}
		}, 0, 1000);
	}

	public void onEnable() {
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		lobby = new Lobby(this);
		conf = getConfig();
		requiredPlayerToStart = conf.getInt("minPlayers", 0);
		this.getServer().getScheduler()
				.scheduleSyncDelayedTask(this, new Runnable() {
					@Override
					public void run() {
						if (!running) {
							if (getServer().getOnlinePlayers().length > requiredPlayerToStart) {
								ticking = true;
							} else {
								ticking = false;
								counter = 180;
							}
						}
					}
				}, 20);
		timer();
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (running) {
			game.addPlayer(new DropperPlayer(event.getPlayer(),
					Gamemode.SPECTATOR));
		} else {
			event.getPlayer().teleport(lobby.getLobbyLocation());
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		event.setCancelled(true);
	}

	private void startGame() {
		this.running = true;
		game = new Game(this);
		for (Player player : this.getServer().getOnlinePlayers()) {
			game.addPlayer(new DropperPlayer(player, Gamemode.GAMER));
		}
		getServer().getPluginManager().registerEvents(this.game, this);
	}

	// public FileConfiguration getConfig() {
	// return this.conf;
	// }

	public Lobby getLobby() {
		return this.lobby;
	}
}
