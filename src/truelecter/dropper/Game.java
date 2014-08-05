package truelecter.dropper;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
//import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;

import truelecter.dropper.lobby.LocationGenerator;
import truelecter.dropper.player.DropperPlayer;
import truelecter.dropper.player.Gamemode;

public class Game implements Listener {
	private Dropper plugin;
	private ArrayList<DropperPlayer> players = new ArrayList<DropperPlayer>();
	private File gameFile;
	private LocationGenerator spawnLocation;
	private LocationGenerator jumpLocation;
	private int round;
	private ArrayList<String> queue = new ArrayList<String>();
	private Objective objective;
	private ScoreboardManager manager;
	private Scoreboard board;
	private String jumper = "";
	private int maxX = 0;
	private int minX = 0;
	private int YY = 0;
	private int maxZ = 0;
	private int minZ = 0;
	private int kickCounter = 30;
	private int blockNumber = 0;
	private int maxPoints = -1;
	private ArrayList<String> winners = new ArrayList<String>();
	public boolean ended = false;
	private boolean winnersSet = false;
	private String winnersString = "";

	public int getOnlineGamers() {
		int i = 0;
		for (DropperPlayer pl : players) {
			if (pl.getGamemode() == Gamemode.GAMER) {
				i++;
			}
		}
		return i;
	}

	private String getEnding(int i) {
		if (i % 100 < 21 && i % 100 > 4) {
			return "монет";
		}
		if (i % 10 == 1) {
			return "монету";
		}
		if (i % 10 < 5 && i % 10 > 1) {
			return "монеты";
		}
		return "монет";
	}

	private String getEnding1(int i) {
		if (i % 100 < 21 && i % 100 > 4) {
			return "очков";
		}
		if (i % 10 == 1) {
			return "очко";
		}
		if (i % 10 < 5 && i % 10 > 1) {
			return "очка";
		}
		return "очков";
	}

