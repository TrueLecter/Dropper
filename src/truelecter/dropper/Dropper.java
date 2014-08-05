package truelecter.dropper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;

import truelecter.dropper.lobby.Lobby;
import truelecter.dropper.player.DropperPlayer;
import truelecter.dropper.player.Gamemode;

public class Dropper extends JavaPlugin implements Listener {
	private FileConfiguration conf;
	private Lobby lobby;
	private boolean running = false;
	private int requiredPlayerToStart;
	private int nonVipPlayers;
	private Game game;
	private boolean ticking = false;
	private int counter = 5;
	private int startCounter;
	private Strings gameString;
	private ArrayList<DropperPlayer> playersDropper = new ArrayList<DropperPlayer>();
	public Economy economy;
	private ScoreboardManager manager;
	private Scoreboard board;
	private Objective objective;
	private String MOTD = "";
	public int tasks[] = { 0, 0, 0, 0, 0, 0 };

	public boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			System.out.println("No vault");
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer()
				.getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			System.out.println("No economy");
			return false;
		}
		economy = rsp.getProvider();
		return economy != null;
	}

	@SuppressWarnings("deprecation")
	private void timer() {
		tasks[0] = getServer().getScheduler().scheduleAsyncRepeatingTask(this,
				new Runnable() {
					@Override
					public void run() {
						if (ticking) {
							counter = counter - 1;
							if (counter == 60)
								broadcastMessage("&9SCDropper &7> До начала минута!");
							if (counter == 30)
								broadcastMessage("&9SCDropper &7> До начала 30 секунд!");
							if (counter == 15)
								broadcastMessage("&9SCDropper &7> До начала 15 секунд!");
							if (counter == 10)
								broadcastMessage("&9SCDropper &7> До начала 10 секунд!");
							if (counter == 5)
								broadcastMessage("&9SCDropper &7> До начала 5 секунд!");
							if (counter == 4)
								broadcastMessage("&9SCDropper &7> До начала 4 секунды!");
							if (counter == 3)
								broadcastMessage("&9SCDropper &7> До начала 3 секунды!");
							if (counter == 2)
								broadcastMessage("&9SCDropper &7> До начала 2 секунды!");
							if (counter == 1)
								broadcastMessage("&9SCDropper &7> До начала 1 секунда!");
						}
						if (Bukkit.getOnlinePlayers().length > 0 && !running) {
							doScore();
						}
						if (counter == 0 && !running) {
							running = true;
							broadcastMessage("&9SCDropper &7> Играйтесь!");
							startGame();
							return;
						}
					}
				}, 0, 20);
		tasks[1] = getServer().getScheduler().scheduleAsyncRepeatingTask(this,
				new Runnable() {
					@Override
					public void run() {
						if (!running) {
							if (getServer().getOnlinePlayers().length >= requiredPlayerToStart) {
								ticking = true;
							} else {
								ticking = false;
								counter = startCounter;
							}
						}
					}
				}, 0, 20);
	}

	public void broadcastMessage(String string) {
		for (Player pl : getServer().getOnlinePlayers()) {
			pl.sendMessage(ChatColor.translateAlternateColorCodes('&', string));
		}
	}

	protected void endInt() {
		for (Player pl : getServer().getOnlinePlayers()) {
			pl.kickPlayer(ChatColor.translateAlternateColorCodes('&',
					"&9SCDropper &7> Конец игры." + game.getWinnersString()));
		}
		game = null;
		getServer().shutdown();
	}

	private void doScore() {
		if (!running) {
			Score score = objective.getScore(Bukkit
					.getOfflinePlayer(ChatColor.AQUA + "Онлайн:"));
			score.setScore(Bukkit.getOnlinePlayers().length);
			Score score2 = objective.getScore(Bukkit
					.getOfflinePlayer(ChatColor.AQUA + "Нужно:"));
			score2.setScore(requiredPlayerToStart);
			if (ticking) {
				Score score3 = objective.getScore(Bukkit
						.getOfflinePlayer(ChatColor.AQUA + "До начала:"));
				score3.setScore(counter);
			}
			for (Player online : Bukkit.getOnlinePlayers()) {
				online.setScoreboard(board);
			}
		}
	}

	@EventHandler
	public void onServerListPing(ServerListPingEvent event) {
		event.setMotd(ChatColor.translateAlternateColorCodes('&', MOTD));
	}

	public void onEnable() {
		MOTD = "&aЖдет игроков";
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		gameString = new Strings(this);
		lobby = new Lobby(this);
		conf = getConfig();
		requiredPlayerToStart = conf.getInt("minPlayers", 2);
		startCounter = conf.getInt("timeToStart", 5);
		nonVipPlayers = conf.getInt("nonVipPlayers", 8);
		if (!setupEconomy()) {
			Logger log = Logger.getLogger("Minecraft");
			log.severe(String.format(
					"[%s] - Disabled due to no Vault dependency found!",
					getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		timer();
		System.out.println(requiredPlayerToStart);
		this.getServer().getScheduler()
				.scheduleSyncDelayedTask(this, new Runnable() {
					public void run() {
						if (running) {
							if (game.getOnlineGamers() < 2) {
								game.endGame();
							}
						}
					}
				}, 60L);
		manager = Bukkit.getScoreboardManager();
		board = manager.getNewScoreboard();

		objective = board.registerNewObjective("lives", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				"&2Инфо"));
	}

	@EventHandler(ignoreCancelled = true)
	public void PlayerQuitEvent(PlayerQuitEvent event) {
		if (running) {
			if (game != null)
				game.removePlayer(event.getPlayer());
		}
	}

	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
	}

	private void saveGameSpawnToConfig(Vector vec, String worldname, float yaw,
			float pitch) {
		File gameFile = new File(getDataFolder() + File.separator + "game.yml");
		FileConfiguration config = YamlConfiguration
				.loadConfiguration(gameFile);
		config.set("game.world", worldname);
		config.set("game.vector", vec);
		config.set("game.yaw", yaw);
		config.set("game.pitch", pitch);
		try {
			config.save(gameFile);
		} catch (IOException e) {
		}
	}

	private void saveJumpSpawnToConfig(Vector vec, String worldname, float yaw,
			float pitch) {
		File gameFile = new File(getDataFolder() + File.separator + "game.yml");
		FileConfiguration config = YamlConfiguration
				.loadConfiguration(gameFile);
		config.set("jump.world", worldname);
		config.set("jump.vector", vec);
		config.set("jump.yaw", yaw);
		config.set("jump.pitch", pitch);
		try {
			config.save(gameFile);
		} catch (IOException e) {
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		doScore();
		if (Bukkit.getOnlinePlayers().length > Bukkit.getMaxPlayers()
				&& !event.getPlayer().hasPermission("dropper.admin")) {
			event.getPlayer().kickPlayer(
					ChatColor.translateAlternateColorCodes('&',
							"&2Сервер совсем переволнен. Лучше подождать."));
		}
		if (Bukkit.getOnlinePlayers().length > nonVipPlayers) {
			if (!event.getPlayer().hasPermission("dropper.vip")) {
				event.getPlayer()
						.kickPlayer(
								ChatColor
										.translateAlternateColorCodes(
												'&',
												"&2Места зарезервированы...\n&7Приобрети &aVIP&7 на сайте &5SplittingCraft.ru и для тебя тоже найдется местечко"));
			}
		}
		if (running) {
			if (event.getPlayer().hasPermission("dropper.vip")) {
				if (game != null)
					game.addPlayer(new DropperPlayer(event.getPlayer(),
							Gamemode.SPECTATOR));
			} else {
				event.getPlayer()
						.kickPlayer(
								ChatColor
										.translateAlternateColorCodes(
												'&',
												"&2Как жаль, что ты не VIP...\n&7Приобрети &aVIP&7 на сайте &5SplittingCraft.ru"));
			}
		} else {
			event.getPlayer().teleport(lobby.getLobbyLocation());
			broadcastMessage("&9SCDropper &7> &4" + event.getPlayer().getName()
					+ " &7подключился!");
		}
		doScore();
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			return false;
		}
		if (cmd.getName().equalsIgnoreCase("setlobbylocation")) {
			if (!sender.hasPermission("dropper.admin")) {
				sender.sendMessage(ChatColor.RED + "Для админов же!");
				return true;
			}
			lobby.saveToConfig(player.getLocation().toVector(), player
					.getLocation().getWorld().getName(), (float) (player
					.getLocation().getYaw() + 120), player.getLocation()
					.getPitch());
			lobby.loadFromConfig();
			sender.sendMessage(ChatColor.GREEN + "Выставили лобби");
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("setgamespawnlocation")) {
			if (!sender.hasPermission("dropper.admin")) {
				sender.sendMessage(ChatColor.RED + "Для админов же!");
				return true;
			}
			saveGameSpawnToConfig(player.getLocation().toVector(), player
					.getLocation().getWorld().getName(), (float) (player
					.getLocation().getYaw() + 120), player.getLocation()
					.getPitch());
			if (running)
				game.loadFromConfig();
			sender.sendMessage(ChatColor.GREEN + "Выставили спавн");
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("setjumpspawnlocation")) {
			if (!sender.hasPermission("dropper.admin")) {
				sender.sendMessage(ChatColor.RED + "Для админов же!");
				return true;
			}
			saveJumpSpawnToConfig(player.getLocation().toVector(), player
					.getLocation().getWorld().getName(), (float) (player
					.getLocation().getYaw() + 120), player.getLocation()
					.getPitch());
			if (running)
				game.loadFromConfig();
			sender.sendMessage(ChatColor.GREEN + "Выставили точку прыжка");
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("setblock1")) {
			if (!sender.hasPermission("dropper.admin")) {
				sender.sendMessage(ChatColor.RED + "Для админов же!");
				return true;
			}
			setBlock1(player.getLocation().getBlock());
			if (running)
				game.loadFromConfig();
			sender.sendMessage(ChatColor.GREEN + "1-я точка басейна выставлена");
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("setblock2")) {
			if (!sender.hasPermission("dropper.admin")) {
				sender.sendMessage(ChatColor.RED + "Для админов же!");
				return true;
			}
			setBlock2(player.getLocation().getBlock());
			if (running)
				game.loadFromConfig();
			sender.sendMessage(ChatColor.GREEN + "2-я точка басейна выставлена");
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("endgame")) {
			if (!sender.hasPermission("dropper.admin")) {
				sender.sendMessage(ChatColor.RED + "Для админов же!");
				return true;
			}
			if (running) {
				game.timerBeforeEnd();
			} else {
				sender.sendMessage(ChatColor.RED + "Игра не идет");
			}
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("startgame")) {
			if (!sender.hasPermission("dropper.admin")) {
				sender.sendMessage(ChatColor.RED + "Для админов же!");
				return true;
			}
			if (running) {
				sender.sendMessage(ChatColor.RED + "Игра уже идет");
			} else {
				startGame();
			}
			return true;
		}
		final CommandSender sender1 = sender;
		if (cmd.getName().equalsIgnoreCase("coins")) {
			Bukkit.getScheduler().runTaskLaterAsynchronously(this,
					new Runnable() {
						@Override
						public void run() {
							sender1.sendMessage(ChatColor
									.translateAlternateColorCodes(
											'&',
											"&9SCDropper &7> Очки: &f"
													+ String.valueOf(
															economy.getBalance(sender1
																	.getName()))
															.replace(".0", "")
													+ "&e©"));
						}
					}, 1);
			return true;
		}
		return false;
	}

	private void setBlock1(Block block) {
		File gameFile = new File(getDataFolder() + File.separator + "game.yml");
		FileConfiguration config = YamlConfiguration
				.loadConfiguration(gameFile);
		config.set("pool.minX", block.getX());
		config.set("pool.minZ", block.getZ());
		config.set("pool.Y", block.getY());
		try {
			config.save(gameFile);
		} catch (IOException e) {
		}
	}

	private void setBlock2(Block block) {
		File gameFile = new File(getDataFolder() + File.separator + "game.yml");
		FileConfiguration config = YamlConfiguration
				.loadConfiguration(gameFile);
		config.set("pool.maxX", block.getX());
		config.set("pool.maxZ", block.getZ());
		config.set("pool.Y", block.getY());
		try {
			config.save(gameFile);
		} catch (IOException e) {
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!event.getPlayer().hasPermission("dropper.admin"))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onFall(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}
		if (running) {
			game.onFall(e);
			return;
		}
		e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onFoodChange(FoodLevelChangeEvent e) {
		e.setFoodLevel(20);
		e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!event.getPlayer().hasPermission("dropper.admin"))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventorClick(InventoryClickEvent event) {
		event.setCancelled(true);
	}

	private void startGame() {
		this.running = true;
		for (Player player : this.getServer().getOnlinePlayers()) {
			playersDropper.add(new DropperPlayer(player, Gamemode.GAMER));
		}
		game = new Game(this, playersDropper);
		getServer().getPluginManager().registerEvents(this.game, this);
	}

	public Lobby getLobby() {
		return this.lobby;
	}

	public Strings getStrings() {
		return this.gameString;
	}

	public int getMinimumPlayers() {
		return this.requiredPlayerToStart;
	}

	public void setMOTD(String motd) {
		this.MOTD = motd;
	}
}
