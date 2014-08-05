package truelecter.dropper.player;

import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DropperPlayer {
	private Player gamer = null;
	private Gamemode gm = Gamemode.GAMER;
	private int points;
	private boolean isJumping;
	private Timer time = new Timer();
	private int counter = 30;
	private boolean left = false;

	public boolean hasLeft() {
		return left;
	}

	public void leave() {
		this.left = true;
	}

	public DropperPlayer(Player player, Gamemode gmode) {
		this.gamer = player;
		this.gm = gmode;
		this.points = 0;
		this.isJumping = false;
		time.schedule(new TimerTask() {
			@Override
			public void run() {
				if (isJumping) {
					counter = counter - 1;
					if (counter == 10)
						if (gamer != null)
							if (!hasLeft())
								gamer.sendMessage(ChatColor
										.translateAlternateColorCodes('&',
												"&9SCDropper &7> Поторапливайся. &410 &7секунд до кика!"));
					if (counter == 0) {
						if (gamer != null)
							if (!hasLeft())
								gamer.kickPlayer(ChatColor
										.translateAlternateColorCodes('&',
												"&4Профукал очередь."));
						isJumping = false;
						return;
					}
					gamer.setExp((30 - counter) / counter);
				}
			}
		}, 0, 1000);
	}

	public Player getPlayer() {
		return this.gamer;
	}

	public void setJumper() {
		counter = 30;
		this.isJumping = true;
	}

	public void unsetJumper() {
		counter = 30;
		this.isJumping = false;
	}

	public Gamemode getGamemode() {
		return this.gm;
	}

	public int getPoints() {
		return this.points;
	}

	public void setPlayer(Player player) {
		this.gamer = player;
	}

	public void setGamemode(Gamemode gmode) {
		this.gm = gmode;
	}

	public void setPoints(int f) {
		this.points = f;
	}

	public void addPoints(int points2) {
		this.points += points2;
	}

}