	protected void timerBeforeEnd() {
		if (findByName(jumper) != null) {
			if (!ended)
				findByName(jumper).unsetJumper();
		}
		ended = true;
		if (ended) {
			for (DropperPlayer pl : players) {
				if (pl.getGamemode() == Gamemode.GAMER) {
					if (pl.getPoints() > maxPoints) {
						maxPoints = pl.getPoints();
						winners.clear();
					}
					if (pl.getPoints() == maxPoints) {
						winners.add(pl.getPlayer().getName());
					}
				}
				pl.getPlayer().teleport(plugin.getLobby().getLobbyLocation());
			}
			plugin.broadcastMessage("&9SCDropper &7> &2Конец игры");
			for (DropperPlayer pl : players) {
				if (pl.getGamemode() == Gamemode.GAMER) {
					pl.getPlayer().sendMessage(
							ChatColor.GREEN + "Ваш счет: " + pl.getPoints()
									+ " " + getEnding1(pl.getPoints()));
				}
			}
			plugin.broadcastMessage("&9SCDropper &7> &2Победители получают +50 монет");
			for (String pl : winners) {
				final String tml = pl;
				Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
						new Runnable() {
							@Override
							public void run() {
								if (!findByName(tml).getPlayer().hasPermission(
										"dropper.vip")) {
									plugin.economy.depositPlayer(tml, 50);
								} else {
									plugin.economy.depositPlayer(tml, 75);

								}

							}
						}, 1);
			}
			plugin.broadcastMessage(getWinnersString());
			for (DropperPlayer pl : players) {
				if (pl.getPlayer().hasPermission("dropper.vip")
						&& pl.getGamemode() == Gamemode.GAMER) {
					plugin.broadcastMessage("&9SCDropper &7> " + ChatColor.AQUA
							+ pl.getPlayer().getName()
							+ " получает дополнительно " + pl.getPoints() * 0.5
							+ " " + getEnding(pl.getPoints()) + ". &2(VIP)");
				}
			}
			plugin.getServer().getScheduler().cancelTask(plugin.tasks[2]);
			plugin.tasks[2] = Bukkit.getScheduler().scheduleSyncRepeatingTask(
					plugin, new Runnable() {
						@Override
						public void run() {
							kickCounter = kickCounter - 1;
							if (kickCounter == 1) {
								plugin.broadcastMessage(getWinnersString());
							}
							if (kickCounter == 0) {
								endGame();
							}
							if (kickCounter % 10 == 0) {
								plugin.broadcastMessage(getWinnersString());
							}
						}
					}, 0, 30);
		}
	}

	public Material getLiquidType() {
		FileConfiguration config = YamlConfiguration
				.loadConfiguration(gameFile);
		return config.getString("game.liquid", "water").toLowerCase() == "lava" ? Material.LAVA
				: Material.WATER;
	}

	protected void endGame() {
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				jumpLocation.getLocation().getWorld().getBlockAt(x, YY, z)
						.setType(getLiquidType());
			}
		}
		jumpLocation.getLocation().getWorld().save();
		plugin.endInt();
	}

	public Game(Dropper plugin, ArrayList<DropperPlayer> gamers) {
		players = gamers;
		System.out.println("players = " + players.size());
		round = 0;
		this.plugin = plugin;
		plugin.setMOTD("&6Подготовка");
		gameFile = new File(this.plugin.getDataFolder() + File.separator
				+ "game.yml");
		loadFromConfig();
		for (DropperPlayer pl : players) {
			pl.getPlayer().teleport(spawnLocation.getLocation());

			pl.getPlayer().addPotionEffect(
					new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,
							18000000, 10));
		}
		manager = Bukkit.getScoreboardManager();
		board = manager.getNewScoreboard();

		objective = board.registerNewObjective("lives", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				plugin.getStrings().getSBName()));
		// timer();
		newRound();
		System.out.println("size=" + blockNumber);
	}

	private void doScore() {
		for (Player online : Bukkit.getOnlinePlayers()) {
			DropperPlayer dummy = findByName(online.getName());
			if (dummy == null)
				return;
			if (dummy.getGamemode() == Gamemode.GAMER) {
				String name = online.getName();
				if (name.length() > 14) {
					name = name.substring(0, 14);
				}
				Score score = objective.getScore(Bukkit
						.getOfflinePlayer(ChatColor.AQUA + name));
				score.setScore(dummy.getPoints());
			}
		}
		for (Player online : Bukkit.getOnlinePlayers()) {
			online.setScoreboard(board);
		}
	}

	public void addPlayer(DropperPlayer gamer) {
		players.add(gamer);
		gamer.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
		gamer.getPlayer().teleport(spawnLocation.getLocation());
		gamer.getPlayer().addPotionEffect(
				new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 18000000,
						10));
		if (gamer.getGamemode() == Gamemode.SPECTATOR) {
			gamer.getPlayer()
					.addPotionEffect(
							new PotionEffect(PotionEffectType.INVISIBILITY,
									1800000, 1));
		}
	}

	public void loadFromConfig() {
		FileConfiguration config = YamlConfiguration
				.loadConfiguration(gameFile);
		String worldname = config.getString("game.world", "world");
		Vector vector = config.getVector("game.vector", new Vector(0, 90, 0));
		float yaw = (float) config.getDouble("game.yaw", 0.0);
		float pitch = (float) config.getDouble("game.pitch", 0.0);
		spawnLocation = new LocationGenerator(worldname, vector, yaw, pitch);
		worldname = config.getString("jump.world", "world");
		vector = config.getVector("jump.vector", new Vector(0, 90, 0));
		yaw = (float) config.getDouble("jump.yaw", 0.0);
		pitch = (float) config.getDouble("jump.pitch", 0.0);
		jumpLocation = new LocationGenerator(worldname, vector, yaw, pitch);
		minX = config.getInt("pool.minX", 0);
		maxX = config.getInt("pool.maxX", 0);
		if (minX > maxX) {
			int tmp = minX;
			minX = maxX;
			maxX = tmp;
		}
		minZ = config.getInt("pool.minZ", 0);
		maxZ = config.getInt("pool.maxZ", 0);
		if (minZ > maxZ) {
			int tmp = minZ;
			minZ = maxZ;
			maxZ = tmp;
		}
		blockNumber = (maxX - minX + 1) * (maxZ - minZ + 1);
		YY = config.getInt("pool.Y", 0);
	}

	public void onFall(EntityDamageEvent e) {
		if (e.getCause() == DamageCause.FALL) {
			Player pl = (Player) e.getEntity();
			if (pl.getName().equalsIgnoreCase(jumper)) {
				plugin.broadcastMessage(ChatColor.translateAlternateColorCodes(
						'&', "&9SCDropper &7> ")
						+ plugin.getStrings().getDeathMessage(
								pl.getDisplayName()));
				pl.setVelocity(new Vector(0, 0, 0));
				pl.teleport(spawnLocation.getLocation());
				pl.setVelocity(new Vector(0, 0, 0));
				e.setCancelled(true);
				nextPlayer();
				findByName(pl.getName()).unsetJumper();
			} else
				e.setCancelled(true);
		}
		e.setCancelled(true);
	}

	@EventHandler
	public void onPlayerMove(final PlayerMoveEvent event) {
		if (!(event.getTo().getBlock().getType() == Material.AIR)
				&& event.getTo().getBlock().isLiquid()) {
			if (!event.getPlayer().getName().equalsIgnoreCase(jumper))
				return;
			if (!blockInSet(event.getTo().getBlock())) {
				plugin.broadcastMessage(ChatColor.translateAlternateColorCodes(
						'&', "&9SCDropper &7> ")
						+ plugin.getStrings().getDeathMessage(
								event.getPlayer().getDisplayName()));
				event.getPlayer().setVelocity(new Vector(0, 0, 0));
				event.getPlayer().teleport(spawnLocation.getLocation());
				findByName(event.getPlayer().getName()).unsetJumper();
				nextPlayer();
				return;
			}
			final int points = 1 + pointsNearBlock(event.getTo().getBlock());

			findByName(event.getPlayer().getName()).addPoints(points);
			plugin.broadcastMessage(plugin.getStrings().getPointMessage(
					event.getPlayer().getName(), points));
			event.getPlayer().setVelocity(new Vector(0, 0, 0));
			event.getPlayer().teleport(spawnLocation.getLocation());
			Material type = Material.GLASS;
			if (event.getPlayer().getEquipment().getHelmet() != null)
				if (event.getPlayer().getEquipment().getHelmet().getType()
						.isBlock()) {
					type = event.getPlayer().getEquipment().getHelmet()
							.getType();
				}
			Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
					new Runnable() {
						@Override
						public void run() {
							plugin.economy.depositPlayer(event.getPlayer()
									.getName(), points);
						}
					}, 1);
			event.getTo().getBlock().setType(type);
			nextPlayer();
			findByName(event.getPlayer().getName()).unsetJumper();
		}
	}

	private int pointsNearBlock(Block block) {
		int i = 0;
		boolean blocka = jumpLocation
				.getLocation()
				.getWorld()
				.getBlockAt(block.getLocation().getBlockX() + 1, YY,
						block.getLocation().getBlockZ()).isLiquid();
		boolean blockb = jumpLocation
				.getLocation()
				.getWorld()
				.getBlockAt(block.getLocation().getBlockX(), YY,
						block.getLocation().getBlockZ() - 1).isLiquid();

		boolean blockc = jumpLocation
				.getLocation()
				.getWorld()
				.getBlockAt(block.getLocation().getBlockX() - 1, YY,
						block.getLocation().getBlockZ()).isLiquid();

		boolean blockd = jumpLocation
				.getLocation()
				.getWorld()
				.getBlockAt(block.getLocation().getBlockX(), YY,
						block.getLocation().getBlockZ() + 1).isLiquid();
		if (!blocka)
			i++;
		if (!blockb)
			i++;
		if (!blockc)
			i++;
		if (!blockd)
			i++;
		return i;
	}

	public DropperPlayer findByName(String name) {
		for (DropperPlayer gamer : players) {
			if (gamer.getPlayer().getName() == name) {
				return gamer;
			}
		}
		return null;
	}

	public void newRound() {
		round += 1;
		plugin.broadcastMessage("&9SCDropper &7> Раунд #" + round);
		queue.clear();
		for (DropperPlayer pl : players) {
			if (pl.getGamemode() == Gamemode.GAMER)
				queue.add(pl.getPlayer().getName());
		}
		if (queue.size() < 2) {
			timerBeforeEnd();
			return;
		}
		System.out.println(queue.size());
		nextPlayer();
	}

	public void nextPlayer() {
		if (queue.size() == 0) {
			newRound();
			return;
		}
		jumper = queue.get(0);
		DropperPlayer ple = findByName(queue.get(0));
		ple.getPlayer().setVelocity(new Vector(0, 0, 0));
		if (getPlayced() == blockNumber) {
			timerBeforeEnd();
			return;
		}
		if (getOnlineGamers() < 2) {
			timerBeforeEnd();
			return;
		}
		ple.getPlayer().teleport(jumpLocation.getLocation());
		ple.getPlayer().playSound(ple.getPlayer().getLocation(),
				Sound.LEVEL_UP, 1, 0);
		ple.setJumper();
		queue.remove(0);
		doScore();
		System.out.println(jumper);
	}

	private int getPlayced() {
		int i = 0;
		for (int x = minX; x < maxX + 1; x++) {
			for (int z = minZ; z < maxZ + 1; z++) {
				if (!spawnLocation.getLocation().getWorld()
						.getBlockAt(x, YY, z).isLiquid()) {
					i++;
				}
			}
		}
		System.out.println(i);
		return i;
	}

	protected String getWinnersString() {
		if (!winnersSet)
			makeWinnerString();
		return winnersString;
	}

	private void makeWinnerString() {
		winnersSet = true;
		String formIt = "&7Победители: &a";
		if (winners.size() == 1) {
			formIt = "&7Победитель: &a" + winners.get(0);
		} else {
			for (int i = 0; i < winners.size() - 1; i++) {
				formIt = formIt + winners.get(i) + "&7, &a";
			}
			formIt += winners.get(winners.size() - 1);
		}
		winnersString = formIt;
	}

	public boolean blockInSet(Block block) {
		if (block.getX() <= maxX && block.getX() >= minX && block.getY() == YY
				&& block.getZ() <= maxZ && block.getZ() >= minZ) {
			return true;
		}
		return false;
	}

	public void removePlayer(Player player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
				0, 1));
		if (player.getName().equalsIgnoreCase(jumper)) {
			if (!ended)
				nextPlayer();
		}
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getPlayer().getName() == player.getName()) {
				players.remove(i);
				plugin.broadcastMessage("&9SCDropper &7> &a" + player.getName()
						+ " &7дезертировал");
				break;
			}
		}
		for (int i = 0; i < queue.size(); i++) {
			if (queue.get(i) == player.getName()) {
				queue.remove(i);
				break;
			}
		}
		if (player.hasPermission("dropper.vip")) {
			final String name = player.getName();
			Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
					new Runnable() {
						@Override
						public void run() {
							if (findByName(name) != null)
								plugin.economy.depositPlayer(name,
										findByName(name).getPoints() * 0.5);
						}
					}, 1);
		}
		if (getOnlineGamers() < 2) {
			timerBeforeEnd();
			return;
		}
		doScore();
	}
}
