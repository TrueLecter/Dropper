package truelecter.dropper.game;
import org.bukkit.Location;

public class Tube {
	private Location spawn;
	private int id;
	private int points;
	private int endLevel;
	
	public Tube(int id, Location spawn, int points, int endLevel){
		this.id = id;
		this.spawn = spawn;
		this.points = points;
		this.endLevel = endLevel;
	}
	
	public int getEnd(){
		return this.endLevel;
	}
	
	public int getId(){
		return this.id;
	}
	
	public int getPoints(){
		return this.points;
	}
	
	public Location getSpawn(){
		return this.spawn;
	}
}
