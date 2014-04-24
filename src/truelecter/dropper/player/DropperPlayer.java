package truelecter.dropper.player;

import org.bukkit.entity.Player;

import truelecter.dropper.game.Tube;

public class DropperPlayer {
	private Player gamer = null;
	private Gamemode gm = Gamemode.GAMER;
	private int fails;
	private Tube tube;
	
	public DropperPlayer(Player player, Gamemode gmode){
		this.gamer = player;
		this.gm = gmode;
		this.fails = 0;
		this.tube = null;
	}
	
	public Player getPlayer(){
		return this.gamer;
	}
	
	public Gamemode getGamemode(){
		return this.gm;
	}
	
	public int getFails(){
		return this.fails;
	}
	
	public Tube getTube(){
		return this.tube;
	}
	
	public void setPlayer(Player player){
		this.gamer = player;
	}
	
	public void setGamemode(Gamemode gmode){
		this.gm = gmode;
	}
	
	public void setFails(int f){
		this.fails = f;
	}
	
	public void setTube(Tube f){
		this.tube = f;
	}
}
